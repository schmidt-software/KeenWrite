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

import com.scrivenvar.editor.EditorPane;
import com.scrivenvar.editor.MarkdownEditorPane;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.events.AlertMessage;
import com.scrivenvar.service.events.AlertService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.Alert;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputEvent;
import javafx.scene.text.Text;
import org.fxmisc.undo.UndoManager;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;

/**
 * Editor for a single file.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class FileEditorTab extends Tab {

  private final Options options = Services.load( Options.class );
  private final AlertService alertService = Services.load( AlertService.class );

  private EditorPane editorPane;
  private HTMLPreviewPane previewPane;

  private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
  private final BooleanProperty canUndo = new SimpleBooleanProperty();
  private final BooleanProperty canRedo = new SimpleBooleanProperty();
  private Path path;

  FileEditorTab( final Path path ) {
    setPath( path );
    setUserData( this );

    this.modified.addListener( (observable, oldPath, newPath) -> updateTab() );
    updateTab();

    setOnSelectionChanged( e -> {
      if( isSelected() ) {
        Platform.runLater( () -> activated() );
      }
    } );
  }

  private void updateTab() {
    final Path filePath = getPath();

    setText( getFilename( filePath ) );
    setGraphic( getModifiedMark() );
    setTooltip( getTooltip( filePath ) );
  }

  private String getFilename( final Path filePath ) {
    return (filePath == null)
      ? Messages.get( "FileEditor.untitled" )
      : filePath.getFileName().toString();
  }

  private Tooltip getTooltip( final Path filePath ) {
    return (filePath == null)
      ? null
      : new Tooltip( filePath.toString() );
  }

  private Text getModifiedMark() {
    return isModified() ? new Text( "*" ) : null;
  }

  /**
   * Called when the user switches tab.
   */
  private void activated() {
    if( getTabPane() == null || !isSelected() ) {
      // Tab is closed or no longer active.
      return;
    }

    if( getContent() != null ) {
      getEditorPane().requestFocus();
      return;
    }

    // Load the text and update the preview before the undo manager.
    load();

    // Track undo requests (must not be called before load).
    initUndoManager();
    initSplitPane();
  }

  public void initSplitPane() {
    final EditorPane editor = getEditorPane();
    final HTMLPreviewPane preview = getPreviewPane();

    // Make the preview pane scroll correspond to the editor pane scroll.
    preview.scrollYProperty().bind( editor.scrollYProperty() );

    // Separate the edit and preview panels.
    final SplitPane splitPane = new SplitPane(
      editor.getScrollPane(),
      preview.getWebView() );
    setContent( splitPane );

    // Set the caret position to 0.
    editor.scrollToTop();

    // Let the user edit.
    editor.requestFocus();
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
   * Delegates to add a listener for changes to the text area.
   *
   * @param listener The listener to receive editor change events.
   */
  public void addChangeListener( ChangeListener<? super String> listener ) {
    getEditorPane().addChangeListener( listener );
  }

  void load() {
    final Path filePath = getPath();

    if( filePath != null ) {
      try {
        final byte[] bytes = Files.readAllBytes( filePath );
        String markdown;

        try {
          markdown = new String( bytes, getOptions().getEncoding() );
        } catch( Exception e ) {
          // Unsupported encodings and null pointers fallback here.
          markdown = new String( bytes );
        }

        getEditorPane().setText( markdown );
      } catch( IOException ex ) {
        final AlertMessage message = getAlertService().createAlertMessage(
          Messages.get( "FileEditor.loadFailed.title" ),
          Messages.get( "FileEditor.loadFailed.message" ),
          filePath,
          ex.getMessage()
        );

        final Alert alert = getAlertService().createAlertError( message );

        alert.showAndWait();
      }
    }
  }

  boolean save() {
    final String text = getEditorPane().getText();

    byte[] bytes;

    try {
      bytes = text.getBytes( getOptions().getEncoding() );
    } catch( Exception ex ) {
      bytes = text.getBytes();
    }

    try {
      Files.write( getPath(), bytes );
      getEditorPane().getUndoManager().mark();
      return true;
    } catch( IOException ex ) {
      final AlertService service = getAlertService();
      final AlertMessage message = service.createAlertMessage(
        Messages.get( "FileEditor.saveFailed.title" ),
        Messages.get( "FileEditor.saveFailed.message" ),
        getPath(),
        ex.getMessage()
      );

      final Alert alert = service.createAlertError( message );

      alert.showAndWait();
      return false;
    }
  }

  Path getPath() {
    return this.path;
  }

  void setPath( final Path path ) {
    this.path = path;
  }

  boolean isModified() {
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

  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    getEditorPane().addEventListener( event, consumer );
  }

  public void addEventListener( final InputMap<InputEvent> map ) {
    getEditorPane().addEventListener( map );
  }

  public void removeEventListener( final InputMap<InputEvent> map ) {
    getEditorPane().removeEventListener( map );
  }

  protected EditorPane getEditorPane() {
    if( this.editorPane == null ) {
      this.editorPane = new MarkdownEditorPane();
    }

    return this.editorPane;
  }

  private AlertService getAlertService() {
    return this.alertService;
  }

  private Options getOptions() {
    return this.options;
  }

  public HTMLPreviewPane getPreviewPane() {
    if( this.previewPane == null ) {
      this.previewPane = new HTMLPreviewPane( getPath() );
    }

    return this.previewPane;
  }
}
