/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Responsible for answering whether a given integer value falls within a
 * set of range specifiers. For example, if the range is "1-3, 5, 7-9, 11-",
 * then values of 0, 4, and 10 return {@code false} while values of 2, 5,
 * and 37 would return {@code true}.
 */
public final class RangeValidator implements Predicate<Integer> {

  /**
   * Container for a pair of integer values that can answer whether a given
   * value is included within the bounds provided by the pair.
   */
  private static class Range {
    private final int mLo;
    private final int mHi;

    private Range( final int lo, final int hi ) {
      assert lo <= hi;

      mLo = lo;
      mHi = hi;
    }

    private boolean includes( final int i ) {
      return mLo <= i && i <= mHi || mLo == -1 && mHi == -1;
    }
  }

  private final List<Range> mRanges = new ArrayList<>();

  /**
   * Creates an instance of {@link RangeValidator} that can verify whether
   * an integer value will fall within one of the numeric ranges in the
   * given listing.
   *
   * @param range The listing of ranges to validate against.
   */
  public RangeValidator( final String range ) {
    assert normalize( range ).equals( range );

    parse( range );
  }

  @Override
  public boolean test( final Integer integer ) {
    for( final var range : mRanges ) {
      if( range.includes( integer ) ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Given a string meant to represent a comma-separated range of numbers,
   * this will ensure that the range meets the formatting requirements.
   *
   * @param range The sequences to validate (can be {@code null}).
   * @return The given range with all non-conforming characters removed, or
   * the empty string if {@code null} was provided.
   */
  public static String normalize( final String range ) {
    return range == null
      ? ""
      : range.matches( "^\\d+(-\\d+)?(?:,\\d+(?:-\\d+)?)*+$" )
      ? range
      : range.replaceAll( "[^-,\\d\\s]", "" );
  }

  /**
   * Populates the internal list of {@link Range} instances.
   *
   * @param s The string containing zero or more comma-separated integer
   *          ranges, themselves separated by hyphens.
   */
  private void parse( final String s ) {
    for( final var commaRange : normalize( s ).split( "," ) ) {
      final var hyphenRanges = commaRange.split( "-" );
      final Range range;

      if( hyphenRanges.length == 2 ) {
        final var hrlo = hyphenRanges[ 0 ].trim();
        final var hrhi = hyphenRanges[ 1 ].trim();

        if( hrlo.isEmpty() ) {
          range = new Range( 1, Integer.parseInt( hrhi ) );
        }
        else {
          final var lo = Integer.parseInt( hrlo );
          final var hi = Integer.parseInt( hrhi );

          range = new Range( lo, hi );
        }
      }
      else if( hyphenRanges.length == 1 ) {
        final var hri = hyphenRanges[ 0 ].trim();

        if( hri.isEmpty() ) {
          // Special case for all numbers being valid.
          range = new Range( -1, -1 );
        }
        else {
          final var i = Integer.parseInt( hyphenRanges[ 0 ].trim() );
          final var index = commaRange.trim().indexOf( '-' );

          // If the hyphen is to the left of the number, the range is bounded
          // from 0 to the number. Otherwise, the range is "unbounded" starting
          // at the number.
          if( index == -1 ) {
            range = new Range( i, i );
          }
          else if( index == 0 ) {
            range = new Range( 1, i );
          }
          else {
            range = new Range( i, Integer.MAX_VALUE );
          }
        }
      }
      else {
        // Ignore the range.
        range = new Range( 0, 0 );
      }

      mRanges.add( range );
    }
  }
}
