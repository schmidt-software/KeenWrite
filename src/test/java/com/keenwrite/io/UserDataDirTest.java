/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class UserDataDirTest {
  @Test
  void test_Unix_GetAppDirectory_DirectoryExists()
    throws FileNotFoundException {
    final var path = UserDataDir.getAppPath( "test" );
    final var file = path.toFile();

    assertTrue( file.exists() );
    assertTrue( file.delete() );
    assertFalse( file.exists() );
  }
}
