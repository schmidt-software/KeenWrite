/*
 * Copyright 2016 Karl Tauber and White Magic Software, Ltd.
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
package com.scrivenvar;

import com.scrivenvar.editors.EditorPane;
import com.scrivenvar.editors.markdown.MarkdownEditorPane;
import com.scrivenvar.service.events.AlertMessage;
import com.scrivenvar.service.events.AlertService;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.util.Locale.ENGLISH;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.text.Text;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * Editor for a single file.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class FileEditorTab extends Tab {

  private final AlertService alertService = Services.load( AlertService.class );
  private EditorPane editorPane;

  /**
   * Character encoding used by the file (or default encoding if none found).
   */
  private Charset encoding;

  private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
  private final BooleanProperty canUndo = new SimpleBooleanProperty();
  private final BooleanProperty canRedo = new SimpleBooleanProperty();
  private Path path;

  FileEditorTab( final Path path ) {
    setPath( path );

    this.modified.addListener( (observable, oldPath, newPath) -> updateTab() );
    updateTab();

    setOnSelectionChanged( e -> {
      if( isSelected() ) {
        Platform.runLater( () -> activated() );
      }
    } );
  }

  private void updateTab() {
    setText( getTabTitle() );
    setGraphic( getModifiedMark() );
    setTooltip( getTabTooltip() );
  }

  /**
   * Returns the base filename (without the directory names).
   *
   * @return The untitled text if the path hasn't been set.
   */
  private String getTabTitle() {
    final Path filePath = getPath();

    return (filePath == null)
      ? Messages.get( "FileEditor.untitled" )
      : filePath.getFileName().toString();
  }

  /**
   * Returns the full filename represented by the path.
   *
   * @return The untitled text if the path hasn't been set.
   */
  private Tooltip getTabTooltip() {
    final Path filePath = getPath();

    return (filePath == null)
      ? null
      : new Tooltip( filePath.toString() );
  }

  /**
   * Returns a marker to indicate whether the file has been modified.
   *
   * @return "*" when the file has changed; otherwise null.
   */
  private Text getModifiedMark() {
    return isModified() ? new Text( "*" ) : null;
  }

  /**
   * Called when the user switches tab.
   */
  private void activated() {
    // Tab is closed or no longer active.
    if( getTabPane() == null || !isSelected() ) {
      return;
    }

    // Switch to the tab without loading if the contents are already in memory.
    if( getContent() != null ) {
      getEditorPane().requestFocus();
      return;
    }

    // Load the text and update the preview before the undo manager.
    load();

    // Track undo requests -- can only be called *after* load.
    initUndoManager();
    initLayout();
    initFocus();
  }

  private void initLayout() {
    setContent( getScrollPane() );
  }

  private Node getScrollPane() {
    return getEditorPane().getScrollPane();
  }

  private void initFocus() {
    getEditorPane().requestFocus();
  }

  private void initUndoManager() {
    final UndoManager undoManager = getUndoManager();

    // Clear undo history after first load.
    undoManager.forgetHistory();

    // Bind the editor undo manager to the properties.
    modified.bind( Bindings.not( undoManager.atMarkedPositionProperty() ) );
    canUndo.bind( undoManager.undoAvailableProperty() );
    canRedo.bind( undoManager.redoAvailableProperty() );
  }

  /**
   * Returns the index into the text where the caret blinks happily away.
   *
   * @return A number from 0 to the editor's document text length.
   */
  public int getCaretPosition() {
    return getEditor().getCaretPosition();
  }

  /**
   * Allows observers to synchronize caret position changes.
   *
   * @return An observable caret property value.
   */
  public final ObservableValue<Integer> caretPositionProperty() {
    return getEditor().caretPositionProperty();
  }

  /**
   * Returns the text area associated with this tab.
   *
   * @return A text editor.
   */
  private StyleClassedTextArea getEditor() {
    return getEditorPane().getEditor();
  }

  /**
   * Returns true if the given path exactly matches this tab's path.
   *
   * @param check The path to compare against.
   *
   * @return true The paths are the same.
   */
  public boolean isPath( final Path check ) {
    final Path filePath = getPath();

    return filePath == null ? false : filePath.equals( check );
  }

  /**
   * Reads the entire file contents from the path associated with this tab.
   */
  private void load() {
    final Path filePath = getPath();

    if( filePath != null ) {
      try {
        getEditorPane().setText( asString( Files.readAllBytes( filePath ) ) );
      } catch( Exception ex ) {
        alert(
          "FileEditor.loadFailed.title", "FileEditor.loadFailed.message", ex
        );
      }
    }
  }

  /**
   * Saves the entire file contents from the path associated with this tab.
   *
   * @return true The file has been saved.
   */
  public boolean save() {
    try {
      Files.write( getPath(), asBytes( getEditorPane().getText() ) );
      getEditorPane().getUndoManager().mark();
      return true;
    } catch( Exception ex ) {
      return alert(
        "FileEditor.saveFailed.title", "FileEditor.saveFailed.message", ex
      );
    }
  }

  /**
   * Creates an alert dialog and waits for it to close.
   *
   * @param titleKey Resource bundle key for the alert dialog title.
   * @param messageKey Resource bundle key for the alert dialog message.
   * @param e The unexpected happening.
   *
   * @return false
   */
  private boolean alert(
    final String titleKey, final String messageKey, final Exception e ) {
    final AlertService service = getAlertService();

    final AlertMessage message = service.createAlertMessage(
      Messages.get( titleKey ),
      Messages.get( messageKey ),
      getPath(),
      e.getMessage()
    );

    service.createAlertError( message ).showAndWait();
    return false;
  }

  /**
   * Returns a best guess at the file encoding. If the encoding could not be
   * detected, this will return the default charset for the JVM.
   *
   * @param bytes The bytes to perform character encoding detection.
   *
   * @return The character encoding.
   */
  private Charset detectEncoding( final byte[] bytes ) {
    final UniversalDetector detector = new UniversalDetector( null );
    detector.handleData( bytes, 0, bytes.length );
    detector.dataEnd();

    final String charset = detector.getDetectedCharset();
    final Charset charEncoding = charset == null
      ? Charset.defaultCharset()
      : Charset.forName( charset.toUpperCase( ENGLISH ) );

    detector.reset();

    return charEncoding;
  }

  /**
   * Converts the given string to an array of bytes using the encoding that was
   * originally detected (if any) and associated with this file.
   *
   * @param text The text to convert into the original file encoding.
   *
   * @return A series of bytes ready for writing to a file.
   */
  private byte[] asBytes( final String text ) {
    return text.getBytes( getEncoding() );
  }

  /**
   * Converts the given bytes into a Java String. This will call setEncoding
   * with the encoding detected by the CharsetDetector.
   *
   * @param text The text of unknown character encoding.
   *
   * @return The text, in its auto-detected encoding, as a String.
   */
  private String asString( final byte[] text ) {
    setEncoding( detectEncoding( text ) );
    return new String( text, getEncoding() );
  }

  public Path getPath() {
    return this.path;
  }

  void setPath( final Path path ) {
    this.path = path;
  }

  public boolean isModified() {
    return this.modified.get();
  }

  ReadOnlyBooleanProperty modifiedProperty() {
    return this.modified.getReadOnlyProperty();
  }

  BooleanProperty canUndoProperty() {
    return this.canUndo;
  }

  BooleanProperty canRedoProperty() {
    return this.canRedo;
  }

  private UndoManager getUndoManager() {
    return getEditorPane().getUndoManager();
  }

  /**
   * Forwards the request to the editor pane.
   *
   * @param <T> The type of event listener to add.
   * @param <U> The type of consumer to add.
   * @param event The event that should trigger updates to the listener.
   * @param consumer The listener to receive update events.
   */
  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    getEditorPane().addEventListener( event, consumer );
  }

  /**
   * Forwards to the editor pane's listeners for keyboard events.
   *
   * @param map The new input map to replace the existing keyboard listener.
   */
  public void addEventListener( final InputMap<InputEvent> map ) {
    getEditorPane().addEventListener( map );
  }

  /**
   * Forwards to the editor pane's listeners for keyboard events.
   *
   * @param map The existing input map to remove from the keyboard listeners.
   */
  public void removeEventListener( final InputMap<InputEvent> map ) {
    getEditorPane().removeEventListener( map );
  }

  /**
   * Forwards to the editor pane's listeners for text change events.
   *
   * @param listener The listener to notify when the text changes.
   */
  public void addTextChangeListener( final ChangeListener<String> listener ) {
    getEditorPane().addTextChangeListener( listener );
  }

  /**
   * Forwards to the editor pane's listeners for caret paragraph change events.
   *
   * @param listener The listener to notify when the caret changes paragraphs.
   */
  public void addCaretParagraphListener( final ChangeListener<Integer> listener ) {
    getEditorPane().addCaretParagraphListener( listener );
  }

  /**
   * Forwards the request to the editor pane.
   *
   * @return The text to process.
   */
  public String getEditorText() {
    return getEditorPane().getText();
  }

  /**
   * Returns the editor pane, or creates one if it doesn't yet exist.
   *
   * @return The editor pane, never null.
   */
  public EditorPane getEditorPane() {
    if( this.editorPane == null ) {
      this.editorPane = new MarkdownEditorPane();
    }

    return this.editorPane;
  }

  private AlertService getAlertService() {
    return this.alertService;
  }

  private Charset getEncoding() {
    return this.encoding;
  }

  private void setEncoding( final Charset encoding ) {
    this.encoding = encoding;
  }

  /**
   * Returns the tab title, without any modified indicators.
   *
   * @return The tab title.
   */
  @Override
  public String toString() {
    return getTabTitle();
  }
}
