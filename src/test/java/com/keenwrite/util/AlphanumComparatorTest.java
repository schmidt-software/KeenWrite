/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for testing the http://www.davekoelle.com/alphanum.html
 * implementation.
 */
class AlphanumComparatorTest {

  /**
   * Test that a randomly sorted list containing a mix of alphanumeric
   * characters ("chunks") will be sorted according to numeric and alphabetic
   * order.
   */
  @Test
  public void test_Sort_UnsortedList_SortedAlphanumerically() {
    final var expected = Arrays.asList(
      "10X Radonius",
      "20X Radonius",
      "20X Radonius Prime",
      "30X Radonius",
      "40X Radonius",
      "200X Radonius",
      "1000X Radonius Maximus",
      "Allegia 6R Clasteron",
      "Allegia 50 Clasteron",
      "Allegia 50B Clasteron",
      "Allegia 51 Clasteron",
      "Allegia 500 Clasteron",
      "Alpha 2",
      "Alpha 2A",
      "Alpha 2A-900",
      "Alpha 2A-8000",
      "Alpha 100",
      "Alpha 200",
      "Callisto Morphamax",
      "Callisto Morphamax 500",
      "Callisto Morphamax 600",
      "Callisto Morphamax 700",
      "Callisto Morphamax 5000",
      "Callisto Morphamax 6000 SE",
      "Callisto Morphamax 6000 SE2",
      "Callisto Morphamax 7000",
      "Xiph Xlater 5",
      "Xiph Xlater 40",
      "Xiph Xlater 50",
      "Xiph Xlater 58",
      "Xiph Xlater 300",
      "Xiph Xlater 500",
      "Xiph Xlater 2000",
      "Xiph Xlater 5000",
      "Xiph Xlater 10000"
    );
    final var actual = new ArrayList<>( expected );

    Collections.shuffle( actual );
    actual.sort( new AlphanumComparator<>() );
    assertEquals( expected, actual );
  }
}
