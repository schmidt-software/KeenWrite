/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Pattern;

class BasedSequenceNameParser extends BasedSequenceParser {
  private static final String REGEX = STR. "#\{ REGEX_INNER }" ;
  private static final Pattern PATTERN = asPattern( REGEX );

  private BasedSequenceNameParser( final String text ) {
    super( text );
  }

  static BasedSequenceNameParser parse( final BasedSequence chars ) {
    return new BasedSequenceNameParser( chars.toString() );
  }

  @Override
  Pattern getPattern() {
    return PATTERN;
  }
}
