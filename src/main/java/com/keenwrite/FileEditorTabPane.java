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
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser.ExtensionFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static com.keenwrite.Constants.GLOB_PREFIX_FILE;
import static com.keenwrite.Constants.sSettings;
import static com.keenwrite.FileType.*;

/**
 * Tab pane for file editors.
 */
public final class FileEditorTabPane extends DetachableTabPane {

  private static final String FILTER_EXTENSION_TITLES =
      "Dialog.file.choose.filter";

  private static final Options sOptions = Services.load( Options.class );

  private final ReadOnlyObjectWrapper<FileEditorController> mActiveFileEditor =
      new ReadOnlyObjectWrapper<>();

  public FileEditorTabPane( ) {
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
    final String activeFileName = sOptions.get( "activeFile", null );

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
    sOptions.putStrings( "file", fileNames );

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

  private Settings getSettings() {
    return sSettings;
  }

  private Preferences getPreferences() {
    return sOptions.getState();
  }
}
