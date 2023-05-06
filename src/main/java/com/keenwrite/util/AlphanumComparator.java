/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers. Rather than sort numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 * Released under the MIT License - https://opensource.org/licenses/MIT
 *
 * Copyright 2007-2017 David Koelle
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.keenwrite.util;

import java.io.Serializable;
import java.util.Comparator;

import static java.lang.Character.isDigit;

/**
 * Responsible for sorting lists that may contain numeric values. Usage:
 * <pre>
 *   Collections.sort(list, new AlphanumComparator());
 * </pre>
 * <p>
 * Where "list" is the list to sort alphanumerically, not lexicographically.
 * </p>
 */
public final class AlphanumComparator<T> implements
  Comparator<T>, Serializable {

  /**
   * Returns a chunk of text that is continuous with respect to digits or
   * non-digits.
   *
   * @param s      The string to compare.
   * @param length The string length, for improved efficiency.
   * @param marker The current index into a subset of the given string.
   * @return The substring {@code s} that is a continuous text chunk of the
   * same character type.
   */
  private StringBuilder chunk( final String s, final int length, int marker ) {
    assert s != null;
    assert length >= 0;
    assert marker < length;

    // Prevent any possible memory re-allocations by using the length.
    final var chunk = new StringBuilder( length );
    var c = s.charAt( marker );
    final var chunkType = isDigit( c );

    // While the character at the current position is the same type (numeric or
    // alphabetic), append the character to the current chunk.
    while( marker < length &&
      isDigit( c = s.charAt( marker++ ) ) == chunkType ) {
      chunk.append( c );
    }

    return chunk;
  }

  /**
   * Performs an alphanumeric comparison of two strings, sorting numerically
   * first when numbers are found within the string. If either argument is
   * {@code null}, this will return zero.
   *
   * @param o1 The object to compare against {@code s2}, converted to string.
   * @param o2 The object to compare against {@code s1}, converted to string.
   * @return a negative integer, zero, or a positive integer if the first
   * argument is less than, equal to, or greater than the second, respectively.
   */
  @Override
  public int compare( final T o1, final T o2 ) {
    if( o1 == null || o2 == null ) {
      return 0;
    }

    final var s1 = o1.toString();
    final var s2 = o2.toString();
    final var s1Length = s1.length();
    final var s2Length = s2.length();

    var thisMarker = 0;
    var thatMarker = 0;

    while( thisMarker < s1Length && thatMarker < s2Length ) {
      final var thisChunk = chunk( s1, s1Length, thisMarker );
      final var thisChunkLength = thisChunk.length();
      thisMarker += thisChunkLength;
      final var thatChunk = chunk( s2, s2Length, thatMarker );
      final var thatChunkLength = thatChunk.length();
      thatMarker += thatChunkLength;

      // If both chunks contain numeric characters, sort them numerically
      int result;

      if( isDigit( thisChunk.charAt( 0 ) ) &&
        isDigit( thatChunk.charAt( 0 ) ) ) {
        // If equal, the first different number counts
        if( (result = thisChunkLength - thatChunkLength) == 0 ) {
          for( var i = 0; i < thisChunkLength; i++ ) {
            result = thisChunk.charAt( i ) - thatChunk.charAt( i );

            if( result != 0 ) {
              return result;
            }
          }
        }
      }
      else {
        result = thisChunk.compareTo( thatChunk );
      }

      if( result != 0 ) {
        return result;
      }
    }

    return s1Length - s2Length;
  }
}
