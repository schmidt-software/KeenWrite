/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.SysFile;
import com.keenwrite.io.UserDataDir;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.dialog.Wizard;

import java.io.File;
import java.net.URI;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getUri;
import static com.keenwrite.typesetting.installer.panes.WindowsManagerInstallPane.WIN_BIN;

/**
 * Responsible for downloading the container manager software on Windows.
 */
public final class WindowsManagerDownloadPane extends InstallerPane {

  /**
   * Property for the download thread to help ensure safe reentrancy.
   */
  private static final String WIN_DOWNLOADER = "windows.container.downloader";

  private static final String PREFIX =
    "Wizard.typesetter.win.2.download.container";

  private final Label mStatus;
  private final File mTarget;
  private final String mFilename;
  private final URI mUri;

  public WindowsManagerDownloadPane() {
    mUri = getUri( PREFIX + ".download.link.url" );
    mFilename = toFilename( mUri );
    final var directory = UserDataDir.getAppPath( APP_TITLE );
    mTarget = directory.resolve( mFilename ).toFile();
    final var source = labelf( PREFIX + ".paths", mFilename, directory );
    mStatus = labelf( PREFIX + ".status.progress", 0, 0 );

    final var border = new BorderPane();
    border.setTop( source );
    border.setCenter( spacer() );
    border.setBottom( mStatus );

    setContent( border );
  }

  @Override
  public void onEnteringPage( final Wizard wizard ) {
    disableNext( true );

    final var properties = wizard.getProperties();
    properties.put( WIN_BIN, mTarget );

    final var thread = properties.get( WIN_DOWNLOADER );
    if( thread instanceof Task<?> downloader && downloader.isRunning() ) {
      return;
    }

    final var sysFile = new SysFile( mTarget );
    final var checksum = get( "Wizard.typesetter.container.checksum" );

    if( sysFile.exists() ) {
      final var checksumOk = sysFile.isChecksum( checksum );
      final var msg = checksumOk
        ? get( PREFIX + ".status.checksum.ok", mFilename )
        : get( PREFIX + ".status.checksum.no", mFilename );

      update( mStatus, msg );
      disableNext( !checksumOk );
    }
    else {
      final var task = downloadAsync( mUri, mTarget, ( progress, bytes ) -> {
        final var msg = progress < 0
          ? get( PREFIX + ".status.bytes", bytes )
          : get( PREFIX + ".status.progress", progress, bytes );

        update( mStatus, msg );
      } );

      properties.put( WIN_DOWNLOADER, task );

      task.setOnSucceeded(
        event -> {
          update( mStatus, get( PREFIX + ".status.success" ) );
          properties.remove( WIN_DOWNLOADER );
          disableNext( false );
        }
      );
      task.setOnFailed(
        event -> {
          update( mStatus, get( PREFIX + ".status.failure" ) );
          properties.remove( WIN_DOWNLOADER );
        }
      );
    }
  }

  @Override
  public String getHeaderKey() {
    return PREFIX + ".header";
  }
}
