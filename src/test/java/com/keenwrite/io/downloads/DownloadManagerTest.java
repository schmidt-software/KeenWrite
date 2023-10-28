/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.io.downloads;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.keenwrite.io.downloads.DownloadManager.ProgressListener;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static java.lang.System.setProperty;
import static org.junit.jupiter.api.Assertions.*;

class DownloadManagerTest {

  static {
    // By default, this returns null, which is not a valid user agent.
    setProperty( "http.agent", DownloadManager.class.getCanonicalName() );
  }

  private static final String URL =
    "https://keenwrite.com/downloads/KeenWrite.exe";

  @Test
  void test_Async_DownloadRequested_DownloadCompletes()
    throws IOException, InterruptedException,
    ExecutionException, URISyntaxException {
    final var complete = new AtomicInteger();
    final var transferred = new AtomicLong();

    final ProgressListener listener = ( percentage, bytes ) -> {
      complete.set( percentage );
      transferred.set( bytes );
    };

    final var file = File.createTempFile( "kw-", "test" );
    file.deleteOnExit();

    final var token = open( URL );
    final var executor = Executors.newFixedThreadPool( 1 );
    final var result = token.download( file, listener );
    final var future = executor.submit( result );

    assertFalse( future.isDone() );
    assertTrue( complete.get() < 100 );
    assertNull( future.get() );
    assertTrue( future.isDone() );
    assertEquals( 100, complete.get() );
    assertTrue( transferred.get() > 100_000 );

    token.close();
  }
}
