/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import java.util.regex.Matcher;

abstract class BasedSequenceParser {
  /**
   * Shared syntax between subclasses.
   */
  static final String REGEX_INNER = "(\\p{Alnum}+):(\\p{Alnum}+)";

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

  abstract Matcher createMatcher( final String text );

  String getTypeName() {
    return mTypeName;
  }

  String getIdName() {
    return mIdName;
  }
}
