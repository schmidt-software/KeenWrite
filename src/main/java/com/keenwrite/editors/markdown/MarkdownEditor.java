/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.markdown;

import com.keenwrite.Caret;
import com.keenwrite.Constants;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.preferences.LocaleProperty;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.spelling.impl.TextEditorSpeller;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.Nodes;

import java.io.File;
import java.nio.charset.Charset;
import java.text.BreakIterator;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.keenwrite.Constants.*;
import static com.keenwrite.MainApp.keyDown;
import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.events.TextEditorFocusEvent.fireTextEditorFocus;
import static com.keenwrite.preferences.WorkspaceKeys.*;
import static java.lang.Character.isWhitespace;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static javafx.application.Platform.runLater;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static org.apache.commons.lang3.StringUtils.stripEnd;
import static org.apache.commons.lang3.StringUtils.stripStart;
import static org.fxmisc.richtext.model.StyleSpans.singleton;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Responsible for editing Markdown documents.
 */
public final class MarkdownEditor extends BorderPane implements TextEditor {
  /**
   * Regular expression that matches the type of markup block. This is used
   * when Enter is pressed to continue the block environment.
   */
  private static final Pattern PATTERN_AUTO_INDENT = Pattern.compile(
    "(\\s*[*+-]\\s+|\\s*[0-9]+\\.\\s+|\\s+)(.*)" );

  /**
   * The text editor.
   */
  private final StyleClassedTextArea mTextArea =
    new StyleClassedTextArea( false );

  /**
   * Wraps the text editor in scrollbars.
   */
  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
    new VirtualizedScrollPane<>( mTextArea );

  private final Workspace mWorkspace;

  /**
   * Tracks where the caret is located in this document. This offers observable
   * properties for caret position changes.
   */
  private final Caret mCaret = createCaret( mTextArea );

  /**
   * File being edited by this editor instance.
   */
  private File mFile;

  /**
   * Set to {@code true} upon text or caret position changes. Value is {@code
   * false} by default.
   */
  private final BooleanProperty mDirty = new SimpleBooleanProperty();

  /**
   * Opened file's character encoding, or {@link Constants#DEFAULT_CHARSET} if
   * either no encoding could be determined or this is a new (empty) file.
   */
  private final Charset mEncoding;

  /**
   * Tracks whether the in-memory definitions have changed with respect to the
   * persisted definitions.
   */
  private final BooleanProperty mModified = new SimpleBooleanProperty();

  public MarkdownEditor( final Workspace workspace ) {
    this( DOCUMENT_DEFAULT, workspace );
  }

  public MarkdownEditor( final File file, final Workspace workspace ) {
    mEncoding = open( mFile = file );
    mWorkspace = workspace;

    initTextArea( mTextArea );
    initStyle( mTextArea );
    initScrollPane( mScrollPane );
    initSpellchecker( mTextArea );
    initHotKeys();
    initUndoManager();
  }

  private void initTextArea( final StyleClassedTextArea textArea ) {
    textArea.setWrapText( true );
    textArea.requestFollowCaret();
    textArea.moveTo( 0 );

    textArea.textProperty().addListener( ( c, o, n ) -> {
      // Fire, regardless of whether the caret position has changed.
      mDirty.set( false );

      // Prevent a caret position change from raising the dirty bits.
      mDirty.set( true );
    } );

    textArea.caretPositionProperty().addListener( ( c, o, n ) -> {
      // Fire when the caret position has changed and the text has not.
      mDirty.set( true );
      mDirty.set( false );
    } );

    textArea.focusedProperty().addListener( ( c, o, n ) -> {
      if( n != null && n ) {
        fireTextEditorFocus( this );
      }
    } );
  }

