/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.dialogs;

import com.keenwrite.events.FileOpenEvent;
import javafx.stage.Window;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.downloads.DownloadManager.*;
import static com.keenwrite.util.Strings.sanitize;

/**
 * Dialog to open a remote Markdown file.
 */
public final class OpenUrlDialog extends CustomDialog<File> {
  private static final String PREFIX = "Dialog.open_url.";
  private static final String DOWNLOAD = "Main.status.url.request.";
  private static final String STATUS = STR."\{DOWNLOAD}status.";

  private final Path mParent;
  private String mUrl = "";

  /**
   * Ensures that all dialogs can be closed.
   *
   * @param owner  The parent window of this dialog.
   * @param parent Directory to store downloaded file.
   */
  public OpenUrlDialog( final Window owner, final Path parent ) {
    super( owner, STR."\{PREFIX}title" );

    mParent = parent;

    super.initialize();
  }

  @Override
  protected void initInputFields() {
    addInputField(
      "url",
      STR."\{PREFIX}label.url", STR."\{PREFIX}prompt.url",
      mUrl,
      ( _, _, n ) -> mUrl = sanitize( n )
    );
  }

  @Override
  protected File handleAccept() {
    return mUrl.isBlank() ? null : download( mUrl );
  }

  private File download( final String reference ) {
    try {
      clue( STR."\{DOWNLOAD}fetch", reference );

      final var uri = new URI( reference );
      final var path = toFile( uri );
      final var basedir = path.getName();
      final var file = mParent.resolve( basedir ).toFile();

      if( file.exists() ) {
        clue( STR."\{DOWNLOAD}exists", file );
      }
      else {
        final var task = downloadAsync( uri, file, ( progress, bytes ) -> {
          final var suffix = progress < 0 ? "bytes" : "progress";

          clue( STR."\{STATUS}\{suffix}", progress, bytes );
        } );

        task.setOnSucceeded( _ -> {
          clue( STR."\{DOWNLOAD}success", file );

          // Only after the download succeeds can we open the file.
          FileOpenEvent.fire( file.toURI() );
        } );
        task.setOnFailed( _ -> clue( STR."\{DOWNLOAD}failure", uri ) );
      }

      // The return value isn't used because the download happens
      // asynchronously. If the download succeeds, an event is fired.
      return null;
    } catch( final Exception e ) {
      throw new RuntimeException( e );
    }
  }
}
