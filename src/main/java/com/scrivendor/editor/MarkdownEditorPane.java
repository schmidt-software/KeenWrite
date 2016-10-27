/*
 * Copyright 2015 Karl Tauber <karl at jformdesigner dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivendor.editor;

import static com.scrivendor.Constants.STYLESHEET_EDITOR;
import com.scrivendor.dialogs.ImageDialog;
import com.scrivendor.dialogs.LinkDialog;
import com.scrivendor.ui.AbstractPane;
import com.scrivendor.util.Utils;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import static javafx.scene.input.KeyCode.ENTER;
import javafx.scene.input.KeyEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.NavigationActions;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import org.fxmisc.wellbehaved.event.Nodes;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

/**
 * Markdown editor pane.
 *
 * Uses pegdown (https://github.com/sirthias/pegdown) for styling the markdown
 * content within a text area.
 *
 * @author Karl Tauber,
 */
public class MarkdownEditorPane extends AbstractPane {

  private static final Pattern AUTO_INDENT_PATTERN = Pattern.compile(
    "(\\s*[*+-]\\s+|\\s*[0-9]+\\.\\s+|\\s+)(.*)" );

  private PegDownProcessor pegDownProcessor;
  private StyleClassedTextArea editor;
  private VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
  private String lineSeparator = getLineSeparator();

  private final ReadOnlyObjectWrapper<RootNode> markdownAST = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyDoubleWrapper scrollY = new ReadOnlyDoubleWrapper();
  private final ObjectProperty<Path> path = new SimpleObjectProperty<>();

  public MarkdownEditorPane() {
    initEditor();
    initScrollEventListener();
    initOptionEventListener();
  }

  private void initEditor() {
    final StyleClassedTextArea textArea = getEditor();

    textArea.setWrapText( true );
    textArea.getStyleClass().add( "markdown-editor" );
    textArea.getStylesheets().add( STYLESHEET_EDITOR );

    addEventListener( keyPressed( ENTER ), this::enterPressed );
//    addEventListener( keyPressed( X, SHORTCUT_DOWN ), this::cutLine );

    textArea.textProperty().addListener( (observable, oldText, newText) -> {
      textChanged( newText );
    } );
  }

