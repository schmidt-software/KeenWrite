/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.installer;

import com.keenwrite.io.SysFile;
import com.keenwrite.io.downloads.DownloadManager;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.keenwrite.Messages.get;
import static com.keenwrite.typesetting.installer.InstallPane.*;
import static com.keenwrite.typesetting.installer.TypesetterInstaller.*;
import static java.lang.System.getProperty;
import static javafx.application.Platform.runLater;

public final class WindowsContainerDownloadPane {
  /**
   * Property for the download thread to help ensure safe reentrancy.
   */
  private static final String WIN_DOWNLOADER = "windows.container.downloader";

  private static final String PREFIX =
    "Wizard.typesetter.win.2.download.container";

  static InstallPane create() {
    final var binary = get( PREFIX + ".download.link.url" );
    final var uri = URI.create( binary );
    final var file = Paths.get( uri.getPath() ).toFile();
    final var filename = file.getName();
    final var directory = Path.of( getProperty( "user.dir" ) );
    final var target = directory.resolve( filename ).toFile();
    final var source = labelf( PREFIX + ".paths", filename, directory );
    final var status = labelf( PREFIX + ".status.progress", 0, 0 );

    final var border = new BorderPane();
    border.setTop( source );
    border.setCenter( spacer() );
    border.setBottom( status );

    final var pane = wizardPane(
      PREFIX + ".header",
      ( wizard, self ) -> {
        self.disableNext( true );

        final var properties = wizard.getProperties();
        properties.put( WIN_BIN, target );

        final var thread = properties.get( WIN_DOWNLOADER );
        if( thread instanceof Task<?> downloader && downloader.isRunning() ) {
          return;
        }

        final var sysFile = new SysFile( target );
        final var checksum = get( "Wizard.typesetter.container.checksum" );

        if( sysFile.exists() ) {
          final var checksumOk = sysFile.isChecksum( checksum );
          final var msg = checksumOk
            ? get( PREFIX + ".status.checksum.ok", filename )
            : get( PREFIX + ".status.checksum.no", filename );

          update( status, msg );
          self.disableNext( !checksumOk );
        }
        else {
          final var task = downloadAsync( uri, target, ( progress, bytes ) -> {
            final var msg = progress < 0
              ? get( PREFIX + ".status.bytes", bytes )
              : get( PREFIX + ".status.progress", progress, bytes );

            update( status, msg );
          } );

          properties.put( WIN_DOWNLOADER, task );

          task.setOnSucceeded(
            event -> {
              update( status, get( PREFIX + ".status.success" ) );
              properties.remove( WIN_DOWNLOADER );
              self.disableNext( false );
            }
          );
          task.setOnFailed(
            event -> {
              update( status, get( PREFIX + ".status.failure" ) );
              properties.remove( WIN_DOWNLOADER );
            }
          );
        }
      }
    );
    pane.setContent( border );

    return pane;
  }

  /**
   * Downloads a resource to a local file in a separate {@link Thread}.
   *
   * @param uri      The resource to download.
   * @param file     The destination target for the resource.
   * @param listener Receives updates as the download proceeds.
   */
  private static Task<Void> downloadAsync(
    final URI uri,
    final File file,
    final DownloadManager.ProgressListener listener ) {
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
}
