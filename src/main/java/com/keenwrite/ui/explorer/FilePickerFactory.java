/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.explorer;

import com.keenwrite.Messages;
import com.keenwrite.preferences.Workspace;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.keenwrite.preferences.AppKeys.KEY_UI_RECENT_DIR;
import static com.keenwrite.ui.explorer.FilePickerFactory.SelectionType.*;
import static java.lang.String.format;

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
        return Optional.ofNullable( mChooser.showOpenMultipleDialog( mOwner ) );
      }

      final File file = mType == FILE_EXPORT || mType == FILE_SAVE_AS
        ? mChooser.showSaveDialog( mOwner )
        : mChooser.showOpenDialog( mOwner );

      return file == null ? Optional.empty() : Optional.of( List.of( file ) );
    }
  }
}
