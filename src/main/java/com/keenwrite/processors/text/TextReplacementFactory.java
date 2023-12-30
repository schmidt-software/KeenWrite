/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import com.keenwrite.util.Strings;

import java.util.Map;

/**
 * Used to generate a class capable of efficiently replacing variable
 * definitions with their values.
 */
public final class TextReplacementFactory {

  private static final TextReplacer APACHE = new StringUtilsReplacer();
  private static final TextReplacer AHO_CORASICK = new AhoCorasickReplacer();

  /**
   * Returns a text search/replacement instance that is reasonably optimal for
   * the given length of text.
   *
   * @param length The length of text that requires some search and replacing.
   * @return A class that can search and replace text with utmost expediency.
   */
  public static TextReplacer getTextReplacer( final int length ) {
    // After about 1,500 characters, the Aho-Corsick algorithm is faster.
    return length < 1500 ? APACHE : AHO_CORASICK;
  }

  /**
   * Convenience method to instantiate a suitable text replacer algorithm and
   * perform a replacement using the given map. At this point, the values should
   * be already dereferenced and ready to be substituted verbatim; any
   * recursively defined values must have been interpolated previously.
   *
   * @param text    The text containing zero or more variables to replace.
   * @param needles The map of variables to their dereferenced values.
   * @return The text with all variables replaced.
   */
  public static String replace(
    final String text, final Map<String, String> needles ) {
    final String haystack = Strings.sanitize( text );
    assert needles != null;

    return getTextReplacer( haystack.length() ).replace( haystack, needles );
  }
}
