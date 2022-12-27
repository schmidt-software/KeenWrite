package com.keenwrite.typesetting.installer;

import com.keenwrite.io.SysFile;
import com.keenwrite.io.downloads.DownloadManager;
import javafx.concurrent.Task;
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

public final class WindowsContainerDownloadPane {
  public static InstallPane create() {
    final var prefix = "Wizard.typesetter.win.2.download.container";

    final var binary = get( prefix + ".download.link.url" );
    final var uri = URI.create( binary );
    final var file = Paths.get( uri.getPath() ).toFile();
    final var filename = file.getName();
    final var directory = Path.of( getProperty( "user.dir" ) );
    final var target = directory.resolve( filename ).toFile();
    final var source = labelf( prefix + ".paths", filename, directory );
    final var status = labelf( prefix + ".status.progress", 0, 0 );

    final var border = new BorderPane();
    border.setTop( source );
    border.setCenter( spacer() );
    border.setBottom( status );

    final var pane = wizardPane(
      prefix + ".header",
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
            ? get( prefix + ".status.checksum.ok", filename )
            : get( prefix + ".status.checksum.no", filename );

          update( status, msg );
          self.disableNext( !checksumOk );
        }
        else {
          final var task = downloadAsync( uri, target, ( progress, bytes ) -> {
            final var msg = progress < 0
              ? get( prefix + ".status.bytes", bytes )
              : get( prefix + ".status.progress", progress, bytes );

            update( status, msg );
          } );

          properties.put( WIN_DOWNLOADER, task );

          task.setOnSucceeded(
            event -> {
              update( status, get( prefix + ".status.success" ) );
              properties.remove( WIN_DOWNLOADER );
              self.disableNext( false );
            }
          );
          task.setOnFailed(
            event -> {
              update( status, get( prefix + ".status.failure" ) );
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
}
