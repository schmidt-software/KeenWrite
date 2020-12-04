/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import java.util.Map;

/**
 * Defines the ability to replace text given a set of keys and values.
 */
public interface TextReplacer {

  /**
   * Searches through the given text for any of the keys given in the map and
   * replaces the keys that appear in the text with the key's corresponding
   * value.
   *
   * @param text The text that contains zero or more keys.
   * @param map  The set of keys mapped to replacement values.
   * @return The given text with all keys replaced with corresponding values.
   */
  String replace( String text, Map<String, String> map );
}
