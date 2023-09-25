/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDataDirTest {
  @Test
  void test_Unix_GetAppDirectory_DirectoryExists() {
    final var path = UserDataDir.getAppPath( "test" );
    final var file = path.toFile();

    assertTrue( file.exists() );
    assertTrue( file.delete() );
    assertFalse( file.exists() );
  }
}
