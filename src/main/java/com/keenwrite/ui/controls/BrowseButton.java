/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.controls;

import com.keenwrite.Messages;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.function.Consumer;

import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.FILE_ALT;

/**
 * Responsible for browsing directories.
 */
public class BrowseButton extends Button {
  /**
   * Initial directory.
   */
  private final File mDirectory;

  /**
   * Called when the user accepts a directory.
   */
  private final Consumer<File> mConsumer;

  public BrowseButton( final File directory, final Consumer<File> consumer ) {
    assert directory != null;
    assert consumer != null;

    mDirectory = directory;
    mConsumer = consumer;

    setGraphic( createGraphic( FILE_ALT ) );
    setOnAction( this::browse );
  }

  public void browse( final ActionEvent ignored ) {
    final var chooser = new DirectoryChooser();
    chooser.setTitle( Messages.get( "BrowseDirectoryButton.chooser.title" ) );
    chooser.setInitialDirectory( mDirectory );

    final var result = chooser.showDialog( getScene().getWindow() );

    if( result != null ) {
      mConsumer.accept( result );
    }
  }
}
