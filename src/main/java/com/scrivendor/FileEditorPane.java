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
package com.scrivendor;

import com.scrivendor.service.Settings;
import com.scrivendor.service.events.AlertMessage;
import com.scrivendor.service.events.AlertService;
import com.scrivendor.ui.AbstractPane;
import com.scrivendor.util.Utils;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.input.InputEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;

/**
 * Tab pane for file editors.
 *
 * @author Karl Tauber
 */
public class FileEditorPane extends AbstractPane {

  private final static List<String> DEFAULT_EXTENSIONS_MARKDOWN = Arrays.asList(
    "*.md", "*.markdown", "*.txt" );

  private final static List<String> DEFAULT_EXTENSIONS_ALL = Arrays.asList(
    "*.*" );

  private final static List<String> DEFAULT_EXTENSIONS_DEFINITION = Arrays.asList(
    "*.yml", "*.yaml", "*.properties", "*.props" );

  private final Settings settings = Services.load( Settings.class );
  private final AlertService alertService = Services.load( AlertService.class );

  private MainWindow mainWindow;
  private final TabPane tabPane;
  private final ReadOnlyObjectWrapper<FileEditor> activeFileEditor = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyBooleanWrapper anyFileEditorModified = new ReadOnlyBooleanWrapper();

  FileEditorPane( MainWindow mainWindow ) {
    setMainWindow( mainWindow );

    tabPane = new TabPane();
    tabPane.setFocusTraversable( false );
    tabPane.setTabClosingPolicy( TabClosingPolicy.ALL_TABS );

    // update activeFileEditor property
    tabPane.getSelectionModel().selectedItemProperty().addListener( (observable, oldTab, newTab) -> {
      this.activeFileEditor.set( (newTab != null) ? (FileEditor)newTab.getUserData() : null );
    } );

    // update anyFileEditorModified property
    ChangeListener<Boolean> modifiedListener = (observable, oldValue, newValue) -> {
      boolean modified = false;
      for( Tab tab : tabPane.getTabs() ) {
        if( ((FileEditor)tab.getUserData()).isModified() ) {
          modified = true;
          break;
        }
      }
      this.anyFileEditorModified.set( modified );
    };

    tabPane.getTabs().addListener( (ListChangeListener<Tab>)c -> {
      while( c.next() ) {
        if( c.wasAdded() ) {
          for( Tab tab : c.getAddedSubList() ) {
            ((FileEditor)tab.getUserData()).modifiedProperty().addListener( modifiedListener );
          }
        } else if( c.wasRemoved() ) {
          for( Tab tab : c.getRemoved() ) {
            ((FileEditor)tab.getUserData()).modifiedProperty().removeListener( modifiedListener );
          }
        }
      }

      // changes in the tabs may also change anyFileEditorModified property
      // (e.g. closed modified file)
      modifiedListener.changed( null, null, null );
    } );

    // re-open files
    restoreState();
  }

  public <T extends Event, U extends T> void addEventListener(
    final EventPattern<? super T, ? extends U> event,
    final Consumer<? super U> consumer ) {
    getActiveFileEditor().addEventListener( event, consumer );
  }

  /**
   * Delegates to the active file editor pane, and, ultimately, to its text
   * area.
   *
   * @param map The map of methods to events.
   */
  public void addEventListener( final InputMap<InputEvent> map ) {
    getActiveFileEditor().addEventListener( map );
  }
  
  public void removeEventListener( final InputMap<InputEvent> map ) {
    getActiveFileEditor().removeEventListener( map );
  }

  private MainWindow getMainWindow() {
    return this.mainWindow;
  }

  private void setMainWindow( MainWindow mainWindow ) {
    this.mainWindow = mainWindow;
  }

  Node getNode() {
    return this.tabPane;
  }

  /**
   * Allows clients to manipulate the editor content directly.
   *
   * @return The text area for the active file editor.
   */
  public StyledTextArea getEditor() {
    return getActiveFileEditor().getEditorPane().getEditor();
  }

  FileEditor getActiveFileEditor() {
    return this.activeFileEditor.get();
  }

  ReadOnlyObjectProperty<FileEditor> activeFileEditorProperty() {
    return this.activeFileEditor.getReadOnlyProperty();
  }

  ReadOnlyBooleanProperty anyFileEditorModifiedProperty() {
    return this.anyFileEditorModified.getReadOnlyProperty();
  }

