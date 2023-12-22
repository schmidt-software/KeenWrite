/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keenwrite.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Character.isWhitespace;
import static java.lang.String.format;

/**
 * Java doesn't allow adding behaviour to its {@link String} class, so these
 * functions have no alternative home. They are duplicated here to eliminate
 * the dependency on an Apache library. Extracting the methods that only
 * the application uses may have some small performance gains, as well,
 * because numerous if clauses have been removed and other code simplified.
 */
public class Strings {
  /**
   * The empty String {@code ""}.
   */
  private static final String EMPTY = "";

  /**
   * Abbreviates a String using ellipses. This will turn
   * "Now is the time for all good men" into "Now is the time for..."
   *
   * @param str   the String to check, may be {@code null}.
   * @param width maximum length of result String, must be at least 4.
   * @return abbreviated String, {@code null} if {@code null} String input.
   * @throws IllegalArgumentException if the width is too small.
   */
  public static String abbreviate( final String str, final int width ) {
    return abbreviate( str, "...", 0, width );
  }

  /**
   * Abbreviates a String using another given String as replacement marker.
   * This will turn"Now is the time for all good men" into "Now is the time
   * for..." if "..." was defined as the replacement marker.
   *
   * @param str        the String to check, may be {@code null}.
   * @param abbrMarker the String used as replacement marker.
   * @param width      maximum length of result String, must be at least
   *                   {@code abbrMarker.length + 1}.
   * @return abbreviated String, {@code null} if {@code null} String input.
   * @throws IllegalArgumentException if the width is too small.
   */
  public static String abbreviate(
    final String str,
    final String abbrMarker,
    final int width ) {
    return abbreviate( str, abbrMarker, 0, width );
  }

  /**
   * Abbreviates a String using a given replacement marker. This will turn
   * "Now is the time for all good men" into "...is the time for..." if "..."
   * was defined as the replacement marker.
   *
   * @param str        the String to check, may be {@code null}.
   * @param abbrMarker the String used as replacement marker.
   * @param offset     left edge of source String.
   * @param width      maximum length of result String, must be at least 4.
   * @return abbreviated String, {@code null} if {@code null} String input.
   * @throws IllegalArgumentException if the width is too small.
   */
  public static String abbreviate(
    final String str,
    final String abbrMarker,
    int offset,
    final int width ) {
    if( !isEmpty( str ) && EMPTY.equals( abbrMarker ) && width > 0 ) {
      return substring( str, width );
    }

    if( isAnyEmpty( str, abbrMarker ) ) {
      return str;
    }

    final int abbrMarkerLen = abbrMarker.length();
    final int minAbbrWidth = abbrMarkerLen + 1;
    final int minAbbrWidthOffset = abbrMarkerLen + abbrMarkerLen + 1;

    if( width < minAbbrWidth ) {
      final String msg = format( "Min abbreviation width: %d", minAbbrWidth );
      throw new IllegalArgumentException( msg );
    }

    final int strLen = str.length();

    if( strLen <= width ) {
      return str;
    }

    if( offset > strLen ) {
      offset = strLen;
    }

    if( strLen - offset < width - abbrMarkerLen ) {
      offset = strLen - (width - abbrMarkerLen);
    }

    if( offset <= abbrMarkerLen + 1 ) {
      return str.substring( 0, width - abbrMarkerLen ) + abbrMarker;
    }

    if( width < minAbbrWidthOffset ) {
      final String msg = format(
        "Min abbreviation width with offset: %d",
        minAbbrWidthOffset
      );
      throw new IllegalArgumentException( msg );
    }

    if( offset + width - abbrMarkerLen < strLen ) {
      return abbrMarker + abbreviate(
        str.substring( offset ),
        abbrMarker,
        width - abbrMarkerLen
      );
    }

    return abbrMarker + str.substring( strLen - (width - abbrMarkerLen) );
  }

  /**
   * Strips whitespace characters from the end of a String.
   *
   * <p>A {@code null} input String returns {@code null}.
   * An empty string ("") input returns the empty string.</p>
   *
   * @param str the String to remove characters from, may be {@code null}.
   * @return the stripped String, {@code null} if {@code null} input.
   */
  public static String trimEnd( final String str ) {
    int end = length( str );

    if( end == 0 ) {
      return str;
    }

    while( end != 0 && isWhitespace( str.charAt( end - 1 ) ) ) {
      end--;
    }

    return str.substring( 0, end );
  }

