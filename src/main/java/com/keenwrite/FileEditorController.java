/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.editors.markdown.MarkdownEditorPane;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.markdown.CaretPosition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Text;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.undo.UndoManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Editor for a single file.
 */
public final class FileEditorController {

  private final MarkdownEditorPane mEditorPane = new MarkdownEditorPane();

  private final ReadOnlyBooleanWrapper mModified = new ReadOnlyBooleanWrapper();
  private final BooleanProperty canUndo = new SimpleBooleanProperty();
  private final BooleanProperty canRedo = new SimpleBooleanProperty();

  /**
   * Character encoding used by the file (or default encoding if none found).
   */
  private Charset mEncoding = UTF_8;

  /**
   * File to load into the editor.
   */
  private Path mPath;

  /**
   * Dynamically updated position of the caret within the text editor.
   */
  private final CaretPosition mCaretPosition;

  public FileEditorController() {
    //getChildren().add( mEditorPane.getScrollPane() );

    mModified.addListener( ( observable, oldPath, newPath ) -> updateTab() );

//    setOnSelectionChanged( e -> {
//      if( isSelected() ) {
//        runLater( this::activated );
//        requestFocus();
//      }
//    } );

    mCaretPosition = createCaretPosition( getEditor() );
  }

  private CaretPosition createCaretPosition(
      final StyleClassedTextArea editor ) {
    return CaretPosition
        .builder().with( CaretPosition.Mutator::setEditor, editor ).build();
  }

  private void updateTab() {
//    setText( getTabTitle() );
//    setGraphic( getModifiedMark() );
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
//    if( getTabPane() == null || !isSelected() ) {
//      return;
//    }

    // If the tab is devoid of content, load it.
//    if( getContent() == null ) {
//      readFile();
//      initLayout();
//      initUndoManager();
//    }
  }


  /**
   * Tracks undo requests, but can only be called <em>after</em> load.
   */
  private void initUndoManager() {
    final UndoManager<?> undoManager = getUndoManager();
    undoManager.forgetHistory();

    // Bind the editor undo manager to the properties.
    mModified.bind( Bindings.not( undoManager.atMarkedPositionProperty() ) );
    canUndo.bind( undoManager.undoAvailableProperty() );
    canRedo.bind( undoManager.redoAvailableProperty() );
  }

  public void requestFocus() {
    getEditorPane().requestFocus();
  }

  /**
   * Searches from the caret position forward for the given string.
   *
   * @param needle The text string to match.
   */
  public void searchNext( final String needle ) {
    final String haystack = getEditorText();
    int index = haystack.indexOf( needle, getCaretTextOffset() );

    // Wrap around.
    if( index == -1 ) {
      index = haystack.indexOf( needle );
    }

    if( index >= 0 ) {
      setCaretTextOffset( index );
      getEditor().selectRange( index, index + needle.length() );
    }
  }

  /**
   * Gets a reference to the scroll pane that houses the editor.
   *
   * @return The editor's scroll pane, containing a vertical scrollbar.
   */
  public VirtualizedScrollPane<StyleClassedTextArea> getScrollPane() {
    return getEditorPane().getScrollPane();
  }

  /**
   * Returns an instance of {@link CaretPosition} that contains information
   * about the caret, including the offset into the text, the paragraph into
   * the text, maximum number of paragraphs, and more. This allows the main
   * application and the {@link Processor} instances to get the current
   * caret position.
   *
   * @return The current values for the caret's position within the editor.
   */
  public CaretPosition getCaretPosition() {
    return mCaretPosition;
  }

  /**
   * Returns the index into the text where the caret blinks happily away.
   *
   * @return A number from 0 to the editor's document text length.
   */
  private int getCaretTextOffset() {
    return getEditor().getCaretPosition();
  }

  /**
   * Moves the caret to a given offset.
   *
   * @param offset The new caret offset.
   */
  private void setCaretTextOffset( final int offset ) {
    getEditor().moveTo( offset );
    getEditor().requestFollowCaret();
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
   * @return true The paths are the same.
   */
  public boolean isPath( final Path check ) {
    final Path filePath = getPath();

    return filePath != null && filePath.equals( check );
  }

  /**
   * Returns the path to the file being edited in this tab.
   *
   * @return A non-null instance.
   */
  public Path getPath() {
    return mPath;
  }

  /**
   * Sets the path to a file for editing and then updates the tab with the
   * file contents.
   *
   * @param path A non-null instance.
   */
  public void setPath( final Path path ) {
    assert path != null;
    mPath = path;

    updateTab();
  }

  /**
   * Convenience method to set the path based on an instance of {@link File}.
   *
   * @param file A non-null instance.
   */
  public void setPath( final File file ) {
    assert file != null;
    setPath( file.toPath() );
  }

  public boolean isModified() {
    return mModified.get();
  }

  ReadOnlyBooleanProperty modifiedProperty() {
    return mModified.getReadOnlyProperty();
  }

  BooleanProperty canUndoProperty() {
    return this.canUndo;
  }

  BooleanProperty canRedoProperty() {
    return this.canRedo;
  }

  private UndoManager<?> getUndoManager() {
    return getEditorPane().getUndoManager();
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
   * Forwards to the editor pane's listeners for caret change events.
   *
   * @param listener Notified when the caret position changes.
   */
  public void addCaretPositionListener(
      final ChangeListener<? super Integer> listener ) {
    getEditorPane().addCaretPositionListener( listener );
  }

//  public <T extends Event> void addEventFilter(
//      final EventType<T> eventType,
//      final EventHandler<? super T> eventFilter ) {
//    getEditor().addEventFilter( eventType, eventFilter );
//  }

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
  @NotNull
  public MarkdownEditorPane getEditorPane() {
    return mEditorPane;
  }

  /**
   * Returns the encoding for the file, defaulting to UTF-8 if it hasn't been
   * determined.
   *
   * @return The file encoding or UTF-8 if unknown.
   */
  private Charset getEncoding() {
    return mEncoding;
  }

  private void setEncoding( final Charset encoding ) {
    assert encoding != null;
    mEncoding = encoding;
  }
}
