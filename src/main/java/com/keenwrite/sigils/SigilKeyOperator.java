/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for bracketing definition keys with token delimiters.
 */
public class SigilKeyOperator implements UnaryOperator<String> {
  private final String mBegan;
  private final String mEnded;
  private final Pattern mPattern;

  public SigilKeyOperator( final String began, final String ended ) {
    assert began != null;
    assert ended != null;

    mBegan = began;
    mEnded = ended;
    mPattern = compile( format( "%s(.*?)%s", quote( began ), quote( ended ) ) );
  }

  @Override
  public String apply( final String key ) {
    assert key != null;
    assert !key.startsWith( mBegan );
    assert !key.endsWith( mEnded );

    return mBegan + key + mEnded;
  }

  public Matcher match( final String text ) {
    return mPattern.matcher( text );
  }
}
