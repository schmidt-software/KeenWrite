/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service;

import java.util.Iterator;
import java.util.List;

/**
 * Defines how settings and options can be retrieved.
 */
public interface Settings extends Service {

  /**
   * Returns a setting property or its default value.
   *
   * @param property     The property key name to obtain its value.
   * @param defaultValue The default value to return iff the property cannot be
   *                     found.
   * @return The property value for the given property key.
   */
  String getSetting( String property, String defaultValue );

  /**
   * Returns a setting property or its default value.
   *
   * @param property     The property key name to obtain its value.
   * @param defaultValue The default value to return iff the property cannot be
   *                     found.
   * @return The property value for the given property key.
   */
  int getSetting( String property, int defaultValue );

  /**
   * Returns a list of property names that begin with the given prefix. The
   * prefix is included in any matching results. This will return keys that
   * either match the prefix or start with the prefix followed by a dot ('.').
   * For example, a prefix value of <code>the.property.name</code> will likely
   * return the expected results, but <code>the.property.name.</code> (note the
   * extraneous period) will probably not.
   *
   * @param prefix The prefix to compare against each property name.
   * @return The list of property names that have the given prefix.
   */
  Iterator<String> getKeys( final String prefix );

  /**
   * Convert the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @param defaults The defaults values to use should the property be unset.
   * @return The list of properties coerced from objects to strings.
   */
  List<String> getStringSettingList( String property, List<String> defaults );

  /**
   * Converts the generic list of property objects into strings.
   *
   * @param property The property value to coerce.
   * @return The list of properties coerced from objects to strings.
   */
  List<String> getStringSettingList( String property );
}
