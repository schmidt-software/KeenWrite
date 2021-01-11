/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.Messages;
import com.keenwrite.io.FileType;
import com.keenwrite.service.Settings;
import javafx.beans.property.Property;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.keenwrite.Constants.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.io.FileType.*;
import static java.lang.String.format;

/**
 * Responsible for opening a dialog that provides users with the ability to
 * select files.
 */
public final class FileChooserCommand {
  private static final String FILTER_EXTENSION_TITLES =
    "Dialog.file.choose.filter";

  /**
   * Dialog owner.
   */
  private final Window mParent;

  /**
   * Set to the directory of most recently selected file.
   */
  private final Property<File> mDirectory;

  /**
   * Constructs a new {@link FileChooserCommand} that will attach to a given
   * parent window and update the given property upon a successful selection.
   *
   * @param parent    The parent window that will own the dialog.
   * @param directory The most recently opened file's directory property.
   */
  public FileChooserCommand(
    final Window parent, final Property<File> directory ) {
    mParent = parent;
    mDirectory = directory;
  }

  /**
   * Returns a list of files to be opened.
   *
   * @return A non-null, possibly empty list of files to open.
   */
  public List<File> openFiles() {
    final var dialog = createFileChooser(
      "Dialog.file.choose.open.title" );
    final var list = dialog.showOpenMultipleDialog( mParent );
    final List<java.io.File> selected = list == null ? List.of() : list;
    final var files = new ArrayList<File>( selected.size() );

    files.addAll( selected );

    if( !files.isEmpty() ) {
      setRecentDirectory( files.get( 0 ) );
    }

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
    final var file = dialog.showSaveDialog( mParent );

    setRecentDirectory( file );

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
    chooser.setInitialDirectory( mDirectory.getValue() );

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
   * @return A file name filter suitable for use by a FileDialog instance.
   */
  private ExtensionFilter createExtensionFilter(
    final FileType filetype ) {
    final var tKey = format( "%s.title.%s", FILTER_EXTENSION_TITLES, filetype );
    final var eKey = format( "%s.%s", GLOB_PREFIX_FILE, filetype );

    return new ExtensionFilter( Messages.get( tKey ), getExtensions( eKey ) );
  }

  /**
   * Sets the value for the most recent directly selected. This will get the
   * parent location from the given file. If the parent is a readable directory
   * then this will update the most recent directory property.
   *
   * @param file A file contained in a directory.
   */
  private void setRecentDirectory( final File file ) {
    if( file != null ) {
      final var parent = file.getParentFile();
      final var dir = parent == null ? USER_DIRECTORY : parent;

      if( dir.isDirectory() && dir.canRead() ) {
        mDirectory.setValue( dir );
      }
    }
  }

  private List<String> getExtensions( final String key ) {
    return getSettings().getStringSettingList( key );
  }

  private static Settings getSettings() {
    return sSettings;
  }
}
