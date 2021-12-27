package com.keenwrite.preferences;

import org.junit.jupiter.api.Test;

import static com.keenwrite.preferences.Key.key;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that {@link Key} hierarchies can be transformed into alternate data
 * models.
 */
class KeyTest {
  @Test
  public void test_String_ParentHierarchy_DotNotation() {
    final var keyRoot = key( "root" );
    final var keyMeta = key( keyRoot, "meta" );
    final var keyDate = key( keyMeta, "date" );

    final var expected = "root.meta.date";
    final var actual = keyDate.toString();

    assertEquals( expected, actual );
  }
}