  /**
   * Strips whitespace characters from the start of a String.
   *
   * <p>A {@code null} input returns {@code null}.
   * An empty string ("") input returns the empty string.</p>
   *
   * @param str the String to remove characters from, may be {@code null}.
   * @return the stripped String, {@code null} if {@code null} input.
   */
  public static String trimStart( final String str ) {
    final int strLen = length( str );

    if( strLen == 0 ) {
      return str;
    }

    int start = 0;

    while( start != strLen && isWhitespace( str.charAt( start ) ) ) {
      start++;
    }

    return str.substring( start );
  }

  /**
   * Replaces all occurrences of Strings within another String.
   *
   * @param text            the haystack, no-op if {@code null}.
   * @param searchList      the needles, no-op if {@code null}.
   * @param replacementList the new needles, no-op if {@code null}.
   * @return the text with any replacements processed, {@code null} if
   * {@code null} String input.
   * @throws IllegalArgumentException if the lengths of the arrays are not
   *                                  the same ({@code null}  is ok, and/or
   *                                  size 0).
   */
  public static String replaceEach( final String text,
                                    final String[] searchList,
                                    final String[] replacementList ) {
    return replaceEach( text, searchList, replacementList, 0 );
  }

  /**
   * Replace all occurrences of Strings within another String.
   *
   * @param text            the haystack, no-op if {@code null}.
   * @param searchList      the needles, no-op if {@code null}.
   * @param replacementList the new needles, no-op if {@code null}.
   * @param timeToLive      if less than 0 then there is a circular reference
   *                        and endless loop
   * @return the text with any replacements processed, {@code null} if
   * {@code null} String input.
   * @throws IllegalStateException    if the search is repeating and there is
   *                                  an endless loop due to outputs of one
   *                                  being inputs to another
   * @throws IllegalArgumentException if the lengths of the arrays are not
   *                                  the same ({@code null} is ok, and/or
   *                                  size 0)
   */
  private static String replaceEach(
    final String text,
    final String[] searchList,
    final String[] replacementList,
    final int timeToLive
  ) {
    // If in a recursive call, this shouldn't be less than zero.
    if( timeToLive < 0 ) {
      final Set<String> searchSet =
        new HashSet<>( Arrays.asList( searchList ) );
      final Set<String> replacementSet = new HashSet<>( Arrays.asList(
        replacementList ) );
      searchSet.retainAll( replacementSet );
      if( !searchSet.isEmpty() ) {
        throw new IllegalStateException(
          "Aborting to protect against StackOverflowError - " +
          "output of one loop is the input of another" );
      }
    }

    if( isEmpty( text ) ||
        isEmpty( searchList ) ||
        isEmpty( replacementList ) ||
        isNotEmpty( searchList ) &&
        timeToLive == -1 ) {
      return text;
    }

    final int searchLength = searchList.length;
    final int replacementLength = replacementList.length;

    // make sure lengths are ok, these need to be equal
    if( searchLength != replacementLength ) {
      final String msg = format(
        "Search and Replace array lengths don't match: %d vs %d",
        searchLength,
        replacementLength
      );
      throw new IllegalArgumentException( msg );
    }

    // keep track of which still have matches
    final boolean[] noMoreMatchesForReplIndex = new boolean[ searchLength ];

    // index on index that the match was found
    int textIndex = -1;
    int replaceIndex = -1;
    int tempIndex;

    // index of replace array that will replace the search string found
    // NOTE: logic duplicated below START
    for( int i = 0; i < searchLength; i++ ) {
      if( noMoreMatchesForReplIndex[ i ] || isEmpty( searchList[ i ] ) || replacementList[ i ] == null ) {
        continue;
      }
      tempIndex = text.indexOf( searchList[ i ] );

      // see if we need to keep searching for this
      if( tempIndex == -1 ) {
        noMoreMatchesForReplIndex[ i ] = true;
      }
      else if( textIndex == -1 || tempIndex < textIndex ) {
        textIndex = tempIndex;
        replaceIndex = i;
      }
    }
    // NOTE: logic mostly below END

    // no search strings found, we are done
    if( textIndex == -1 ) {
      return text;
    }

    int start = 0;

    // Guess the result buffer size, to prevent doubling capacity.
    final StringBuilder buf = createStringBuilder(
      text, searchList, replacementList
    );

    while( textIndex != -1 ) {
      for( int i = start; i < textIndex; i++ ) {
        buf.append( text.charAt( i ) );
      }

      buf.append( replacementList[ replaceIndex ] );

      start = textIndex + searchList[ replaceIndex ].length();

      textIndex = -1;
      replaceIndex = -1;

      // find the next earliest match
      // NOTE: logic mostly duplicated above START
      for( int i = 0; i < searchLength; i++ ) {
        if( noMoreMatchesForReplIndex[ i ] || isEmpty( searchList[ i ] ) || replacementList[ i ] == null ) {
          continue;
        }
        tempIndex = text.indexOf( searchList[ i ], start );

        // see if we need to keep searching for this
        if( tempIndex == -1 ) {
          noMoreMatchesForReplIndex[ i ] = true;
        }
        else if( textIndex == -1 || tempIndex < textIndex ) {
          textIndex = tempIndex;
          replaceIndex = i;
        }
      }

      // NOTE: logic duplicated above END
    }

    final int textLength = text.length();
    for( int i = start; i < textLength; i++ ) {
      buf.append( text.charAt( i ) );
    }

    return replaceEach(
      buf.toString(),
      searchList,
      replacementList,
      timeToLive - 1
    );
  }