  private FileEditor createFileEditor( Path path ) {
    FileEditor fileEditor = new FileEditor( path );
    fileEditor.getTab().setOnCloseRequest( e -> {
      if( !canCloseEditor( fileEditor ) ) {
        e.consume();
      }
    } );
    return fileEditor;
  }

  FileEditor newEditor() {
    FileEditor fileEditor = createFileEditor( null );
    Tab tab = fileEditor.getTab();
    tabPane.getTabs().add( tab );
    tabPane.getSelectionModel().select( tab );
    return fileEditor;
  }

  FileEditor[] openEditor() {
    FileChooser fileChooser = createFileChooser( Messages.get( "Dialog.file.choose.open.title" ) );
    List<File> selectedFiles = fileChooser.showOpenMultipleDialog( getMainWindow().getScene().getWindow() );

    if( selectedFiles == null ) {
      return null;
    }

    saveLastDirectory( selectedFiles.get( 0 ) );
    return openEditors( selectedFiles, 0 );
  }

  FileEditor[] openEditors( List<File> files, int activeIndex ) {
    // close single unmodified "Untitled" tab
    if( tabPane.getTabs().size() == 1 ) {
      FileEditor fileEditor = (FileEditor)tabPane.getTabs().get( 0 ).getUserData();
      if( fileEditor.getPath() == null && !fileEditor.isModified() ) {
        closeEditor( fileEditor, false );
      }
    }

    FileEditor[] fileEditors = new FileEditor[ files.size() ];
    for( int i = 0; i < files.size(); i++ ) {
      Path path = files.get( i ).toPath();

      // check whether file is already opened
      FileEditor fileEditor = findEditor( path );
      if( fileEditor == null ) {
        fileEditor = createFileEditor( path );

        tabPane.getTabs().add( fileEditor.getTab() );
      }

      // select first file
      if( i == activeIndex ) {
        tabPane.getSelectionModel().select( fileEditor.getTab() );
      }

      fileEditors[ i ] = fileEditor;
    }
    return fileEditors;
  }

  boolean saveEditor( FileEditor fileEditor ) {
    if( fileEditor == null || !fileEditor.isModified() ) {
      return true;
    }

    if( fileEditor.getPath() == null ) {
      tabPane.getSelectionModel().select( fileEditor.getTab() );

      FileChooser fileChooser = createFileChooser( Messages.get( "Dialog.file.choose.save.title" ) );
      File file = fileChooser.showSaveDialog( getMainWindow().getScene().getWindow() );
      if( file == null ) {
        return false;
      }

      saveLastDirectory( file );
      fileEditor.setPath( file.toPath() );
    }

    return fileEditor.save();
  }

  boolean saveAllEditors() {
    FileEditor[] allEditors = getAllEditors();

    boolean success = true;
    for( FileEditor fileEditor : allEditors ) {
      if( !saveEditor( fileEditor ) ) {
        success = false;
      }
    }

    return success;
  }

  boolean canCloseEditor( final FileEditor fileEditor ) {
    if( !fileEditor.isModified() ) {
      return true;
    }

    final AlertMessage message = getAlertService().createAlertMessage(
      Messages.get( "Alert.file.close.title" ),
      Messages.get( "Alert.file.close.text" ),
      fileEditor.getTab().getText()
    );

    final Alert alert = getAlertService().createAlertConfirmation( message );
    final ButtonType result = alert.showAndWait().get();

    if( result != ButtonType.YES ) {
      return (result == ButtonType.NO);
    }

    return saveEditor( fileEditor );
  }

  private AlertService getAlertService() {
    return this.alertService;
  }

  boolean closeEditor( FileEditor fileEditor, boolean save ) {
    if( fileEditor == null ) {
      return true;
    }

    Tab tab = fileEditor.getTab();

    if( save ) {
      Event event = new Event( tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT );
      Event.fireEvent( tab, event );
      if( event.isConsumed() ) {
        return false;
      }
    }

    tabPane.getTabs().remove( tab );
    if( tab.getOnClosed() != null ) {
      Event.fireEvent( tab, new Event( Tab.CLOSED_EVENT ) );
    }

    return true;
  }