  /**
   * This method adds listeners to editor events.
   *
   * @see
   * https://docs.oracle.com/javase/8/javafx/api/javafx/scene/input/KeyEvent.html
   *
   * @param <T> The type of event.
   * @param <U>
   * @param event The event of interest.
   * @param consumer The method to call when the event happens.
   */
  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    Nodes.addInputMap( getEditor(), consume( event, consumer ) );
  }
  
  /**
   * Add a listener to update the scrollY property.
   */
  private void initScrollEventListener() {
    final StyleClassedTextArea textArea = getEditor();

    ChangeListener<Double> scrollYListener = (observable, oldValue, newValue) -> {
      double value = textArea.estimatedScrollYProperty().getValue();
      double maxValue = textArea.totalHeightEstimateProperty().getOrElse( 0. ) - textArea.getHeight();
      scrollY.set( (maxValue > 0) ? Math.min( Math.max( value / maxValue, 0 ), 1 ) : 0 );
    };

    textArea.estimatedScrollYProperty().addListener( scrollYListener );
    textArea.totalHeightEstimateProperty().addListener( scrollYListener );
  }

  /**
   * Listen to option changes.
   */
  private void initOptionEventListener() {
    StyleClassedTextArea textArea = getEditor();

    InvalidationListener listener = e -> {
      if( textArea.getScene() == null ) {
        // Editor closed but not yet garbage collected.
        return;
      }

      // Re-process markdown if markdown extensions option changes.
      if( e == getOptions().markdownExtensionsProperty() ) {
        pegDownProcessor = null;
        textChanged( textArea.getText() );
      }
    };

    WeakInvalidationListener weakOptionsListener = new WeakInvalidationListener( listener );
    getOptions().markdownExtensionsProperty().addListener( weakOptionsListener );
  }

  private void setEditor( StyleClassedTextArea textArea ) {
    this.editor = textArea;
  }

  public synchronized StyleClassedTextArea getEditor() {
    if( this.editor == null ) {
      setEditor( createTextArea() );
    }

    return this.editor;
  }

  protected StyleClassedTextArea createTextArea() {
    return new StyleClassedTextArea( false );
  }

  /**
   * Returns the scroll pane that contains the text area.
   * 
   * @return 
   */
  public Node getNode() {
    if( this.scrollPane == null ) {
      this.scrollPane = createScrollPane();
    }

    return scrollPane;
  }

  protected VirtualizedScrollPane<StyleClassedTextArea> createScrollPane() {
    return new VirtualizedScrollPane<>( getEditor() );
  }

  public UndoManager getUndoManager() {
    return getEditor().getUndoManager();
  }

  @Override
  public void requestFocus() {
    Platform.runLater( () -> getEditor().requestFocus() );
  }

  private String getLineSeparator() {
    final String separator = getOptions().getLineSeparator();
    return (separator != null) ? separator : System.getProperty( "line.separator", "\n" );
  }

  private String determineLineSeparator( String str ) {
    int strLength = str.length();
    for( int i = 0; i < strLength; i++ ) {
      char ch = str.charAt( i );
      if( ch == '\n' ) {
        return (i > 0 && str.charAt( i - 1 ) == '\r') ? "\r\n" : "\n";
      }
    }
    return getLineSeparator();
  }

  public String getMarkdown() {
    String markdown = getEditor().getText();
    if( !lineSeparator.equals( "\n" ) ) {
      markdown = markdown.replace( "\n", lineSeparator );
    }
    return markdown;
  }

  public void setMarkdown( String markdown ) {
    lineSeparator = determineLineSeparator( markdown );
    getEditor().replaceText( markdown );
    getEditor().selectRange( 0, 0 );
  }

  public ObservableValue<String> markdownProperty() {
    return getEditor().textProperty();
  }

  public RootNode getMarkdownAST() {
    return markdownAST.get();
  }

  public ReadOnlyObjectProperty<RootNode> markdownASTProperty() {
    return markdownAST.getReadOnlyProperty();
  }

  public double getScrollY() {
    return scrollY.get();
  }

  public ReadOnlyDoubleProperty scrollYProperty() {
    return scrollY.getReadOnlyProperty();
  }

  public Path getPath() {
    return path.get();
  }

  public void setPath( Path path ) {
    this.path.set( path );
  }

  public ObjectProperty<Path> pathProperty() {
    return path;
  }

  private Path getParentPath() {
    Path parentPath = getPath();
    return (parentPath != null) ? parentPath.getParent() : null;
  }

  private void textChanged( String newText ) {
    RootNode astRoot = parseMarkdown( newText );
    applyHighlighting( astRoot );
    markdownAST.set( astRoot );
  }

  /**
   * TODO: Change to interface so that other processors can be pipelined.
   *
   * @return
   */
  private synchronized PegDownProcessor getPegDownProcessor() {
    if( this.pegDownProcessor == null ) {
      this.pegDownProcessor = createPegDownProcessor();
    }

    return this.pegDownProcessor;
  }

  protected PegDownProcessor createPegDownProcessor() {
    return new PegDownProcessor( getOptions().getMarkdownExtensions() );
  }

  private RootNode parseMarkdown( String text ) {
    return getPegDownProcessor().parseMarkdown( text.toCharArray() );
  }

  private void applyHighlighting( RootNode astRoot ) {
    MarkdownSyntaxHighlighter.highlight( editor, astRoot );
  }

  private void enterPressed( KeyEvent e ) {
    final String currentLine = getEditor().getText( getEditor().getCurrentParagraph() );
    final Matcher matcher = AUTO_INDENT_PATTERN.matcher( currentLine );

    String newText = "\n";

    if( matcher.matches() ) {
      if( !matcher.group( 2 ).isEmpty() ) {
        // indent new line with same whitespace characters and list markers as current line
        newText = newText.concat( matcher.group( 1 ) );
      } else {
        // current line contains only whitespace characters and list markers
        // --> empty current line
        final int caretPosition = getEditor().getCaretPosition();
        getEditor().selectRange( caretPosition - currentLine.length(), caretPosition );
      }
    }

    getEditor().replaceSelection( newText );
  }

  /**
   * @param e
   */
  private void deleteLine( KeyEvent e ) {
    final StyleClassedTextArea textArea = getEditor();

    // Move the caret to the start of the line.
//    textArea.lineStart( NavigationActions.SelectionPolicy.CLEAR );
    final int start = textArea.getCaretPosition() - textArea.getCaretColumn();

    System.out.println( "start = " + start );

    // Move the caret to the end of the line.
    textArea.lineEnd( NavigationActions.SelectionPolicy.CLEAR );

//    final int end = start + textArea.getParagraph( textArea.getCurrentParagraph() ).length() + 1;
    final int end = textArea.getCaretPosition();

    System.out.println( "end = " + end );

    textArea.deleteText( start, end );
  }

  public void undo() {
    getEditor().getUndoManager().undo();
  }

  public void redo() {
    getEditor().getUndoManager().redo();
  }

  public void surroundSelection( String leading, String trailing ) {
    surroundSelection( leading, trailing, null );
  }

  public void surroundSelection( String leading, String trailing, String hint ) {
    // Note: not using getEditor().insertText() to insert leading and trailing
    //       because this would add two changes to undo history

    IndexRange selection = getEditor().getSelection();
    int start = selection.getStart();
    int end = selection.getEnd();

    String selectedText = getEditor().getSelectedText();

    // remove leading and trailing whitespaces from selected text
    String trimmedSelectedText = selectedText.trim();
    if( trimmedSelectedText.length() < selectedText.length() ) {
      start += selectedText.indexOf( trimmedSelectedText );
      end = start + trimmedSelectedText.length();
    }

    // remove leading whitespaces from leading text if selection starts at zero
    if( start == 0 ) {
      leading = Utils.ltrim( leading );
    }

    // remove trailing whitespaces from trailing text if selection ends at text end
    if( end == getEditor().getLength() ) {
      trailing = Utils.rtrim( trailing );
    }

    // remove leading line separators from leading text
    // if there are line separators before the selected text
    if( leading.startsWith( "\n" ) ) {
      for( int i = start - 1; i >= 0 && leading.startsWith( "\n" ); i-- ) {
        if( !"\n".equals( getEditor().getText( i, i + 1 ) ) ) {
          break;
        }
        leading = leading.substring( 1 );
      }
    }

    // remove trailing line separators from trailing or leading text
    // if there are line separators after the selected text
    boolean trailingIsEmpty = trailing.isEmpty();
    String str = trailingIsEmpty ? leading : trailing;
    if( str.endsWith( "\n" ) ) {
      for( int i = end; i < getEditor().getLength() && str.endsWith( "\n" ); i++ ) {
        if( !"\n".equals( getEditor().getText( i, i + 1 ) ) ) {
          break;
        }
        str = str.substring( 0, str.length() - 1 );
      }
      if( trailingIsEmpty ) {
        leading = str;
      } else {
        trailing = str;
      }
    }

    int selStart = start + leading.length();
    int selEnd = end + leading.length();

    // insert hint text if selection is empty
    if( hint != null && trimmedSelectedText.isEmpty() ) {
      trimmedSelectedText = hint;
      selEnd = selStart + hint.length();
    }

    // prevent undo merging with previous text entered by user
    getEditor().getUndoManager().preventMerge();

    // replace text and update selection
    getEditor().replaceText( start, end, leading + trimmedSelectedText + trailing );
    getEditor().selectRange( selStart, selEnd );
  }

  public void insertLink() {
    LinkDialog dialog = new LinkDialog( getNode().getScene().getWindow(), getParentPath() );
    dialog.showAndWait().ifPresent( result -> {
      getEditor().replaceSelection( result );
    } );
  }

  public void insertImage() {
    ImageDialog dialog = new ImageDialog( getNode().getScene().getWindow(), getParentPath() );
    dialog.showAndWait().ifPresent( result -> {
      getEditor().replaceSelection( result );
    } );
  }
}