  private void initStyle( final StyleClassedTextArea textArea ) {
    textArea.getStyleClass().add( "markdown" );

    final var stylesheets = textArea.getStylesheets();
    stylesheets.add( getStylesheetPath( getLocale() ) );

    localeProperty().addListener( ( c, o, n ) -> {
      if( n != null ) {
        stylesheets.clear();
        stylesheets.add( getStylesheetPath( getLocale() ) );
      }
    } );

    fontNameProperty().addListener(
      ( c, o, n ) ->
        setFont( mTextArea, getFontName(), getFontSize() )
    );

    fontSizeProperty().addListener(
      ( c, o, n ) ->
        setFont( mTextArea, getFontName(), getFontSize() )
    );

    setFont( mTextArea, getFontName(), getFontSize() );
  }

  private void initScrollPane(
    final VirtualizedScrollPane<StyleClassedTextArea> scrollpane ) {
    scrollpane.setVbarPolicy( ALWAYS );
    setCenter( scrollpane );
  }

  private void initSpellchecker( final StyleClassedTextArea textarea ) {
    final var speller = new TextEditorSpeller();
    speller.checkDocument( textarea );
    speller.checkParagraphs( textarea );
  }

  private void initHotKeys() {
    addEventListener( keyPressed( ENTER ), this::onEnterPressed );
    addEventListener( keyPressed( X, CONTROL_DOWN ), this::cut );
    addEventListener( keyPressed( TAB ), this::tab );
    addEventListener( keyPressed( TAB, SHIFT_DOWN ), this::untab );
    addEventListener( keyPressed( INSERT ), this::onInsertPressed );
  }

  private void initUndoManager() {
    final var undoManager = getUndoManager();
    final var markedPosition = undoManager.atMarkedPositionProperty();

    undoManager.forgetHistory();
    undoManager.mark();
    mModified.bind( Bindings.not( markedPosition ) );
  }

  @Override
  public void moveTo( final int offset ) {
    assert 0 <= offset && offset <= mTextArea.getLength();
    mTextArea.moveTo( offset );
    mTextArea.requestFollowCaret();
  }

  /**
   * Delegate the focus request to the text area itself.
   */
  @Override
  public void requestFocus() {
    mTextArea.requestFocus();
  }

  @Override
  public void setText( final String text ) {
    mTextArea.clear();
    mTextArea.appendText( text );
    mTextArea.getUndoManager().mark();
  }

  @Override
  public String getText() {
    return mTextArea.getText();
  }

  @Override
  public Charset getEncoding() {
    return mEncoding;
  }

  @Override
  public File getFile() {
    return mFile;
  }

  @Override
  public void rename( final File file ) {
    mFile = file;
  }

  @Override
  public void undo() {
    final var manager = getUndoManager();
    xxdo( manager::isUndoAvailable, manager::undo, "Main.status.error.undo" );
  }

  @Override
  public void redo() {
    final var manager = getUndoManager();
    xxdo( manager::isRedoAvailable, manager::redo, "Main.status.error.redo" );
  }

  /**
   * Performs an undo or redo action, if possible, otherwise displays an error
   * message to the user.
   *
   * @param ready  Answers whether the action can be executed.
   * @param action The action to execute.
   * @param key    The informational message key having a value to display if
   *               the {@link Supplier} is not ready.
   */
  private void xxdo(
    final Supplier<Boolean> ready, final Runnable action, final String key ) {
    if( ready.get() ) {
      action.run();
    }
    else {
      clue( key );
    }
  }

  @Override
  public void cut() {
    final var selected = mTextArea.getSelectedText();

    // Emulate selecting the current line by firing Home then Shift+Down Arrow.
    if( selected == null || selected.isEmpty() ) {
      // Note: mTextArea.selectLine() does not select empty lines.
      mTextArea.fireEvent( keyDown( HOME, false ) );
      mTextArea.fireEvent( keyDown( DOWN, true ) );
    }

    mTextArea.cut();
  }

  @Override
  public void copy() {
    mTextArea.copy();
  }

  @Override
  public void paste() {
    mTextArea.paste();
  }

  @Override
  public void selectAll() {
    mTextArea.selectAll();
  }

