/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.io.FileType;
import com.keenwrite.Messages;
import com.keenwrite.io.File;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.service.Settings;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.keenwrite.Constants.*;
import static com.keenwrite.io.FileType.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.preferences.Workspace.KEY_UI_WORKING_DIR;
import static java.lang.String.format;

/**
 * Responsible for opening a dialog that provides users with the ability to
 * select files.
 */
public class FileChooserCommand {
  private static final String FILTER_EXTENSION_TITLES =
      "Dialog.file.choose.filter";
  private static final String PREF_DIRECTORY = DEFAULT_DIRECTORY.toString();

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

  /**
   * Allows saving the document under a new file name.
   *
   * @return The new file name.
   */
  public Optional<File> saveAs() {
    final var dialog = createFileChooser( "Dialog.file.choose.save.title" );
    return saveOrExportAs( dialog );
  }

  /**
   * Allows exporting the document to a new file format.
   *
   * @return The file name for exporting into.
   */
  public Optional<File> exportAs( final File filename ) {
    final var dialog = createFileChooser( "Dialog.file.choose.export.title" );
    dialog.setInitialFileName( filename.getName() );
    return saveOrExportAs( dialog );
  }

  /**
   * Helper method called when saving or exporting.
   *
   * @param dialog The {@link FileChooser} to display.
   * @return The file selected by the user.
   */
  private Optional<File> saveOrExportAs( final FileChooser dialog ) {
    final var selected = dialog.showSaveDialog( mWindow );
    final var file = selected == null ? null : new File( selected );

    storeLastDirectory( file );

    return Optional.ofNullable( file );
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
        getWorkspace().get( KEY_UI_WORKING_DIR, PREF_DIRECTORY )
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
    final var tKey = format( "%s.title.%s", FILTER_EXTENSION_TITLES, filetype );
    final var eKey = format( "%s.%s", GLOB_PREFIX_FILE, filetype );

    return new ExtensionFilter( Messages.get( tKey ), getExtensions( eKey ) );
  }

  private void storeLastDirectory( final File file ) {
    if( file != null ) {
      final var parent = file.getParent();
      getWorkspace().put(
          KEY_UI_WORKING_DIR, parent == null ? PREF_DIRECTORY : parent
      );
    }
  }

  private void storeLastDirectory( final List<File> files ) {
    if( files != null && !files.isEmpty() ) {
      storeLastDirectory( files.get( 0 ) );
    }
  }

  private List<String> getExtensions( final String key ) {
    return getSettings().getStringSettingList( key );
  }

  private Settings getSettings() {
    return sSettings;
  }

  private Workspace getWorkspace() {
    return Workspace.getInstance();
  }
}
