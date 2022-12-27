/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer.panes;

import com.keenwrite.io.SysFile;
import com.keenwrite.io.UserDataDir;
import com.keenwrite.io.downloads.DownloadManager;
import com.keenwrite.io.downloads.DownloadManager.ProgressListener;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.dialog.Wizard;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Paths;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.Messages.getUri;
import static javafx.application.Platform.runLater;

public final class WindowsContainerDownloadPane extends InstallerPane {
  private static final String WIN_BIN = "windows.container.binary";

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

  public WindowsContainerDownloadPane() {
    mUri = getUri( PREFIX + ".download.link.url" );
    mFilename = getFilename( mUri );
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
  public String getHeaderKey() {
    return PREFIX + ".header";
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

  /**
   * Downloads a resource to a local file in a separate {@link Thread}.
   *
   * @param uri      The resource to download.
   * @param file     The destination mTarget for the resource.
   * @param listener Receives updates as the download proceeds.
   */
  private static Task<Void> downloadAsync(
    final URI uri,
    final File file,
    final ProgressListener listener ) {
    final Task<Void> task = createTask( () -> {
      try( final var token = DownloadManager.open( uri ) ) {
        final var output = new FileOutputStream( file );
        final var downloader = token.download( output, listener );

        downloader.run();
      }

      return null;
    } );

    createThread( task ).start();
    return task;
  }

  private static void update( final Label node, final String text ) {
    runLater( () -> node.setText( text ) );
  }

  private static String getFilename( final URI uri ) {
    return Paths.get( uri.getPath() ).toFile().getName();
  }
}