  boolean closeAllEditors() {
    FileEditor[] allEditors = getAllEditors();
    FileEditor activeEditor = activeFileEditor.get();

    // try to save active tab first because in case the user decides to cancel,
    // then it stays active
    if( activeEditor != null && !canCloseEditor( activeEditor ) ) {
      return false;
    }

    // save modified tabs
    for( int i = 0; i < allEditors.length; i++ ) {
      FileEditor fileEditor = allEditors[ i ];
      if( fileEditor == activeEditor ) {
        continue;
      }

      if( fileEditor.isModified() ) {
        // activate the modified tab to make its modified content visible to the user
        tabPane.getSelectionModel().select( i );

        if( !canCloseEditor( fileEditor ) ) {
          return false;
        }
      }
    }

    // close all tabs
    for( FileEditor fileEditor : allEditors ) {
      if( !closeEditor( fileEditor, false ) ) {
        return false;
      }
    }

    saveState( allEditors, activeEditor );

    return tabPane.getTabs().isEmpty();
  }

  private FileEditor[] getAllEditors() {
    ObservableList<Tab> tabs = tabPane.getTabs();
    FileEditor[] allEditors = new FileEditor[ tabs.size() ];
    for( int i = 0; i < tabs.size(); i++ ) {
      allEditors[ i ] = (FileEditor)tabs.get( i ).getUserData();
    }
    return allEditors;
  }

  private FileEditor findEditor( Path path ) {
    for( final Tab tab : tabPane.getTabs() ) {
      FileEditor fileEditor = (FileEditor)tab.getUserData();

      if( path.equals( fileEditor.getPath() ) ) {
        return fileEditor;
      }
    }

    return null;
  }

  private FileChooser createFileChooser( String title ) {
    final FileChooser fileChooser = new FileChooser();

    fileChooser.setTitle( title );
    fileChooser.getExtensionFilters().addAll(
      new ExtensionFilter( Messages.get( "Dialog.file.choose.filter.title.markdown" ), getMarkdownExtensions() ),
      new ExtensionFilter( Messages.get( "Dialog.file.choose.filter.title.definition" ), getDefinitionExtensions() ),
      new ExtensionFilter( Messages.get( "Dialog.file.choose.filter.title.all" ), getAllExtensions() ) );

    final String lastDirectory = getState().get( "lastDirectory", null );
    File file = new File( (lastDirectory != null) ? lastDirectory : "." );

    if( !file.isDirectory() ) {
      file = new File( "." );
    }

    fileChooser.setInitialDirectory( file );
    return fileChooser;
  }

  private Settings getSettings() {
    return this.settings;
  }

  private List<String> getMarkdownExtensions() {
    return getStringSettingList( "Dialog.file.choose.filter.ext.markdown", DEFAULT_EXTENSIONS_MARKDOWN );
  }

  private List<String> getDefinitionExtensions() {
    return getStringSettingList( "Dialog.file.choose.filter.ext.definition", DEFAULT_EXTENSIONS_DEFINITION );
  }

  private List<String> getAllExtensions() {
    return getStringSettingList( "Dialog.file.choose.filter.ext.all", DEFAULT_EXTENSIONS_ALL );
  }

  private List<String> getStringSettingList( String key, List<String> values ) {
    return getSettings().getStringSettingList( key, values );
  }

  private void saveLastDirectory( File file ) {
    getState().put( "lastDirectory", file.getParent() );
  }

  private void restoreState() {
    Preferences state = getState();
    String[] fileNames = Utils.getPrefsStrings( state, "file" );
    String activeFileName = state.get( "activeFile", null );

    int activeIndex = 0;
    ArrayList<File> files = new ArrayList<>( fileNames.length );
    for( String fileName : fileNames ) {
      File file = new File( fileName );
      if( file.exists() ) {
        files.add( file );

        if( fileName.equals( activeFileName ) ) {
          activeIndex = files.size() - 1;
        }
      }
    }

    if( files.isEmpty() ) {
      newEditor();
      return;
    }

    openEditors( files, activeIndex );
  }

  private void saveState( FileEditor[] allEditors, FileEditor activeEditor ) {
    ArrayList<String> fileNames = new ArrayList<>( allEditors.length );
    for( FileEditor fileEditor : allEditors ) {
      if( fileEditor.getPath() != null ) {
        fileNames.add( fileEditor.getPath().toString() );
      }
    }

    Preferences state = getState();
    Utils.putPrefsStrings( state, "file", fileNames.toArray( new String[ fileNames.size() ] ) );
    if( activeEditor != null && activeEditor.getPath() != null ) {
      state.put( "activeFile", activeEditor.getPath().toString() );
    } else {
      state.remove( "activeFile" );
    }
  }
}
