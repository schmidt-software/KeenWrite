/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite.editors.base;

import com.keenwrite.Constants;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.io.File;
import com.keenwrite.preferences.UserPreferences;
import com.keenwrite.processors.markdown.Caret;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.Nodes;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.keenwrite.StatusBarNotifier.clearClue;
import static java.lang.String.format;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Represents common editing features for various types of text editors.
 */
public class PlainTextEditor extends StyleClassedTextArea
    implements TextEditor {

  /**
   * Used when changing the text area font size.
   */
  private static final String FMT_CSS_FONT_SIZE = "-fx-font-size: %dpt;";

  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
      new VirtualizedScrollPane<>( this );
  private final ObjectProperty<Path> mPath = new SimpleObjectProperty<>();

  /**
   * Opened file's character encoding, or {@link Constants#DEFAULT_CHARSET} if
   * either no encoding could be determined or this is a new (empty) file.
   */
  private final Charset mEncoding;

  private final BooleanProperty mModified = new SimpleBooleanProperty();

  public PlainTextEditor( final File file ) {
    super( false );
    getScrollPane().setVbarPolicy( ScrollPane.ScrollBarPolicy.ALWAYS );
    fontsSizeProperty().addListener(
        ( l, o, n ) -> setFontSize( n.intValue() )
    );

    // Clear out any previous alerts after the user has typed. If the problem
    // persists, re-rendering the document will re-raise the error. If there
    // was no previous error, clearing the alert is essentially a no-op.
    textProperty().addListener(
        ( l, o, n ) -> clearClue()
    );

    mPath.set( file.toPath() );
    mEncoding = open( file );
  }

  @Override
  public void undo() {
    getUndoManager().undo();
  }

  @Override
  public void redo() {
    getUndoManager().redo();
  }

  /**
   * Cuts the actively selected text; if no text is selected, this will cut
   * the entire paragraph.
   */
  @Override
  public void cut() {
    final var editor = getEditor();
    final var selected = editor.getSelectedText();

    if( selected == null || selected.isEmpty() ) {
      editor.selectParagraph();
    }

    editor.cut();
  }

  @Override
  public void copy() {
    getEditor().copy();
  }

  @Override
  public void paste() {
    getEditor().paste();
  }

  @Override
  public void selectAll() {
    getEditor().selectAll();
  }

  @Override
  public void setText( final String text ) {
    final var editor = getEditor();
    editor.deselect();
    editor.replaceText( text );
    getUndoManager().mark();
  }

  @Override
  public String getText() {
    return getEditor().getText();
  }

  @Override
  public Charset getEncoding() {
    return mEncoding;
  }

  @Override
  public void rename( final File file ) {
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
    mModified.setValue( false );
  }

  public UndoManager<?> getUndoManager() {
    return getEditor().getUndoManager();
  }

  /**
   * Call to hook into changes to the text area.
   *
   * @param listener Receives editor text change events.
   */
  public void addTextChangeListener(
      final ChangeListener<? super String> listener ) {
    getEditor().textProperty().addListener( listener );
  }

  /**
   * Notifies observers when the caret changes position.
   *
   * @param listener Receives change event.
   */
  public void addCaretPositionListener(
      final ChangeListener<? super Integer> listener ) {
    getEditor().caretPositionProperty().addListener( listener );
  }

  /**
   * This method adds listeners to editor events.
   *
   * @param <T>      The event type.
   * @param <U>      The consumer type for the given event type.
   * @param event    The event of interest.
   * @param consumer The method to call when the event happens.
   */
  public <T extends Event, U extends T> void addKeyboardListener(
      final EventPattern<? super T, ? extends U> event,
      final Consumer<? super U> consumer ) {
    Nodes.addInputMap( getEditor(), consume( event, consumer ) );
  }

  /**
   * Repositions the cursor and scroll bar to the top of the file.
   */
  public void scrollToTop() {
    getEditor().moveTo( 0 );
    getScrollPane().scrollYToPixel( 0 );
  }

  public StyleClassedTextArea getEditor() {
    return this;
  }

  /**
   * Returns the scroll pane that contains the text area.
   *
   * @return The scroll pane that contains the content to edit.
   */
  @Override
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return mScrollPane;
  }

  @Override
  public Caret getCaret() {
    return Caret
        .builder()
        .with( Caret.Mutator::setEditor, this )
        .build();
  }

  @Override
  public void moveTo( final int offset ) {
    super.moveTo( offset );
  }

  /**
   * TODO: Merge functionality from {@link MarkdownEditor}.
   *
   * NOTE: This method is not implemented, so do not use.
   *
   * @return An invalid range of 0, 0.
   */
  @Override
  public IndexRange getCaretWord() {
    return new IndexRange( 0, 0 );
  }

  @Override
  public void replaceText( IndexRange indexes, String s ) {
    replaceText( indexes.getStart(), indexes.getEnd(), s );
  }

  public File getFile() {
    return new File( mPath.get().toFile() );
  }

  /**
   * Sets the font size in points.
   *
   * @param size The new font size to use for the text editor.
   */
  private void setFontSize( final int size ) {
    setStyle( format( FMT_CSS_FONT_SIZE, size ) );
  }

  /**
   * Returns the text editor font size property for handling font size change
   * events.
   */
  private IntegerProperty fontsSizeProperty() {
    return UserPreferences.getInstance().fontsSizeEditorProperty();
  }
}
