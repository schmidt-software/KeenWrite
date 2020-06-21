/*
 * Copyright 2016 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.processors;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the keys of the resolved map from default form to R form, then
 * performs a substitution on the text. The default R variable syntax is
 * {@code v$tree$leaf}.
 *
 * @author White Magic Software, Ltd.
 */
public class RVariableProcessor extends DefinitionProcessor {

  public RVariableProcessor(
      final Processor<String> rp, final Map<String, String> map ) {
    super( rp, map );
  }

  /**
   * Returns the R-based version of the interpolated variable definitions.
   *
   * @return Variable names transmogrified from the default syntax to R syntax.
   */
  @Override
  protected Map<String, String> getDefinitions() {
    return toR( super.getDefinitions() );
  }

  /**
   * Converts the given map from regular variables to R variables.
   *
   * @param map Map of variable names to values.
   * @return Map of R variables.
   */
  private Map<String, String> toR( final Map<String, String> map ) {
    final Map<String, String> rMap = new HashMap<>( map.size() );

    for( final Map.Entry<String, String> entry : map.entrySet() ) {
      final var key = entry.getKey();
      rMap.put( toRKey( key ), toRValue( map.get( key ) ) );
    }

    return rMap;
  }

  /**
   * Transforms a variable name from $tree.branch.leaf$ to v$tree$branch$leaf
   * form.
   *
   * @param key The variable name to transform, can be empty but not null.
   * @return The transformed variable name.
   */
  private String toRKey( final String key ) {
    // Replace all the periods with dollar symbols.
    final StringBuilder sb = new StringBuilder( 'v' + key );
    final int length = sb.length();

    // Replace all periods with dollar symbols. Normally we'd check i >= 0,
    // but the prepended 'v' is always going to be a 'v', not a dot.
    for( int i = length - 1; i > 0; i-- ) {
      if( sb.charAt( i ) == '.' ) {
        sb.setCharAt( i, '$' );
      }
    }

    // The length is always at least 1 (the 'v'), so bounds aren't broken here.
    sb.setLength( length - 1 );

    return sb.toString();
  }

  private String toRValue( final String value ) {
    return '\'' + escape( value, '\'', "\\'" ) + '\'';
  }

  /**
   * TODO: Make generic method for replacing text.
   *
   * @param haystack Search this string for the needle, must not be null.
   * @param needle   The character to find in the haystack.
   * @param thread   Replace the needle with this text, if the needle is found.
   * @return The haystack with the all instances of needle replaced with thread.
   */
  @SuppressWarnings("SameParameterValue")
  private String escape(
      final String haystack, final char needle, final String thread ) {
    int end = haystack.indexOf( needle );

    if( end < 0 ) {
      return haystack;
    }

    final int length = haystack.length();
    int start = 0;

    // Replace up to 32 occurrences before the string reallocates its buffer.
    final StringBuilder sb = new StringBuilder( length + 32 );

    while( end >= 0 ) {
      sb.append( haystack, start, end ).append( thread );
      start = end + 1;
      end = haystack.indexOf( needle, start );
    }

    return sb.append( haystack.substring( start ) ).toString();
  }
}
