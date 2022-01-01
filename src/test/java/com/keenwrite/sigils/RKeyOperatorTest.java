/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for simulating R variable injection.
 */
class RKeyOperatorTest {

  /**
   * Test that a key name becomes an R variable.
   */
  @Test
  void test_Process_KeyName_Processed() {
    final var mOperator = new RKeyOperator();
    final var expected = "v$a$b$c$d";
    final var actual = mOperator.apply( "a.b.c.d" );

    assertEquals( expected, actual );
  }
}
