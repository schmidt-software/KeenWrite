/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import static java.lang.Math.max;

/**
 * Scans an array (haystack) for a particular value (needle).
 *
 * <p>
 * This class is {@code null}-hostile.
 */
public class ArrayScanner {

  /**
   * The index value returned when an element is not found in an array.
   */
  public static final int MISSING = -1;

  /**
   * Finds the index of the given needle in the haystack.
   *
   * @param haystack The haystack to search through for the needle.
   * @param needle   The needle to find in the haystack.
   * @return Index of the needle within the haystack, or {@link #MISSING}
   * if not found.
   */
  public static int indexOf( final Object[] haystack, final Object needle ) {
    assert haystack != null;
    assert needle != null;

    return indexOf( haystack, needle, 0 );
  }

  /**
   * Finds the index of the given needle in the haystack.
   *
   * @param haystack The haystack to search through for the needle.
   * @param needle   The needle to find in the haystack.
   * @param offset   The starting offset into the haystack to begin looking
   *                 (the value may be greater than or less than the number
   *                 of array elements).
   * @return Index of the needle within the haystack, or {@link #MISSING}
   * if not found.
   */
  public static int indexOf(
    final Object[] haystack, final Object needle, int offset ) {
    assert haystack != null;
    assert needle != null;

    for( int i = max( 0, offset ); i < haystack.length; i++ ) {
      if( needle.equals( haystack[ i ] ) ) {
        return i;
      }
    }

    return MISSING;
  }

  /**
   * Checks if the object is in the given array.
   *
   * @param haystack The haystack to search through for the needle.
   * @param needle   The needle to find in the haystack.
   * @return {@code true} if the array contains the object.
   */
  public static boolean contains(
    final Object[] haystack, final Object needle ) {
    assert haystack != null;
    assert needle != null;

    return indexOf( haystack, needle ) != MISSING;
  }
}
