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
package com.keenwrite;

import com.keenwrite.service.Options;
import com.keenwrite.service.Settings;
import com.keenwrite.service.events.Notifier;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

import static com.keenwrite.Constants.GLOB_PREFIX_FILE;
import static com.keenwrite.Constants.sSettings;
import static com.keenwrite.FileType.*;
import static com.keenwrite.Messages.get;

/**
 * Tab pane for file editors.
 */
public final class FileEditorTabPane extends DetachableTabPane {

  private static final String FILTER_EXTENSION_TITLES =
      "Dialog.file.choose.filter";

  private static final Options sOptions = Services.load( Options.class );
  private static final Notifier sNotifier = Services.load( Notifier.class );

  private final ReadOnlyObjectWrapper<Path> mOpenDefinition =
      new ReadOnlyObjectWrapper<>();
  private final ReadOnlyObjectWrapper<FileEditorController> mActiveFileEditor =
      new ReadOnlyObjectWrapper<>();
  private final ReadOnlyBooleanWrapper mAnyFileEditorModified =
      new ReadOnlyBooleanWrapper();
  private final ChangeListener<Integer> mCaretPositionListener;

  /**
   * Constructs a new file editor tab pane.
   *
   * @param caretPositionListener Listens for caret position changes so
   *                              that the status bar can update.
   */
  public FileEditorTabPane(
      final ChangeListener<Integer> caretPositionListener ) {
    final var tabs = getTabs();

    setFocusTraversable( false );
    setTabClosingPolicy( TabClosingPolicy.ALL_TABS );

    addTabSelectionListener(
        ( tabPane, oldTab, newTab ) -> {
          if( newTab != null ) {
            // TODO: FIXME REFACTOR TABS
//            mActiveFileEditor.set( (FileEditorView) newTab );
          }
        }
    );

    final ChangeListener<Boolean> modifiedListener =
        ( observable, oldValue, newValue ) -> {
          for( final Tab tab : tabs ) {
            // TODO: FIXME REFACTOR TABS
//            if( ((FileEditorView) tab).isModified() ) {
//              mAnyFileEditorModified.set( true );
//              break;
//            }
          }
        };

    tabs.addListener(
        (ListChangeListener<Tab>) change -> {
          while( change.next() ) {
            if( change.wasAdded() ) {
              change.getAddedSubList().forEach(
                  ( tab ) -> {
                    // TODO: FIXME REFACTOR TABS
//                    final var fet = (FileEditorView) tab;
//                    fet.modifiedProperty().addListener( modifiedListener );
                  } );
            }
            else if( change.wasRemoved() ) {
              change.getRemoved().forEach(
                  ( tab ) -> {
                    // TODO: FIXME REFACTOR TABS
//                    final var fet = (FileEditorView) tab;
//                    fet.modifiedProperty().removeListener( modifiedListener );
                  }
              );
            }
          }

          // Changes in the tabs may also change anyFileEditorModified property
          // (e.g. closed modified file)
          modifiedListener.changed( null, null, null );
        }
    );

    mCaretPositionListener = caretPositionListener;
  }

  /**
   * Allows observers to be notified when the current file editor tab changes.
   *
   * @param listener The listener to notify of tab change events.
   */
  public void addTabSelectionListener( final ChangeListener<Tab> listener ) {
    // Observe the tab so that when a new tab is opened or selected,
    // a notification is kicked off.
    getSelectionModel().selectedItemProperty().addListener( listener );
  }

  /**
   * Returns the tab that has keyboard focus.
   *
   * @return A non-null instance.
   */
  public FileEditorController getActiveFileEditor() {
    return mActiveFileEditor.get();
  }

  /**
   * Returns the property corresponding to the tab that has focus.
   *
   * @return A non-null instance.
   */
  public ReadOnlyObjectProperty<FileEditorController> activeFileEditorProperty() {
    return mActiveFileEditor.getReadOnlyProperty();
  }

  /**
   * Property that can answer whether the text has been modified.
   *
   * @return A non-null instance, true meaning the content has not been saved.
   */
  ReadOnlyBooleanProperty anyFileEditorModifiedProperty() {
    return mAnyFileEditorModified.getReadOnlyProperty();
  }

