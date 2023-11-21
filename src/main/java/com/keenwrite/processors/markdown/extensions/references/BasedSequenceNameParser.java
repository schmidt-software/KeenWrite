/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;

class BasedSequenceNameParser extends BasedSequenceParser {
  private static final String REGEX = STR. "#\{ REGEX_INNER }" ;
  private static final Pattern PATTERN = compile(
    REGEX, UNICODE_CHARACTER_CLASS
  );

  private BasedSequenceNameParser( final String text ) {
    super( text );
  }

  @Override
  Matcher createMatcher( final String text ) {
    return PATTERN.matcher( text );
  }

  static BasedSequenceNameParser parse( final BasedSequence chars ) {
    return new BasedSequenceNameParser( chars.toString() );
  }
}
