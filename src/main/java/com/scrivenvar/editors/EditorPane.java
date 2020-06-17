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
package com.scrivenvar.editors;

import com.scrivenvar.AbstractPane;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.ScrollPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.Nodes;

import java.nio.file.Path;
import java.util.function.Consumer;

import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Represents common editing features for various types of text editors.
 *
 * @author White Magic Software, Ltd.
 */
public class EditorPane extends AbstractPane {

  private final StyleClassedTextArea mEditor =
      new StyleClassedTextArea( false );
  private final VirtualizedScrollPane<StyleClassedTextArea> mScrollPane =
      new VirtualizedScrollPane<>( mEditor );
  private final ObjectProperty<Path> mPath = new SimpleObjectProperty<>();

  public EditorPane() {
    getScrollPane().setVbarPolicy( ScrollPane.ScrollBarPolicy.ALWAYS );
  }

  @Override
  public void requestFocus() {
    Platform.runLater( () -> getEditor().requestFocus() );
  }

  public void undo() {
    getUndoManager().undo();
  }

  public void redo() {
    getUndoManager().redo();
  }

  public UndoManager<?> getUndoManager() {
    return getEditor().getUndoManager();
  }

  public String getText() {
    return getEditor().getText();
  }

  public void setText( final String text ) {
    final var editor = getEditor();
    editor.deselect();
    editor.replaceText( text );
    getUndoManager().mark();
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
   * Call to listen for when the caret moves to another paragraph.
   *
   * @param listener Receives paragraph change events.
   */
  public void addCaretParagraphListener(
      final ChangeListener<? super Integer> listener ) {
    getEditor().currentParagraphProperty().addListener( listener );
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
    return mEditor;
  }

  /**
   * Returns the scroll pane that contains the text area.
   *
   * @return The scroll pane that contains the content to edit.
   */
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return mScrollPane;
  }

  public Path getPath() {
    return mPath.get();
  }

  public void setPath( final Path path ) {
    mPath.set( path );
  }
}
