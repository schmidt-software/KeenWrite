/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.ui;

import com.keenwrite.FileType;
import com.keenwrite.Messages;
import com.keenwrite.Services;
import com.keenwrite.io.File;
import com.keenwrite.service.Options;
import com.keenwrite.service.Settings;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static com.keenwrite.Constants.*;
import static com.keenwrite.FileType.*;
import static com.keenwrite.Messages.get;

/**
 * Responsible for opening a dialog that provides users with the ability to
 * select files.
 */
public class FileChooserCommand {
  private static final String FILTER_EXTENSION_TITLES =
      "Dialog.file.choose.filter";
  private static final String OPTION_DIRECTORY_LAST = "lastDirectory";
  private static final String PREF_DIRECTORY = DEFAULT_DIRECTORY.toString();

  private static final Options sOptions = Services.load( Options.class );

  private final Window mWindow;

  public FileChooserCommand( final Window window ) {
    mWindow = window;
  }

  /**
   * Returns a list of files to be opened.
   *
   * @return A non-null, possibly empty list of files to open.
   */
  public List<File> openFiles() {
    final var dialog = createFileChooser( "Dialog.file.choose.open.title" );
    final var list = dialog.showOpenMultipleDialog( mWindow );
    final List<java.io.File> selected = list == null ? List.of() : list;
    final var files = new ArrayList<File>( selected.size() );

    for( final var file : selected ) {
      files.add( new File( file ) );
    }

    storeLastDirectory( files );

    return files;
  }

  public File saveAs() {
    final var dialog = createFileChooser( "Dialog.file.choose.save.title" );
    final var selected = dialog.showSaveDialog( mWindow );
    final var file = selected == null ? null : new File( selected );

    storeLastDirectory( file );

    return file;
  }

  private void storeLastDirectory( final File file ) {
    if( file != null ) {
      final var parent = file.getParent();
      getPreferences().put(
          OPTION_DIRECTORY_LAST, parent == null ? PREF_DIRECTORY : parent
      );
    }
  }

  private void storeLastDirectory( final List<File> files ) {
    if( files != null && !files.isEmpty() ) {
      storeLastDirectory( files.get( 0 ) );
    }
  }

  /**
   * Opens a new {@link FileChooser} at the previously selected directory.
   *
   * @param key Message key from resource bundle.
   * @return {@link FileChooser} GUI allowing the user to pick a file.
   */
  private FileChooser createFileChooser( final String key ) {
    final var chooser = new FileChooser();

    chooser.setTitle( get( key ) );
    chooser.getExtensionFilters().addAll( createExtensionFilters() );

    final var dir = new File(
        getPreferences().get( OPTION_DIRECTORY_LAST, PREF_DIRECTORY )
    );

    chooser.setInitialDirectory(
        dir.isDirectory() ? dir : DEFAULT_DIRECTORY.toFile()
    );

    return chooser;
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
