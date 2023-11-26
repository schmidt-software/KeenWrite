/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;

abstract class BasedSequenceParser {
  /**
   * Shared syntax between subclasses: a letter followed by zero or more
   * alphanumeric characters.
   */
  static final String REGEX_INNER =
    "(\\p{Alpha}[\\p{Alnum}-_]+):(\\p{Alpha}[\\p{Alnum}-_]+)";

  private final String mTypeName;
  private final String mIdName;

  BasedSequenceParser( final String text ) {
    final var matcher = createMatcher( text );

    if( matcher.find() ) {
      mTypeName = matcher.group( 1 );
      mIdName = matcher.group( 2 );
    }
    else {
      mTypeName = null;
      mIdName = null;
    }
  }

  static Pattern asPattern( final String regex ) {
    return Pattern.compile( regex, UNICODE_CHARACTER_CLASS );
  }

  abstract Pattern getPattern();

  /**
   * Creates a regular expression pattern matcher that can extract the
   * reference elements from text.
   *
   * @param text The text containing an anchor or cross-reference to an anchor.
   * @return The {@link Matcher} to use when extracting the text elements.
   */
  Matcher createMatcher( final String text ) {
    return getPattern().matcher( text );
  }

  String getTypeName() {
    return mTypeName;
  }

  String getIdName() {
    return mIdName;
  }
}
