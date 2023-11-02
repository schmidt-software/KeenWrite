/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static java.io.File.createTempFile;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for testing that the {@link FileWatchService} fires the
 * expected {@link FileEvent} when the system raises state changes.
 */
class FileWatchServiceTest {
  /**
   * Test that modifying a file produces a {@link FileEvent}.
   *
   * @throws IOException          Could not create watcher service.
   * @throws InterruptedException Could not join on watcher service thread.
   */
  @Test
  @Timeout( value = 5, unit = SECONDS )
  void test_SingleFile_Write_Notified() throws
    IOException, InterruptedException {
    final var text = "arbitrary text to write";
    final var file = createTemporaryFile();
    final var service = new FileWatchService( file );
    final var thread = new Thread( service );
    final var semaphor = new Semaphore( 0 );
    final var listener = createListener( f -> {
      semaphor.release();
      assertEquals( file, f );
    } );

    thread.start();
    service.addListener( listener );
    Files.writeString( file.toPath(), text, UTF_8, CREATE, APPEND );
    semaphor.acquire();
    service.stop();
    thread.join();
  }

  private FileModifiedListener createListener( final Consumer<File> action ) {
    return fileEvent -> action.accept( fileEvent.getFile() );
  }

  private File createTemporaryFile() throws IOException {
    final var prefix = getClass().getPackageName();
    final var file = createTempFile( prefix, null, null );
    file.deleteOnExit();
    return file;
  }
}
