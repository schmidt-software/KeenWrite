/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SysFileTest {

  @Test
  void test_Locate_ExistingExecutable_PathFound() {
    final var command = "ls";
    final var file = new SysFile( command );
    assertTrue( file.canRun() );

    final var located = file.locate();
    assertTrue( located.isPresent() );

    final var path = located.get();
    final var actual = path.toAbsolutePath().toString();
    final var expected = "/usr/bin/" + command;

    assertEquals( expected, actual );
  }
}