  private static StringBuilder createStringBuilder(
    final String text,
    final String[] searchList,
    final String[] replacementList ) {
    int increase = 0;

    // count the replacement text elements that are larger than their
    // corresponding text being replaced
    for( int i = 0; i < searchList.length; i++ ) {
      if( searchList[ i ] == null || replacementList[ i ] == null ) {
        continue;
      }
      final int greater =
        replacementList[ i ].length() - searchList[ i ].length();
      if( greater > 0 ) {
        increase += 3 * greater; // assume 3 matches
      }
    }

    // have upper-bound at 20% increase, then let Java take over
    increase = Math.min( increase, text.length() / 5 );

    return new StringBuilder( text.length() + increase );
  }

  /**
   * Gets a {@link CharSequence} length or {@code 0} if the
   * {@link CharSequence} is {@code null}.
   *
   * @param cs a {@link CharSequence} or {@code null}.
   * @return {@link CharSequence} length or {@code 0} if the
   * {@link CharSequence} is {@code null}.
   */
  private static int length( final CharSequence cs ) {
    return cs == null ? 0 : cs.length();
  }

  /**
   * Checks if a {@link CharSequence} is empty ("") or {@code null}.
   *
   * @param cs the {@link CharSequence} to check, may be {@code null}.
   * @return {@code true} if the {@link CharSequence} is empty or {@code null}.
   */
  public static boolean isEmpty( final CharSequence cs ) {
    return cs == null || cs.isEmpty();
  }

  private static boolean isEmpty( final Object[] array ) {
    return array == null || Array.getLength( array ) == 0;
  }

  private static boolean isNotEmpty( final Object[] array ) {
    return array != null && Array.getLength( array ) > 0;
  }

  private static boolean isAnyEmpty( final CharSequence... css ) {
    if( isNotEmpty( css ) ) {
      for( final CharSequence cs : css ) {
        if( isEmpty( cs ) ) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Gets a substring from the specified String avoiding exceptions.
   *
   * <p>A negative start position can be used to start/end {@code n}
   * characters from the end of the String.</p>
   *
   * <p>The returned substring starts with the character in the {@code start}
   * position and ends before the {@code end} position. All position counting
   * is zero-based -- i.e., to start at the beginning of the string use
   * {@code start = 0}. Negative start and end positions can be used to
   * specify offsets relative to the end of the String.</p>
   *
   * <p>If {@code start} is not strictly to the left of {@code end}, ""
   * is returned.</p>
   *
   * @param str the String to get the substring from, may be {@code null}.
   * @param end the position to end at (exclusive), negative means
   *            count back from the end of the String by this many characters
   * @return substring from start position to end position, {@code null} if
   * {@code null} String input
   */
  private static String substring( final String str, int end ) {
    if( str == null ) {
      return null;
    }

    final int len = str.length();

    if( end < 0 ) {
      end = len + end;
    }

    if( end > len ) {
      end = len;
    }

    final int start = 0;

    if( start > end ) {
      return EMPTY;
    }

    return str.substring( start, end );
  }
}