  @Override
  public void bold() {
    enwrap( "**" );
  }

  @Override
  public void italic() {
    enwrap( "*" );
  }

  @Override
  public void superscript() {
    enwrap( "^" );
  }

  @Override
  public void subscript() {
    enwrap( "~" );
  }

  @Override
  public void strikethrough() {
    enwrap( "~~" );
  }

  @Override
  public void blockquote() {
    block( "> " );
  }

  @Override
  public void code() {
    enwrap( "`" );
  }

  @Override
  public void fencedCodeBlock() {
    enwrap( "\n\n```\n", "\n```\n\n" );
  }

  @Override
  public void heading( final int level ) {
    final var hashes = new String( new char[ level ] ).replace( "\0", "#" );
    block( format( "%s ", hashes ) );
  }

  @Override
  public void unorderedList() {
    block( "* " );
  }

  @Override
  public void orderedList() {
    block( "1. " );
  }

  @Override
  public void horizontalRule() {
    block( format( "---%n%n" ) );
  }

  @Override
  public Node getNode() {
    return this;
  }

  @Override
  public ReadOnlyBooleanProperty modifiedProperty() {
    return mModified;
  }

  @Override
  public void clearModifiedProperty() {
    getUndoManager().mark();
  }

  @Override
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return mScrollPane;
  }

  @Override
  public StyleClassedTextArea getTextArea() {
    return mTextArea;
  }

  private final Map<String, IndexRange> mStyles = new HashMap<>();

  @Override
  public void stylize( final IndexRange range, final String style ) {
    final var began = range.getStart();
    final var ended = range.getEnd() + 1;

    assert 0 <= began && began <= ended;
    assert style != null;

    // TODO: Ensure spell check and find highlights can coexist.
//    final var spans = mTextArea.getStyleSpans( range );
//    System.out.println( "SPANS: " + spans );

//    final var spans = mTextArea.getStyleSpans( range );
//    mTextArea.setStyleSpans( began, merge( spans, range.getLength(), style
//    ) );

//    final var builder = new StyleSpansBuilder<Collection<String>>();
//    builder.add( singleton( style ), range.getLength() + 1 );
//    mTextArea.setStyleSpans( began, builder.create() );

//    final var s = mTextArea.getStyleSpans( began, ended );
//    System.out.println( "STYLES: " +s );

    mStyles.put( style, range );
    mTextArea.setStyleClass( began, ended, style );

    // Ensure that whenever the user interacts with the text that the found
    // word will have its highlighting removed. The handler removes itself.
    // This won't remove the highlighting if the caret position moves by mouse.
    final var handler = mTextArea.getOnKeyPressed();
    mTextArea.setOnKeyPressed( ( event ) -> {
      mTextArea.setOnKeyPressed( handler );
      unstylize( style );
    } );

    //mTextArea.setStyleSpans(began, ended, s);
  }

  private static StyleSpans<Collection<String>> merge(
    StyleSpans<Collection<String>> spans, int len, String style ) {
    spans = spans.overlay(
      singleton( singletonList( style ), len ),
      ( bottomSpan, list ) -> {
        final List<String> l =
          new ArrayList<>( bottomSpan.size() + list.size() );
        l.addAll( bottomSpan );
        l.addAll( list );
        return l;
      } );

    return spans;
  }

  @Override
  public void unstylize( final String style ) {
    final var indexes = mStyles.remove( style );
    if( indexes != null ) {
      mTextArea.clearStyle( indexes.getStart(), indexes.getEnd() + 1 );
    }
  }

  @Override
  public Caret getCaret() {
    return mCaret;
  }

  private Caret createCaret( final StyleClassedTextArea editor ) {
    return Caret
      .builder()
      .with( Caret.Mutator::setEditor, editor )
      .build();
  }

  /**
   * This method adds listeners to editor events.
   *
   * @param <T>      The event type.
   * @param <U>      The consumer type for the given event type.
   * @param event    The event of interest.
   * @param consumer The method to call when the event happens.
   */
  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    Nodes.addInputMap( mTextArea, consume( event, consumer ) );
  }

  private void onEnterPressed( final KeyEvent ignored ) {
    final var currentLine = getCaretParagraph();
    final var matcher = PATTERN_AUTO_INDENT.matcher( currentLine );

    // By default, insert a new line by itself.
    String newText = NEWLINE;

    // If the pattern was matched then determine what block type to continue.
    if( matcher.matches() ) {
      if( matcher.group( 2 ).isEmpty() ) {
        final var pos = mTextArea.getCaretPosition();
        mTextArea.selectRange( pos - currentLine.length(), pos );
      }
      else {
        // Indent the new line with the same whitespace characters and
        // list markers as current line. This ensures that the indentation
        // is propagated.
        newText = newText.concat( matcher.group( 1 ) );
      }
    }

    mTextArea.replaceSelection( newText );
  }

  /**
   * TODO: 105 - Insert key toggle overwrite (typeover) mode
   *
   * @param ignored Unused.
   */
  private void onInsertPressed( final KeyEvent ignored ) {
  }

  private void cut( final KeyEvent event ) {
    cut();
  }

  private void tab( final KeyEvent event ) {
    final var range = mTextArea.selectionProperty().getValue();
    final var sb = new StringBuilder( 1024 );

    if( range.getLength() > 0 ) {
      final var selection = mTextArea.getSelectedText();

      selection.lines().forEach(
        ( l ) -> sb.append( "\t" ).append( l ).append( NEWLINE )
      );
    }
    else {
      sb.append( "\t" );
    }

    mTextArea.replaceSelection( sb.toString() );
  }

  private void untab( final KeyEvent event ) {
    final var range = mTextArea.selectionProperty().getValue();

    if( range.getLength() > 0 ) {
      final var selection = mTextArea.getSelectedText();
      final var sb = new StringBuilder( selection.length() );

      selection.lines().forEach(
        ( l ) -> sb.append( l.startsWith( "\t" ) ? l.substring( 1 ) : l )
                   .append( NEWLINE )
      );

      mTextArea.replaceSelection( sb.toString() );
    }
    else {
      final var p = getCaretParagraph();

      if( p.startsWith( "\t" ) ) {
        mTextArea.selectParagraph();
        mTextArea.replaceSelection( p.substring( 1 ) );
      }
    }
  }

  /**
   * Observers may listen for changes to the property returned from this method
   * to receive notifications when either the text or caret have changed. This
   * should not be used to track whether the text has been modified.
   */
  public void addDirtyListener( ChangeListener<Boolean> listener ) {
    mDirty.addListener( listener );
  }

  /**
   * Surrounds the selected text or word under the caret in Markdown markup.
   *
   * @param token The beginning and ending token for enclosing the text.
   */
  private void enwrap( final String token ) {
    enwrap( token, token );
  }

  /**
   * Surrounds the selected text or word under the caret in Markdown markup.
   *
   * @param began The beginning token for enclosing the text.
   * @param ended The ending token for enclosing the text.
   */
  private void enwrap( final String began, String ended ) {
    // Ensure selected text takes precedence over the word at caret position.
    final var selected = mTextArea.selectionProperty().getValue();
    final var range = selected.getLength() == 0
      ? getCaretWord()
      : selected;
    String text = mTextArea.getText( range );

    int length = range.getLength();
    text = stripStart( text, null );
    final int beganIndex = range.getStart() + (length - text.length());

    length = text.length();
    text = stripEnd( text, null );
    final int endedIndex = range.getEnd() - (length - text.length());

    mTextArea.replaceText( beganIndex, endedIndex, began + text + ended );
  }

  /**
   * Inserts the given block-level markup at the current caret position
   * within the document. This will prepend two blank lines to ensure that
   * the block element begins at the start of a new line.
   *
   * @param markup The text to insert at the caret.
   */
  private void block( final String markup ) {
    final int pos = mTextArea.getCaretPosition();
    mTextArea.insertText( pos, format( "%n%n%s", markup ) );
  }

  /**
   * Returns the caret position within the current paragraph.
   *
   * @return A value from 0 to the length of the current paragraph.
   */
  private int getCaretColumn() {
    return mTextArea.getCaretColumn();
  }

  @Override
  public IndexRange getCaretWord() {
    final var paragraph = getCaretParagraph();
    final var length = paragraph.length();
    final var column = getCaretColumn();

    var began = column;
    var ended = column;

    while( began > 0 && !isWhitespace( paragraph.charAt( began - 1 ) ) ) {
      began--;
    }

    while( ended < length && !isWhitespace( paragraph.charAt( ended ) ) ) {
      ended++;
    }

    final var iterator = BreakIterator.getWordInstance();
    iterator.setText( paragraph );

    while( began < length && iterator.isBoundary( began + 1 ) ) {
      began++;
    }

    while( ended > 0 && iterator.isBoundary( ended - 1 ) ) {
      ended--;
    }

    final var offset = getCaretDocumentOffset( column );

    return IndexRange.normalize( began + offset, ended + offset );
  }

  private int getCaretDocumentOffset( final int column ) {
    return mTextArea.getCaretPosition() - column;
  }

  /**
   * Returns the index of the paragraph where the caret resides.
   *
   * @return A number greater than or equal to 0.
   */
  private int getCurrentParagraph() {
    return mTextArea.getCurrentParagraph();
  }

  /**
   * Returns the text for the paragraph that contains the caret.
   *
   * @return A non-null string, possibly empty.
   */
  private String getCaretParagraph() {
    return getText( getCurrentParagraph() );
  }

  @Override
  public String getText( final int paragraph ) {
    return mTextArea.getText( paragraph );
  }

  @Override
  public String getText( final IndexRange indexes )
    throws IndexOutOfBoundsException {
    return mTextArea.getText( indexes.getStart(), indexes.getEnd() );
  }

  @Override
  public void replaceText( final IndexRange indexes, final String s ) {
    mTextArea.replaceText( indexes, s );
  }

  private UndoManager<?> getUndoManager() {
    return mTextArea.getUndoManager();
  }

  /**
   * Returns the path to a {@link Locale}-specific stylesheet.
   *
   * @return A non-null string to inject into the HTML document head.
   */
  private static String getStylesheetPath( final Locale locale ) {
    return get(
      sSettings.getSetting( STYLESHEET_MARKDOWN_LOCALE, "" ),
      locale.getLanguage(),
      locale.getScript(),
      locale.getCountry()
    );
  }

  private Locale getLocale() {
    return localeProperty().toLocale();
  }

  private LocaleProperty localeProperty() {
    return mWorkspace.localeProperty( KEY_LANGUAGE_LOCALE );
  }

  /**
   * Sets the font family name and font size at the same time. When the
   * workspace is loaded, the default font values are changed, which results
   * in this method being called.
   *
   * @param area Change the font settings for this text area.
   * @param name New font family name to apply.
   * @param points New font size to apply (in points, not pixels).
   */
  private void setFont(
    final StyleClassedTextArea area, final String name, final double points ) {
    runLater( () -> area.setStyle(
      format(
        "-fx-font-family:'%s';-fx-font-size:%spx;", name, toPixels( points )
      )
    ) );
  }

  private String getFontName() {
    return fontNameProperty().get();
  }

  private StringProperty fontNameProperty() {
    return mWorkspace.stringProperty( KEY_UI_FONT_EDITOR_NAME );
  }

  private double getFontSize() {
    return fontSizeProperty().get();
  }

  private DoubleProperty fontSizeProperty() {
    return mWorkspace.doubleProperty( KEY_UI_FONT_EDITOR_SIZE );
  }
}
