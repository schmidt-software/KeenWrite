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
import static com.keenwrite.ui.explorer.FilePickerFactory.SelectionType.*;
import static java.lang.String.format;
import static java.nio.file.FileSystems.getDefault;

/**
 * Shim for a {@link FilePicker} instance that is implemented in pure Java.
 * This particular picker is added to avoid using the bug-ridden JavaFX
 * {@link FileChooser} that invokes the native file chooser.
 */
public class FilePickerFactory {
  public enum SelectionType {
    DIRECTORY_OPEN( "open" ),
    FILE_IMPORT( "import" ),
    FILE_EXPORT( "export" ),
    FILE_OPEN_SINGLE( "open" ),
    FILE_OPEN_MULTIPLE( "open" ),
    FILE_OPEN_NEW( "open" ),
    FILE_SAVE_AS( "save" );

    private final String mTitle;

    SelectionType( final String title ) {
      assert title != null;
      mTitle = Messages.get( format( "Dialog.file.choose.%s.title", title ) );
    }

    public String getTitle() {
      return mTitle;
    }
  }

  private final ObjectProperty<File> mDirectory;
  private final Locale mLocale;

  public FilePickerFactory( final Workspace workspace ) {
    mDirectory = workspace.fileProperty( KEY_UI_RECENT_DIR );
    mLocale = workspace.getLocale();
  }

  public FilePicker createModal(
    final Window owner, final SelectionType options ) {
    final var picker = new NativeFilePicker( owner, options );

    picker.setInitialDirectory( mDirectory.get().toPath() );

    return picker;
  }

  public Node createModeless() {
    return new FilesView( mDirectory, mLocale );
  }

  /**
   * Operating system's file selection dialog.
   */
  private static final class NativeFilePicker implements FilePicker {
    private final FileChooser mChooser = new FileChooser();
    private final Window mOwner;
    private final SelectionType mType;

    public NativeFilePicker( final Window owner, final SelectionType type ) {
      assert owner != null;
      assert type != null;

      mOwner = owner;
      mType = type;
    }

    @Override
    public void setInitialFilename( final File file ) {
      assert file != null;
      mChooser.setInitialFileName( file.getName() );
    }

    @Override
    public void setInitialDirectory( final Path path ) {
      assert path != null;
      mChooser.setInitialDirectory( path.toFile() );
    }

    @Override
    public Optional<List<File>> choose() {
      if( mType == FILE_OPEN_MULTIPLE ) {
        return Optional.of( mChooser.showOpenMultipleDialog( mOwner ) );
      }

      final File file = mType == FILE_EXPORT || mType == FILE_SAVE_AS
        ? mChooser.showSaveDialog( mOwner )
        : mChooser.showOpenDialog( mOwner );

      return file == null ? Optional.empty() : Optional.of( List.of( file ) );
    }
  }

  /**
   * Pure JavaFX file selection dialog.
   */
  private class PureFilePicker implements FilePicker {
    private final Window mParent;
    private final Builder mBuilder;

    private PureFilePicker( final Window owner, final SelectionType type ) {
      assert owner != null;
      assert type != null;

      mParent = owner;
      mBuilder = builder().setFileSystem( getDefault() );

      mBuilder.setTitle( type.getTitle() );
      mBuilder.setAction( switch( type ) {
        case FILE_OPEN_MULTIPLE -> OPEN_EXISTING_MULTIPLE;
        case FILE_EXPORT, FILE_SAVE_AS -> CREATE;
        default -> OPEN_EXISTING_SINGLE;
      } );
      mBuilder.setAllowDirectoryCreation( true );
    }

    @Override
    public void setInitialFilename( final File file ) {
      mBuilder.setInitialFileName( file.getName() );
    }

    @Override
    public void setInitialDirectory( final Path path ) {
      mBuilder.setInitialDirectory( path );
    }

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
