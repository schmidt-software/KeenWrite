package com.keenwrite.io;

import org.junit.jupiter.api.Test;

import static com.keenwrite.io.WindowsRegistry.*;
import static org.junit.jupiter.api.Assertions.*;

class WindowsRegistryTest {
  private static final String REG_PATH_PREFIX =
    "%USERPROFILE%";
  private static final String REG_PATH_SUFFIX =
    "\\AppData\\Local\\Microsoft\\WindowsApps;";
  private static final String REG_PATH = REG_PATH_PREFIX + REG_PATH_SUFFIX;

  @Test
  void test_Parse_RegistryEntry_ValueObtained() {
    final var expected = REG_PATH;
    final var actual = parseRegEntry(
      "    path    REG_EXPAND_SZ    " + expected
    );

    assertEquals( expected, actual );
  }

  @Test
  void test_Expand_RegistryEntry_VariablesExpanded() {
    final var value = "UserProfile";
    final var expected = value + REG_PATH_SUFFIX;
    final var actual = expand( REG_PATH, s -> value );

    assertEquals( expected, actual );
  }
}
