/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.scrivenvar.definition;

import com.scrivenvar.sigils.YamlSigilOperator;

import java.util.Map;
import java.util.regex.Matcher;

import static com.scrivenvar.sigils.YamlSigilOperator.REGEX_PATTERN;

/**
 * Responsible for performing string interpolation on key/value pairs stored
 * in a map. The values in the map can use a delimited syntax to refer to
 * keys in the map.
 */
public class MapInterpolator {
  private static final int GROUP_DELIMITED = 1;

  /**
   * Empty.
   */
  private MapInterpolator() {
  }

  /**
   * Performs string interpolation on the values in the given map. This will
   * change any value in the map that contains a variable that matches
   * {@link YamlSigilOperator#REGEX_PATTERN}.
   *
   * @param map Contains values that represent references to keys.
   */
  public static void interpolate( final Map<String, String> map ) {
    map.replaceAll( ( k, v ) -> resolve( map, v ) );
  }

  /**
   * Given a value with zero or more key references, this will resolve all
   * the values, recursively. If a key cannot be dereferenced, the value will
   * contain the key name.
   *
   * @param map   Map to search for keys when resolving key references.
   * @param value Value containing zero or more key references
   * @return The given value with all embedded key references interpolated.
   */
  private static String resolve(
      final Map<String, String> map, String value ) {
    final Matcher matcher = REGEX_PATTERN.matcher( value );

    while( matcher.find() ) {
      final String keyName = matcher.group( GROUP_DELIMITED );
      final String mapValue = map.get( keyName );
      final String keyValue = mapValue == null
          ? keyName
          : resolve( map, mapValue );

      value = value.replace( keyName, keyValue );
    }

    return value;
  }
}
