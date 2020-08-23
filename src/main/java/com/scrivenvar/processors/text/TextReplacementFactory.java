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
package com.scrivenvar.processors.text;

import java.util.Map;

/**
 * Used to generate a class capable of efficiently replacing variable
 * definitions with their values.
 */
public final class TextReplacementFactory {

  private final static TextReplacer APACHE = new StringUtilsReplacer();
  private final static TextReplacer AHO_CORASICK = new AhoCorasickReplacer();

  /**
   * Returns a text search/replacement instance that is reasonably optimal for
   * the given length of text.
   *
   * @param length The length of text that requires some search and replacing.
   * @return A class that can search and replace text with utmost expediency.
   */
  public static TextReplacer getTextReplacer( final int length ) {
    // After about 1,500 characters, the StringUtils implementation is less
    // performant than the Aho-Corsick implementation.
    //
    // See http://stackoverflow.com/a/40836618/59087
    return length < 1500 ? APACHE : AHO_CORASICK;
  }

  /**
   * Convenience method to instantiate a suitable text replacer algorithm and
   * perform a replacement using the given map. At this point, the values should
   * be already dereferenced and ready to be substituted verbatim; any
   * recursively defined values must have been interpolated previously.
   *
   * @param text The text containing zero or more variables to replace.
   * @param map  The map of variables to their dereferenced values.
   * @return The text with all variables replaced.
   */
  public static String replace(
      final String text, final Map<String, String> map ) {
    return getTextReplacer( text.length() ).replace( text, map );
  }
}
