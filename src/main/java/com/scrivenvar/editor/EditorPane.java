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

import com.scrivenvar.ui.AbstractPane;
import java.nio.file.Path;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ScrollPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;

/**
 * Represents common editing features for various types of text editors.
 *
 * @author White Magic Software, Ltd.
 */
public class EditorPane extends AbstractPane {

  private StyleClassedTextArea editor;
  private VirtualizedScrollPane<StyleClassedTextArea> scrollPane;
  private final ReadOnlyDoubleWrapper scrollY = new ReadOnlyDoubleWrapper();
  private final ObjectProperty<Path> path = new SimpleObjectProperty<>();

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

  public UndoManager getUndoManager() {
    return getEditor().getUndoManager();
  }

  public void scrollToTop() {
    getEditor().moveTo( 0 );
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

  /**
   * Returns the scroll pane that contains the text area.
   *
   * @return The scroll pane that contains the content to edit.
   */
  public synchronized VirtualizedScrollPane getScrollPane() {
    if( this.scrollPane == null ) {
      this.scrollPane = createScrollPane();
    }

    return this.scrollPane;
  }

  protected VirtualizedScrollPane<StyleClassedTextArea> createScrollPane() {
    final VirtualizedScrollPane<StyleClassedTextArea> pane = new VirtualizedScrollPane<>( getEditor() );

    pane.setVbarPolicy( ScrollPane.ScrollBarPolicy.ALWAYS );
    return pane;
  }

  protected StyleClassedTextArea createTextArea() {
    return new StyleClassedTextArea( false );
  }

  public double getScrollY() {
    return this.scrollY.get();
  }

  protected void setScrollY( double scrolled ) {
    this.scrollY.set( scrolled );
  }

  public ReadOnlyDoubleProperty scrollYProperty() {
    return this.scrollY.getReadOnlyProperty();
  }

  public Path getPath() {
    return this.path.get();
  }

  public void setPath( final Path path ) {
    this.path.set( path );
  }

  public ObjectProperty<Path> pathProperty() {
    return this.path;
  }
}
