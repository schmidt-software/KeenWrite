/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.config;

import com.keenwrite.collections.InterpolatingMap;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static java.util.Arrays.*;

/**
 * Responsible for reading and interpolating properties files.
 */
public class PropertiesConfiguration {
  private static final String VALUE_SEPARATOR = ",";

  private final InterpolatingMap mMap = new InterpolatingMap();

  public PropertiesConfiguration() {}

  public void read( final Reader reader ) throws IOException {
    final var properties = new Properties();
    properties.load( reader );

    for( final var name : properties.stringPropertyNames() ) {
      mMap.put( name, properties.getProperty( name ) );
    }

    mMap.interpolate();
  }

  /**
   * Returns the value of a string property.
   *
   * @param property     The property key.
   * @param defaultValue The value to return if no property key has been set.
   * @return The property key value, or defaultValue when no key found.
   */
  public String getString( final String property, final String defaultValue ) {
    assert property != null;

    return mMap.getOrDefault( property, defaultValue );
  }

  /**
   * Returns the value of a string property.
   *
   * @param property     The property key.
   * @param defaultValue The value to return if no property key has been set.
   * @return The property key value, or defaultValue when no key found.
   */
  public int getInt( final String property, final int defaultValue ) {
    assert property != null;

    return parse( mMap.get( property ), defaultValue );
  }

  /**
   * Convert the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @param defaults The values to use should the property be unset.
   * @return The list of properties coerced from objects to strings.
   */
  public List<String> getList(
    final String property, final List<String> defaults ) {
    assert property != null;

    final var value = mMap.get( property );

    return value == null
      ? defaults
      : asList( value.split( VALUE_SEPARATOR ) );
  }

  /**
   * Returns a list of property names that begin with the given prefix.
   * Note that the prefix must be separated from other values with a
   * period.
   *
   * @param prefix The prefix to compare against each property name. When
   *               comparing, the prefix value will have a period appended.
   * @return The list of property names that have the given prefix.
   */
  public Iterator<String> getKeys( final String prefix ) {
    assert prefix != null;

    final var result = new HashMap<String, String>();
    final var prefixDotted = prefix + '.';

    for( final var entry : mMap.entrySet() ) {
      final var key = entry.getKey();

      if( key.startsWith( prefixDotted ) ) {
        final var value = entry.getValue();
        result.put( key, value );
      }
    }

    return result.keySet().iterator();
  }

  private static int parse( final String s, final int defaultValue ) {
    try {
      return s == null || s.isBlank() ? defaultValue : Integer.parseInt( s );
    } catch( final NumberFormatException e ) {
      return defaultValue;
    }
  }
}
