/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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
package com.scrivenvar;

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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
public final class FileEditor {

  private final Options options = Services.load( Options.class );
  private final AlertService alertService = Services.load( AlertService.class );

  private Tab tab;
  private MarkdownEditorPane markdownEditorPane;
  private HTMLPreviewPane htmlPreviewPane;

  private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
  private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
  private final BooleanProperty canUndo = new SimpleBooleanProperty();
  private final BooleanProperty canRedo = new SimpleBooleanProperty();

  FileEditor( final Path path ) {
    setPath( path );

    Tab activeTab = getTab();

    // avoid that this is GCed
    activeTab.setUserData( this );

    pathProperty().addListener( (observable, oldPath, newPath) -> updateTab() );
    this.modified.addListener( (observable, oldPath, newPath) -> updateTab() );
    updateTab();

    activeTab.setOnSelectionChanged( e -> {
      if( activeTab.isSelected() ) {
        Platform.runLater( () -> activated() );
      }
    } );
  }

  private void updateTab() {
    final Tab activeTab = getTab();
    final Path filePath = getPath();
    activeTab.setText( (filePath != null) ? filePath.getFileName().toString() : Messages.get( "FileEditor.untitled" ) );
    activeTab.setTooltip( (filePath != null) ? new Tooltip( filePath.toString() ) : null );
    activeTab.setGraphic( isModified() ? new Text( "*" ) : null );
  }

  private void activated() {
    final Tab activeTab = getTab();

    if( activeTab.getTabPane() == null || !activeTab.isSelected() ) {
      // Tab is closed or no longer active.
      return;
    }

    final MarkdownEditorPane editorPane = getEditorPane();
    editorPane.pathProperty().bind( pathProperty() );


    if( activeTab.getContent() != null ) {
      editorPane.requestFocus();
      return;
    }

    // Load the text and update the preview before the undo manager.
    load();

    // Track undo requests (must not be called before load).
    initUndoManager();
    initSplitPane();
  }
  
  public void initSplitPane() {
    final MarkdownEditorPane editorPane = getEditorPane();
    final HTMLPreviewPane previewPane = getPreviewPane();

    // Make the preview pane scroll correspond to the editor pane scroll.
    previewPane.scrollYProperty().bind( editorPane.scrollYProperty() );
    
    // Separate the edit and preview panels.
    SplitPane splitPane = new SplitPane(
      editorPane.getScrollPane(),
      previewPane.getWebView() );
    getTab().setContent( splitPane );

    // Set the caret position to 0.
    editorPane.scrollToTop();

    // Let the user edit.
    editorPane.requestFocus();
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

        getEditorPane().setMarkdown( markdown );
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
    final String markdown = getEditorPane().getMarkdown();

    byte[] bytes;

    try {
      bytes = markdown.getBytes( getOptions().getEncoding() );
    } catch( Exception ex ) {
      bytes = markdown.getBytes();
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

  public synchronized Tab getTab() {
    if( this.tab == null ) {
      this.tab = new Tab();
    }

    return this.tab;
  }

  Path getPath() {
    return this.path.get();
  }

  void setPath( Path path ) {
    this.path.set( path );
  }

  ObjectProperty<Path> pathProperty() {
    return this.path;
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

  protected MarkdownEditorPane getEditorPane() {
    if( this.markdownEditorPane == null ) {
      this.markdownEditorPane = new MarkdownEditorPane();
    }

    return this.markdownEditorPane;
  }

  private AlertService getAlertService() {
    return this.alertService;
  }

  private Options getOptions() {
    return this.options;
  }

  public HTMLPreviewPane getPreviewPane() {
    if( this.htmlPreviewPane == null ) {
      this.htmlPreviewPane = new HTMLPreviewPane( pathProperty() );
    }

    return this.htmlPreviewPane;
  }
}
