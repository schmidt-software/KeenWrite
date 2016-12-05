/*
 * Copyright 2016 Karl Tauber and White Magic Software, Ltd.
 *
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
package com.scrivenvar.editor;

import static com.scrivenvar.Constants.STYLESHEET_EDITOR;
import com.scrivenvar.dialogs.ImageDialog;
import com.scrivenvar.dialogs.LinkDialog;
import com.scrivenvar.processors.MarkdownProcessor;
import com.scrivenvar.util.Utils;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Node;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialog;
import javafx.scene.control.IndexRange;
import static javafx.scene.input.KeyCode.ENTER;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.fxmisc.richtext.StyleClassedTextArea;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Markdown editor pane.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class MarkdownEditorPane extends EditorPane {

  private static final Pattern AUTO_INDENT_PATTERN = Pattern.compile(
    "(\\s*[*+-]\\s+|\\s*[0-9]+\\.\\s+|\\s+)(.*)" );

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

    // TODO: Wait for implementation that allows cutting lines, not paragraphs.
//    addEventListener( keyPressed( X, SHORTCUT_DOWN ), this::cutLine );
  }

  /**
   * Add a listener to update the scrollY property.
   */
  private void initScrollEventListener() {
    final StyleClassedTextArea textArea = getEditor();

    ChangeListener<Double> scrollYListener = (observable, oldValue, newValue) -> {
      double value = textArea.estimatedScrollYProperty().getValue();
      double maxValue = textArea.totalHeightEstimateProperty().getOrElse( 0. ) - textArea.getHeight();
      setScrollY( (maxValue > 0) ? Math.min( Math.max( value / maxValue, 0 ), 1 ) : 0 );
    };

    textArea.estimatedScrollYProperty().addListener( scrollYListener );
    textArea.totalHeightEstimateProperty().addListener( scrollYListener );
  }

  /**
   * Listen to option changes.
   */
  private void initOptionEventListener() {
    final InvalidationListener listener = e -> {
    };

    WeakInvalidationListener weakOptionsListener = new WeakInvalidationListener( listener );
    getOptions().markdownExtensionsProperty().addListener( weakOptionsListener );
  }

  public ObservableValue<String> markdownProperty() {
    return getEditor().textProperty();
  }

  private void enterPressed( final KeyEvent e ) {
    final StyleClassedTextArea textArea = getEditor();
    final String currentLine = textArea.getText( textArea.getCurrentParagraph() );
    final Matcher matcher = AUTO_INDENT_PATTERN.matcher( currentLine );

    String newText = "\n";

    if( matcher.matches() ) {
      if( !matcher.group( 2 ).isEmpty() ) {
        // indent new line with same whitespace characters and list markers as current line
        newText = newText.concat( matcher.group( 1 ) );
      } else {
        // current line contains only whitespace characters and list markers
        // --> empty current line
        final int caretPosition = textArea.getCaretPosition();
        textArea.selectRange( caretPosition - currentLine.length(), caretPosition );
      }
    }

    textArea.replaceSelection( newText );
  }

  public void surroundSelection( final String leading, final String trailing ) {
    surroundSelection( leading, trailing, null );
  }

  public void surroundSelection( String leading, String trailing, final String hint ) {
    final StyleClassedTextArea textArea = getEditor();

    // Note: not using textArea.insertText() to insert leading and trailing
    // because this would add two changes to undo history
    final IndexRange selection = textArea.getSelection();
    int start = selection.getStart();
    int end = selection.getEnd();

    final String selectedText = textArea.getSelectedText();

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
    if( end == textArea.getLength() ) {
      trailing = Utils.rtrim( trailing );
    }

    // remove leading line separators from leading text
    // if there are line separators before the selected text
    if( leading.startsWith( "\n" ) ) {
      for( int i = start - 1; i >= 0 && leading.startsWith( "\n" ); i-- ) {
        if( !"\n".equals( textArea.getText( i, i + 1 ) ) ) {
          break;
        }
        leading = leading.substring( 1 );
      }
    }

    // remove trailing line separators from trailing or leading text
    // if there are line separators after the selected text
    final boolean trailingIsEmpty = trailing.isEmpty();
    String str = trailingIsEmpty ? leading : trailing;

    if( str.endsWith( "\n" ) ) {
      final int length = textArea.getLength();

      for( int i = end; i < length && str.endsWith( "\n" ); i++ ) {
        if( !"\n".equals( textArea.getText( i, i + 1 ) ) ) {
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
    getUndoManager().preventMerge();

    // replace text and update selection
    textArea.replaceText( start, end, leading + trimmedSelectedText + trailing );
    textArea.selectRange( selStart, selEnd );
  }

  /**
   * Returns one of: selected text, word under cursor, or parsed hyperlink from
   * the markdown AST.
   *
   * @return
   */
  private HyperlinkModel getHyperlink() {
    final StyleClassedTextArea textArea = getEditor();
    final String selectedText = textArea.getSelectedText();

    // Get the current paragraph, convert to Markdown nodes.
    final MarkdownProcessor mp = new MarkdownProcessor( null );
    final int p = textArea.getCurrentParagraph();
    final String paragraph = textArea.getText( p );
    final Node node = mp.toNode( paragraph );
    final LinkVisitor visitor = new LinkVisitor( textArea.getCaretColumn() );
    final Link link = visitor.process( node );

    if( link != null ) {
      textArea.selectRange( p, link.getStartOffset(), p, link.getEndOffset() );
    }

    final HyperlinkModel model = createHyperlinkModel(
      link, selectedText, "https://website.com"
    );

    return model;
  }

  private HyperlinkModel createHyperlinkModel(
    final Link link, final String selection, final String url ) {

    return link == null
      ? new HyperlinkModel( selection, url )
      : new HyperlinkModel( link );
  }

  private Path getParentPath() {
    final Path parentPath = getPath();
    return (parentPath != null) ? parentPath.getParent() : null;
  }

  private Dialog<String> createLinkDialog() {
    return new LinkDialog( getWindow(), getHyperlink(), getParentPath() );
  }

  private Dialog<String> createImageDialog() {
    return new ImageDialog( getWindow(), getParentPath() );
  }

  private void insertObject( final Dialog<String> dialog ) {
    dialog.showAndWait().ifPresent( result -> {
      getEditor().replaceSelection( result );
    } );
  }

  public void insertLink() {
    insertObject( createLinkDialog() );
  }

  public void insertImage() {
    insertObject( createImageDialog() );
  }

  private Window getWindow() {
    return getScrollPane().getScene().getWindow();
  }
}
