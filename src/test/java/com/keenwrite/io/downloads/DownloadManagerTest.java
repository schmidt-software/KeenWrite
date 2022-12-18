/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io.downloads;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.keenwrite.io.downloads.DownloadManager.ProgressListener;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static java.io.OutputStream.nullOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DownloadManagerTest {
  private static final String SITE = "https://github.com/";
  private static final String URL
    = SITE + "DaveJarvis/keenwrite/releases/latest/download/keenwrite.exe";

  @Test
  void test_Async_DownloadRequested_DownloadCompletes()
    throws IOException, InterruptedException, ExecutionException {
    final var complete = new AtomicInteger();
    final var transferred = new AtomicLong();

    final OutputStream output = nullOutputStream();
    final ProgressListener listener = ( percentage, bytes ) -> {
      complete.set( percentage );
      transferred.set( bytes );
    };

    final var token = open( URL );
    final var executor = Executors.newFixedThreadPool( 1 );
    final var result = token.download( output, listener );
    final var future = executor.submit( result );

    assertTrue( complete.get() < 100 );
    assertTrue( future.get() );
    assertEquals( 100, complete.get() );
    assertTrue( transferred.get() > 100_000 );

    token.close();
  }
}