  /**
   * Creates a new editor instance from the given path.
   *
   * @param path The file to open.
   * @return A non-null instance.
   */
  private FileEditorController createFileEditor( final Path path ) {
    assert path != null;

    final FileEditorController tab = new FileEditorController();

    // TODO: FIXME REFACTOR TABS
//    tab.setOnCloseRequest( e -> {
//      if( !canCloseEditor( tab ) ) {
//        e.consume();
//      }
//      else if( isActiveFileEditor( tab ) ) {
//        // Prevent prompting the user to save when there are no file editor
//        // tabs open.
//        mActiveFileEditor.set( null );
//      }
//    } );

    tab.addCaretPositionListener( mCaretPositionListener );

    return tab;
  }

  /**
   * Returns a property that changes when a new definition file is opened.
   *
   * @return The path to a definition file that was opened.
   */
  public ReadOnlyObjectProperty<Path> onOpenDefinitionFileProperty() {
    return getOnOpenDefinitionFile().getReadOnlyProperty();
  }

  private ReadOnlyObjectWrapper<Path> getOnOpenDefinitionFile() {
    return mOpenDefinition;
  }

  /**
   * Called when the user has opened a definition file (using the file open
   * dialog box). This will replace the current set of definitions for the
   * active tab.
   *
   * @param definition The file to open.
   */
  private void openDefinition( final File definition ) {
    // TODO: Prevent reading this file twice when a new text document is opened.
    // (might be a matter of checking the value first).
    getOnOpenDefinitionFile().set( definition.toPath() );
  }

  /**
   * Opens the Save As dialog for the user to save the content under a new
   * path.
   *
   * @param tab The tab with contents to save.
   * @return true The contents were saved, or the tab was null.
   */
  public boolean saveEditorAs( final FileEditorController tab ) {
    return false;
  }

  /**
   * Answers whether the file has had modifications.
   *
   * @param tab THe tab to check for modifications.
   * @return false The file is unmodified.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  boolean canCloseEditor( final FileEditorController tab ) {
    final var service = getNotifyService();
    final var canClose = new AtomicReference<>( true );

    if( tab.isModified() ) {
      // TODO: FIXME REFACTOR TABS
      final var message = service.createNotification(
          Messages.get( "Alert.file.close.title" ),
          Messages.get( "Alert.file.close.text" ),
          ""//tab.getText()
      );

      final var confirmSave = service.createConfirmation(
          getWindow(), message );

      // TODO: FIXME REFACTOR TABS
//      confirmSave.showAndWait().ifPresent(
//          save -> canClose.set( save == YES ? tab.save() : save == NO )
//      );
    }

    return canClose.get();
  }

  // TODO: FIXME REFACTOR TABS
  /*
  boolean closeEditor( final FileEditorController tab, final boolean save ) {
    if( tab == null ) {
      return true;
    }

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
  }*/

  boolean closeAllEditors() {
    final FileEditorController[] allEditors = getAllEditors();
    final FileEditorController activeEditor = getActiveFileEditor();

    // try to save active tab first because in case the user decides to cancel,
    // then it stays active
    if( activeEditor != null && !canCloseEditor( activeEditor ) ) {
      return false;
    }

    // This should be called any time a tab changes.
    persistPreferences();

    // save modified tabs
    for( int i = 0; i < allEditors.length; i++ ) {
      final FileEditorController fileEditor = allEditors[ i ];

      if( fileEditor == activeEditor ) {
        continue;
      }

      if( fileEditor.isModified() ) {
        // activate the modified tab to make its modified content visible to
        // the user
        getSelectionModel().select( i );

        if( !canCloseEditor( fileEditor ) ) {
          return false;
        }
      }
    }

    // Close all tabs.
    for( final FileEditorController fileEditor : allEditors ) {
      // TODO: FIXME REFACTOR TABS
//      if( !closeEditor( fileEditor, false ) ) {
//        return false;
//      }
    }

    return getTabs().isEmpty();
  }

