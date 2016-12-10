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
package com.scrivenvar;

import static com.scrivenvar.Messages.get;
import com.scrivenvar.predicates.files.FileTypePredicate;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Settings;
import com.scrivenvar.service.events.AlertMessage;
import com.scrivenvar.service.events.AlertService;
import static com.scrivenvar.service.events.AlertService.NO;
import static com.scrivenvar.service.events.AlertService.YES;
import com.scrivenvar.util.Utils;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.stage.Window;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;

/**
 * Tab pane for file editors.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class FileEditorTabPane extends TabPane implements ChangeListener<Tab> {

  private final static String FILTER_PREFIX = "Dialog.file.choose.filter";

  private final Options options = Services.load( Options.class );
  private final Settings settings = Services.load( Settings.class );
  private final AlertService alertService = Services.load( AlertService.class );

  private final ReadOnlyObjectWrapper<FileEditorTab> activeFileEditor = new ReadOnlyObjectWrapper<>();
  private final ReadOnlyBooleanWrapper anyFileEditorModified = new ReadOnlyBooleanWrapper();

  public FileEditorTabPane() {
    final ObservableList<Tab> tabs = getTabs();

    setFocusTraversable( false );
    setTabClosingPolicy( TabClosingPolicy.ALL_TABS );

    // Observe the tab so that when a new tab is opened or selected,
    // a notification is kicked off.
    getSelectionModel().selectedItemProperty().addListener( this );

    // update anyFileEditorModified property
    final ChangeListener<Boolean> modifiedListener = (observable, oldValue, newValue) -> {
      for( final Tab tab : tabs ) {
        if( ((FileEditorTab)tab.getUserData()).isModified() ) {
          this.anyFileEditorModified.set( true );
          break;
        }
      }
    };

    tabs.addListener( (ListChangeListener<Tab>)change -> {
      while( change.next() ) {
        if( change.wasAdded() ) {
          change.getAddedSubList().stream().forEach( (tab) -> {
            ((FileEditorTab)tab.getUserData()).modifiedProperty().addListener( modifiedListener );
          } );
        } else if( change.wasRemoved() ) {
          change.getRemoved().stream().forEach( (tab) -> {
            ((FileEditorTab)tab.getUserData()).modifiedProperty().removeListener( modifiedListener );
          } );
        }
      }

      // Changes in the tabs may also change anyFileEditorModified property
      // (e.g. closed modified file)
      modifiedListener.changed( null, null, null );
    } );
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

  @Override
  public void changed(
    final ObservableValue<? extends Tab> observable,
    final Tab oldTab,
    final Tab newTab ) {

    if( newTab != null ) {
      this.activeFileEditor.set( (FileEditorTab)newTab.getUserData() );
    }
  }

  Node getNode() {
    return this;
  }

  /**
   * Allows clients to manipulate the editor content directly.
   *
   * @return The text area for the active file editor.
   */
  public StyledTextArea getEditor() {
    return getActiveFileEditor().getEditorPane().getEditor();
  }

  public FileEditorTab getActiveFileEditor() {
    return this.activeFileEditor.get();
  }

  ReadOnlyObjectProperty<FileEditorTab> activeFileEditorProperty() {
    return this.activeFileEditor.getReadOnlyProperty();
  }

  ReadOnlyBooleanProperty anyFileEditorModifiedProperty() {
    return this.anyFileEditorModified.getReadOnlyProperty();
  }

  private FileEditorTab createFileEditor( final Path path ) {
    final FileEditorTab tab = new FileEditorTab( path );

    tab.setOnCloseRequest( e -> {
      if( !canCloseEditor( tab ) ) {
        e.consume();
      }
    } );

    return tab;
  }

  /**
   * Called when the user selects New from the File menu.
   *
   * @return The newly added tab.
   */
  FileEditorTab newEditor() {
    final FileEditorTab tab = createFileEditor( null );

    getTabs().add( tab );
    getSelectionModel().select( tab );
    return tab;
  }

  List<FileEditorTab> openFileDialog() {
    final FileChooser dialog
      = createFileChooser( get( "Dialog.file.choose.open.title" ) );
    final List<File> files = dialog.showOpenMultipleDialog( getWindow() );

    return (files != null && !files.isEmpty())
      ? openFiles( files )
      : new ArrayList<>();
  }

  /**
   * Opens the files into new editors, unless one of those files was a
   * definition file. The definition file is loaded into the definition pane,
   * but only the first one selected (multiple definition files will result in a
   * warning).
   *
   * @param files The list of non-definition files that the were requested to
   * open.
   *
   * @return A list of files that can be opened in text editors.
   */
  private List<FileEditorTab> openFiles( final List<File> files ) {
    final List<FileEditorTab> openedEditors = new ArrayList<>();

    final FileTypePredicate predicate
      = new FileTypePredicate( createExtensionFilter( "definition" ).getExtensions() );

    // The user might have opened muliple definitions files. These will
    // be discarded from the text editable files.
    final List<File> definitions
      = files.stream().filter( predicate ).collect( Collectors.toList() );

    // Create a modifiable list to remove any definition files that were
    // opened.
    final List<File> editors = new ArrayList<>( files );
    editors.removeAll( definitions );

    // If there are any editor-friendly files opened (e.g,. Markdown, XML), then
    // open them up in new tabs.
    if( editors.size() > 0 ) {
      saveLastDirectory( editors.get( 0 ) );
      openedEditors.addAll( openEditors( editors, 0 ) );
    }

    if( definitions.size() > 0 ) {
      openDefinition( definitions.get( 0 ) );
    }

    return openedEditors;
  }

  private List<FileEditorTab> openEditors( final List<File> files, final int activeIndex ) {
    final int fileTally = files.size();
    final List<FileEditorTab> editors = new ArrayList<>( fileTally );
    final List<Tab> tabs = getTabs();

    // Close single unmodified "Untitled" tab.
    if( tabs.size() == 1 ) {
      final FileEditorTab fileEditor = (FileEditorTab)(tabs.get( 0 ).getUserData());

      if( fileEditor.getPath() == null && !fileEditor.isModified() ) {
        closeEditor( fileEditor, false );
      }
    }

    for( int i = 0; i < fileTally; i++ ) {
      final Path path = files.get( i ).toPath();

      // Check whether file is already opened.
      FileEditorTab fileEditor = findEditor( path );

      if( fileEditor == null ) {
        fileEditor = createFileEditor( path );
        getTabs().add( fileEditor );
        editors.add( fileEditor );
      }

      // Select first file.
      if( i == activeIndex ) {
        getSelectionModel().select( fileEditor );
      }
    }

    return editors;
  }

  /**
   * Called when the user has opened a definition file (using the file open
   * dialog box). This will replace the current set of definitions for the
   * active tab.
   *
   * @param definition The file to open.
   */
  private void openDefinition( final File definition ) {
    System.out.println( "open definition file: " + definition.toString() );
  }

  boolean saveEditor( final FileEditorTab fileEditor ) {
    if( fileEditor == null || !fileEditor.isModified() ) {
      return true;
    }

    if( fileEditor.getPath() == null ) {
      getSelectionModel().select( fileEditor );

      final FileChooser fileChooser = createFileChooser( Messages.get( "Dialog.file.choose.save.title" ) );
      final File file = fileChooser.showSaveDialog( getWindow() );
      if( file == null ) {
        return false;
      }

      saveLastDirectory( file );
      fileEditor.setPath( file.toPath() );
    }

    return fileEditor.save();
  }

  boolean saveAllEditors() {
    boolean success = true;

    for( FileEditorTab fileEditor : getAllEditors() ) {
      if( !saveEditor( fileEditor ) ) {
        success = false;
      }
    }

    return success;
  }

  boolean canCloseEditor( final FileEditorTab tab ) {
    if( !tab.isModified() ) {
      return true;
    }

    final AlertMessage message = getAlertService().createAlertMessage(
      Messages.get( "Alert.file.close.title" ),
      Messages.get( "Alert.file.close.text" ),
      tab.getText()
    );

    final Alert alert = getAlertService().createAlertConfirmation( message );
    final ButtonType response = alert.showAndWait().get();

    return response == YES ? saveEditor( tab ) : response == NO;
  }

  private AlertService getAlertService() {
    return this.alertService;
  }

  boolean closeEditor( FileEditorTab fileEditor, boolean save ) {
    if( fileEditor == null ) {
      return true;
    }

    final Tab tab = fileEditor;

    if( save ) {
      Event event = new Event( tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT );
      Event.fireEvent( tab, event );

      if( event.isConsumed() ) {
        return false;
      }
    }

    getTabs().remove( tab );

    if( tab.getOnClosed() != null ) {
      Event.fireEvent( tab, new Event( Tab.CLOSED_EVENT ) );
    }

    return true;
  }

  boolean closeAllEditors() {
    final FileEditorTab[] allEditors = getAllEditors();
    final FileEditorTab activeEditor = getActiveFileEditor();

    // try to save active tab first because in case the user decides to cancel,
    // then it stays active
    if( activeEditor != null && !canCloseEditor( activeEditor ) ) {
      return false;
    }

    // This should be called any time a tab changes.
    persistPreferences();

    // save modified tabs
    for( int i = 0; i < allEditors.length; i++ ) {
      final FileEditorTab fileEditor = allEditors[ i ];

      if( fileEditor == activeEditor ) {
        continue;
      }

      if( fileEditor.isModified() ) {
        // activate the modified tab to make its modified content visible to the user
        getSelectionModel().select( i );

        if( !canCloseEditor( fileEditor ) ) {
          return false;
        }
      }
    }

    // Close all tabs.
    for( final FileEditorTab fileEditor : allEditors ) {
      if( !closeEditor( fileEditor, false ) ) {
        return false;
      }
    }

    return getTabs().isEmpty();
  }

  private FileEditorTab[] getAllEditors() {
    final ObservableList<Tab> tabs = getTabs();
    final FileEditorTab[] allEditors = new FileEditorTab[ tabs.size() ];
    final int length = tabs.size();

    for( int i = 0; i < length; i++ ) {
      allEditors[ i ] = (FileEditorTab)tabs.get( i ).getUserData();
    }

    return allEditors;
  }

  /**
   * Returns the file editor tab that has the given path.
   *
   * @return null No file editor tab for the given path was found.
   */
  private FileEditorTab findEditor( final Path path ) {
    for( final Tab tab : getTabs() ) {
      final FileEditorTab fileEditor = (FileEditorTab)tab;

      System.out.println( "path = " + path );
      System.out.println( "fileEditor = " + fileEditor.isPath( path ) );

      if( fileEditor.isPath( path ) ) {
        return fileEditor;
      }
    }

    return null;
  }

  private FileChooser createFileChooser( String title ) {
    final FileChooser fileChooser = new FileChooser();

    fileChooser.setTitle( title );
    fileChooser.getExtensionFilters().addAll(
      createExtensionFilters() );

    final String lastDirectory = getState().get( "lastDirectory", null );
    File file = new File( (lastDirectory != null) ? lastDirectory : "." );

    if( !file.isDirectory() ) {
      file = new File( "." );
    }

    fileChooser.setInitialDirectory( file );
    return fileChooser;
  }

  private List<ExtensionFilter> createExtensionFilters() {
    final List<ExtensionFilter> list = new ArrayList<>();

    // TODO: Return a list of all properties that match the filter prefix.
    // This will allow dynamic filters to be added and removed just by
    // updating the properties file.
    list.add( createExtensionFilter( "markdown" ) );
    list.add( createExtensionFilter( "definition" ) );
    list.add( createExtensionFilter( "xml" ) );
    list.add( createExtensionFilter( "all" ) );
    return list;
  }

  private ExtensionFilter createExtensionFilter( final String filetype ) {
    final String tKey = String.format( "%s.title.%s", FILTER_PREFIX, filetype );
    final String eKey = String.format( "%s.ext.%s", FILTER_PREFIX, filetype );

    return new ExtensionFilter( Messages.get( tKey ), getExtensions( eKey ) );
  }

  private List<String> getExtensions( final String key ) {
    return getStringSettingList( key );
  }

  private List<String> getStringSettingList( String key ) {
    return getStringSettingList( key, null );
  }

  private List<String> getStringSettingList( String key, List<String> values ) {
    return getSettings().getStringSettingList( key, values );
  }

  private void saveLastDirectory( final File file ) {
    getState().put( "lastDirectory", file.getParent() );
  }

  public void restorePreferences() {
    int activeIndex = 0;

    final Preferences preferences = getState();
    final String[] fileNames = Utils.getPrefsStrings( preferences, "file" );
    final String activeFileName = preferences.get( "activeFile", null );

    final ArrayList<File> files = new ArrayList<>( fileNames.length );

    for( final String fileName : fileNames ) {
      final File file = new File( fileName );

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

  public void persistPreferences() {
    final ObservableList<Tab> allEditors = getTabs();
    final List<String> fileNames = new ArrayList<>( allEditors.size() );

    for( final Tab tab : allEditors ) {
      final FileEditorTab fileEditor = (FileEditorTab)tab;

      if( fileEditor.getPath() != null ) {
        fileNames.add( fileEditor.getPath().toString() );
      }
    }

    final Preferences preferences = getState();
    Utils.putPrefsStrings( preferences, "file", fileNames.toArray( new String[ fileNames.size() ] ) );

    final FileEditorTab activeEditor = getActiveFileEditor();

    if( activeEditor != null && activeEditor.getPath() != null ) {
      preferences.put( "activeFile", activeEditor.getPath().toString() );
    } else {
      preferences.remove( "activeFile" );
    }
  }

  private Settings getSettings() {
    return this.settings;
  }

  protected Options getOptions() {
    return this.options;
  }

  private Window getWindow() {
    return getScene().getWindow();
  }

  protected Preferences getState() {
    return getOptions().getState();
  }
}
