/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import com.io7m.jwheatsheaf.ui.JWFileChoosers;
import com.keenwrite.Messages;
import com.keenwrite.preferences.Workspace;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.io7m.jwheatsheaf.api.JWFileChooserAction.*;
import static com.io7m.jwheatsheaf.api.JWFileChooserConfiguration.Builder;
import static com.io7m.jwheatsheaf.api.JWFileChooserConfiguration.builder;
import static com.keenwrite.constants.Constants.USER_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.KEY_UI_RECENT_DIR;
import static java.nio.file.FileSystems.getDefault;
import static java.util.Optional.ofNullable;

/**
 * Shim for a {@link FilePicker} instance that is implemented in pure Java.
 * This particular picker is added to avoid using the bug-ridden JavaFX
 * {@link FileChooser} that invokes the native file chooser.
 */
public class FilePickerFactory {
  public enum Options {
    DIRECTORY_OPEN,
    FILE_IMPORT,
    FILE_EXPORT,
    FILE_OPEN_SINGLE,
    FILE_OPEN_MULTIPLE,
    FILE_OPEN_NEW,
    FILE_SAVE_AS,
    PERMIT_CREATE_DIRS,
  }

  private final ObjectProperty<File> mDirectory;
  private final Locale mLocale;

  public FilePickerFactory( final Workspace workspace ) {
    mDirectory = workspace.fileProperty( KEY_UI_RECENT_DIR );
    mLocale = workspace.getLocale();
  }

  public FilePicker createModal(
    final Window owner, final Options... options ) {
    final var picker = new PureFilePicker( owner, options );
    picker.setInitialDirectory( mDirectory.get().toPath() );

    return picker;
  }

  public Node createModeless() {
    return new FilesView( mDirectory, mLocale );
  }

  /**
   * Pure Java implementation of a file selection widget.
   */
  private class PureFilePicker implements FilePicker {
    private final Window mParent;
    private final Builder mBuilder;

    private PureFilePicker( final Window window, final Options... options ) {
      mParent = window;
      mBuilder = builder().setFileSystem( getDefault() );

      final var args = ofNullable( options ).orElse( options );

      var title = "Dialog.file.choose.open.title";
      var action = OPEN_EXISTING_SINGLE;

      // It is a programming error to provide options that save or export to
      // multiple files.
      for( final var arg : args ) {
        switch( arg ) {
          case FILE_EXPORT -> {
            title = "Dialog.file.choose.export.title";
            action = CREATE;
          }
          case FILE_SAVE_AS -> {
            title = "Dialog.file.choose.save.title";
            action = CREATE;
          }
          case FILE_OPEN_SINGLE -> action = OPEN_EXISTING_SINGLE;
          case FILE_OPEN_MULTIPLE -> action = OPEN_EXISTING_MULTIPLE;
          case PERMIT_CREATE_DIRS -> mBuilder.setAllowDirectoryCreation( true );
        }
      }

      mBuilder.setTitle( Messages.get( title ) );
      mBuilder.setAction( action );
    }

    @Override
    public void setInitialFilename( final File file ) {
      mBuilder.setInitialFileName( file.getName() );
    }

    @Override
    public void setInitialDirectory( final Path path ) {
      mBuilder.setInitialDirectory( path );
    }

//    private JWFileChooserFilterType createFileFilters() {
//      final var filters = new JWFilterGlobFactory();
//
//      return filters.create( "PDF Files" )
//                    .addRule( INCLUDE, "**/*.pdf" )
//                    .addRule( EXCLUDE_AND_HALT, "**/.*" )
//                    .build();
//    }

    @Override
    public Optional<List<File>> choose() {
      final var config = mBuilder.build();
      try( final var chooserType = JWFileChoosers.create() ) {
        final var chooser = chooserType.create( mParent, config );
        final var paths = chooser.showAndWait();
        final var files = new ArrayList<File>( paths.size() );
        paths.forEach( path -> {
          final var file = path.toFile();
          files.add( file );

          // Set to the directory of the last file opened successfully.
          setRecentDirectory( file );
        } );

        return files.isEmpty() ? Optional.empty() : Optional.of( files );
      } catch( final Exception ex ) {
        clue( ex );
      }

      return Optional.empty();
    }
  }

  /**
   * Sets the value for the most recent directly selected. This will get the
   * parent location from the given file. If the parent is a readable directory
   * then this will update the most recent directory property.
   *
   * @param file A file contained in a directory.
   */
  private void setRecentDirectory( final File file ) {
    assert file != null;

    final var parent = file.getParentFile();
    final var dir = parent == null ? USER_DIRECTORY : parent;

    if( dir.isDirectory() && dir.canRead() ) {
      mDirectory.setValue( dir );
    }
  }
}
