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
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.InputEvent;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import org.fxmisc.wellbehaved.event.Nodes;

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

  private String lineSeparator = getLineSeparator();

  /**
   * Set when entering variable edit mode; retrieved upon exiting.
   */
  private InputMap<InputEvent> nodeMap;

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

  public String getText() {
    String text = getEditor().getText();

    if( !this.lineSeparator.equals( "\n" ) ) {
      text = text.replace( "\n", this.lineSeparator );
    }

    return text;
  }

  public void setText( final String text ) {
    this.lineSeparator = determineLineSeparator( text );
    getEditor().deselect();
    getEditor().replaceText( text );
    getUndoManager().mark();
  }

  /**
   * Call to hook into changes to the text area.
   *
   * @param listener Receives editor text change events.
   */
  public void addChangeListener( final ChangeListener<? super String> listener ) {
    getEditor().textProperty().addListener( listener );
  }

  /**
   * Call to listen for when the caret moves to another paragraph.
   * 
   * @param listener Receives paragraph change events.
   */
  public void addCaretParagraphListener( final ChangeListener<? super Integer> listener ) {
    getEditor().currentParagraphProperty().addListener( listener );
  }

  /**
   * This method adds listeners to editor events.
   *
   * @param <T> The event type.
   * @param <U> The consumer type for the given event type.
   * @param event The event of interest.
   * @param consumer The method to call when the event happens.
   */
  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    Nodes.addInputMap( getEditor(), consume( event, consumer ) );
  }

  /**
   * This method adds listeners to editor events that can be removed without
   * affecting the original listeners (i.e., the original lister is restored on
   * a call to removeEventListener).
   *
   * @param map The map of methods to events.
   */
  @SuppressWarnings( "unchecked" )
  public void addEventListener( final InputMap<InputEvent> map ) {
    this.nodeMap = (InputMap<InputEvent>)getInputMap();
    Nodes.addInputMap( getEditor(), map );
  }

  /**
   * This method removes listeners to editor events and restores the default
   * handler.
   *
   * @param map The map of methods to events.
   */
  public void removeEventListener( final InputMap<InputEvent> map ) {
    Nodes.removeInputMap( getEditor(), map );
    Nodes.addInputMap( getEditor(), this.nodeMap );
  }

  /**
   * Returns the value for "org.fxmisc.wellbehaved.event.inputmap".
   *
   * @return An input map of input events.
   */
  private Object getInputMap() {
    return getEditor().getProperties().get( getInputMapKey() );
  }

  /**
   * Returns the hashmap key entry for the input map.
   *
   * @return "org.fxmisc.wellbehaved.event.inputmap"
   */
  private String getInputMapKey() {
    return "org.fxmisc.wellbehaved.event.inputmap";
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
  public synchronized VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
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

  private String getLineSeparator() {
    final String separator = getOptions().getLineSeparator();

    return (separator != null)
      ? separator
      : System.lineSeparator();
  }

  private String determineLineSeparator( final String s ) {
    final int length = s.length();

    // TODO: Looping backwards will probably detect a newline sooner.
    for( int i = 0; i < length; i++ ) {
      char ch = s.charAt( i );
      if( ch == '\n' ) {
        return (i > 0 && s.charAt( i - 1 ) == '\r') ? "\r\n" : "\n";
      }
    }

    return getLineSeparator();
  }
}
