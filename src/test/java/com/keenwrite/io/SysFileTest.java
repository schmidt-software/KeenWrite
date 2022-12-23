/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysFileTest {
  private static final String REG_PATH_PREFIX =
    "%USERPROFILE%";
  private static final String REG_PATH_SUFFIX =
    "\\AppData\\Local\\Microsoft\\WindowsApps;";
  private static final String REG_PATH = REG_PATH_PREFIX + REG_PATH_SUFFIX;

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

  @Test
  void test_Parse_RegistryEntry_ValueObtained() {
    final var file = new SysFile( "unused" );
    final var expected = REG_PATH;
    final var actual =
      file.parseRegEntry( "    path    REG_EXPAND_SZ    " + expected );

    assertEquals( expected, actual );
  }

  @Test
  void test_Expand_RegistryEntry_VariablesExpanded() {
    final var value = "UserProfile";
    final var file = new SysFile( "unused" );
    final var expected = value + REG_PATH_SUFFIX;
    final var actual = file.expand( REG_PATH, s -> value );

    assertEquals( expected, actual );
  }
}
