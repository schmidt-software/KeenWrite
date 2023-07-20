/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SysFileTest {

  @Test
  void test_Locate_ExistingExecutable_PathFound() {
    testFunction( SysFile::locate, "ls", "/usr/bin/ls" );
  }

  @Test
  void test_Where_ExistingExecutable_PathFound() {
    testFunction( sysFile -> {
      try {
        return sysFile.where();
      } catch( final IOException e ) {
        throw new RuntimeException( e );
      }
    }, "which", "/usr/bin/which" );
  }

  void testFunction( final Function<SysFile, Optional<Path>> consumer,
                     final String command,
                     final String expected ) {
    final var file = new SysFile( command );
    final var path = consumer.apply( file );
    final var failed = new AtomicBoolean( false );

    assertTrue( file.canRun() );

    path.ifPresentOrElse(
      location -> {
        final var actual = location.toAbsolutePath().toString();

        assertEquals( expected, actual );
      },
      () -> failed.set( true )
    );

    assertFalse( failed.get() );
  }
}