  private FileEditorController[] getAllEditors() {
    final var tabs = getTabs();
    final int length = tabs.size();
    final var allEditors = new FileEditorController[ length ];

    for( int i = 0; i < length; i++ ) {
      // TODO: FIXME REFACTOR TABS
//      allEditors[ i ] = (FileEditorView) tabs.get( i );
    }

    return allEditors;
  }

  /**
   * Returns the file editor tab that has the given path.
   *
   * @return null No file editor tab for the given path was found.
   */
  private FileEditorController findEditor( final Path path ) {
    for( final Tab tab : getTabs() ) {
      // TODO: FIXME REFACTOR TABS
      final FileEditorController fileEditor = null;// (FileEditorView) tab;

      if( fileEditor.isPath( path ) ) {
        return fileEditor;
      }
    }

    return null;
  }

  /**
   * Opens a new {@link FileChooser} at the previously selected directory.
   *
   * @param key Message key from resource bundle.
   * @return {@link FileChooser} GUI allowing the user to pick a file.
   */
  private FileChooser createFileChooser( final String key ) {
    final FileChooser fileChooser = new FileChooser();

    fileChooser.setTitle( get( key ) );
    fileChooser.getExtensionFilters().addAll(
        createExtensionFilters() );

    final var lastDirectory = getPreferences().get( "lastDirectory", null );
    var file = new File( (lastDirectory != null) ? lastDirectory : "." );

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
    list.add( createExtensionFilter( ALL ) );
    list.add( createExtensionFilter( SOURCE ) );
    list.add( createExtensionFilter( DEFINITION ) );
    list.add( createExtensionFilter( XML ) );
    return list;
  }

  /**
   * Returns a filter for file name extensions recognized by the application
   * that can be opened by the user.
   *
   * @param filetype Used to find the globbing pattern for extensions.
   * @return A filename filter suitable for use by a FileDialog instance.
   */
  private ExtensionFilter createExtensionFilter( final FileType filetype ) {
    final String tKey = String.format( "%s.title.%s",
                                       FILTER_EXTENSION_TITLES,
                                       filetype );
    final String eKey = String.format( "%s.%s", GLOB_PREFIX_FILE, filetype );

    return new ExtensionFilter( Messages.get( tKey ), getExtensions( eKey ) );
  }

  public void initPreferences() {
    int activeIndex = 0;

    final String[] fileNames = sOptions.getStrings( "file" );
    final String activeFileName = sOptions.get( "activeFile", null );

    final List<File> files = new ArrayList<>( fileNames.length );

    for( final String fileName : fileNames ) {
      final File file = new File( fileName );

      if( file.exists() ) {
        files.add( file );

        if( fileName.equals( activeFileName ) ) {
          activeIndex = files.size() - 1;
        }
      }
    }

    // TODO: FIXME REFACTOR TABS
//    if( files.isEmpty() ) {
//      newEditor();
//    }
  }

  public void persistPreferences() {
    final var allEditors = getTabs();
    final List<String> fileNames = new ArrayList<>( allEditors.size() );

    for( final var tab : allEditors ) {
      // TODO: FIXME REFACTOR TABS
      final FileEditorController fileEditor = null;//(FileEditorView) tab;
      final var filePath = fileEditor.getPath();

      if( filePath != null ) {
        fileNames.add( filePath.toString() );
      }
    }

    final var preferences = getPreferences();
    sOptions.putStrings( "file", fileNames.toArray( new String[ 0 ] ) );

    final var activeEditor = getActiveFileEditor();
    final var filePath = activeEditor == null ? null : activeEditor.getPath();

    if( filePath == null ) {
      preferences.remove( "activeFile" );
    }
    else {
      preferences.put( "activeFile", filePath.toString() );
    }
  }

  private List<String> getExtensions( final String key ) {
    return getSettings().getStringSettingList( key );
  }

  private Notifier getNotifyService() {
    return sNotifier;
  }

  private Settings getSettings() {
    return sSettings;
  }

  private Window getWindow() {
    return getScene().getWindow();
  }

  private Preferences getPreferences() {
    return sOptions.getState();
  }
}
