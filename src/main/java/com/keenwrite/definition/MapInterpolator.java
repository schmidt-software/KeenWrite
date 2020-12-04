/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.definition;

import com.keenwrite.sigils.YamlSigilOperator;

import java.util.Map;
import java.util.regex.Matcher;

import static com.keenwrite.sigils.YamlSigilOperator.REGEX_PATTERN;

/**
 * Responsible for performing string interpolation on key/value pairs stored
 * in a map. The values in the map can use a delimited syntax to refer to
 * keys in the map.
 */
public final class MapInterpolator {
  private static final int GROUP_DELIMITED = 1;

  /**
   * Prevent instantiation.
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
  public static Map<String, String> interpolate(
      final Map<String, String> map ) {
    map.replaceAll( ( k, v ) -> resolve( map, v ) );
    return map;
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
