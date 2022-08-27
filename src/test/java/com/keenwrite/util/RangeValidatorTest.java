package com.keenwrite.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that the range format specifiers correctly identify integer values
 * inside and outside the range.
 */
class RangeValidatorTest {
  @Test
  void test_Validation_SingleRange_Valid() {
    // Arbitrary start and end.
    final var lo = 1;
    final var hi = 5;
    final var validator = new RangeValidator( lo + "-" + hi );

    for( int i = lo; i < hi; i++ ) {
      assertTrue( validator.test( i ) );
    }

    // Arbitrary bounds checks.
    assertFalse( validator.test( lo - 1 ) );
    assertFalse( validator.test( lo - 11 ) );
    assertFalse( validator.test( hi + 1 ) );
    assertFalse( validator.test( hi + 11 ) );
  }

  @Test
  void test_Validation_SingleValue_Valid() {
    // Arbitrary.
    final var i = 7;
    final var validator = new RangeValidator( Integer.toString( i ) );

    assertTrue( validator.test( i ) );
  }

  @Test
  void test_Validation_UnboundedMaxIntegerRange_Valid() {
    // Arbitrary.
    final var lo = 11;
    final var validator = new RangeValidator( lo + "-" );

    // Arbitrary end value.
    for( int i = lo; i < lo + 101; i++ ) {
      assertTrue( validator.test( i ) );
    }

    assertFalse( validator.test( 10 ) );
  }

  @Test
  void test_Validation_UnboundedMinIntegerRange_Valid() {
    // Arbitrary.
    final var hi = 5;
    final var validator = new RangeValidator( "-" + hi );

    for( int i = 1; i < hi; i++ ) {
      assertTrue( validator.test( i ) );
    }

    assertFalse( validator.test( 0 ) );
    assertFalse( validator.test( -1 ) );
  }

  @Test
  void test_Validation_MultipleRanges_Valid() {
    // Arbitrary.
    final var validator = new RangeValidator( "-5, 7-11, 13, 15-20, 30-" );

    assertTrue( validator.test( 1 ) );
    assertTrue( validator.test( 5 ) );
    assertTrue( validator.test( 7 ) );
    assertTrue( validator.test( 11 ) );
    assertTrue( validator.test( 13 ) );
    assertTrue( validator.test( 15 ) );
    assertTrue( validator.test( 20 ) );
    assertTrue( validator.test( 30 ) );
    assertTrue( validator.test( 101 ) );

    assertFalse( validator.test( -1 ) );
    assertFalse( validator.test( 0 ) );
    assertFalse( validator.test( 6 ) );
    assertFalse( validator.test( 12 ) );
    assertFalse( validator.test( 14 ) );
    assertFalse( validator.test( 21 ) );
    assertFalse( validator.test( 29 ) );
  }
}
