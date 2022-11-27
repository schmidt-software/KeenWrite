/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import java.util.function.UnaryOperator;

/**
 * Converts dot-separated variable names into names compatible with R. That is,
 * {@code variable.name.qualified} becomes {@code v$variable$name$qualified}.
 */
public final class RKeyOperator implements UnaryOperator<String> {
  private static final char KEY_SEPARATOR_DEF = '.';
  private static final char KEY_SEPARATOR_R = '$';

  private final StringBuilder mVarName = new StringBuilder( 128 );

  /**
   * Constructs a new instance capable of converting dot-separated variable
   * names into R's dollar-symbol-separated names.
   */
  public RKeyOperator() { }

  /**
   * Transforms a definition key name into the expected format for an R
   * variable key name.
   * <p>
   * This algorithm is faster than {@link String#replace(char, char)}. Faster
   * still would be to cache the values, but that would mean managing the
   * cache when the user changes the beginning and ending of the R delimiters.
   * This code gives about a 2% performance boost when scrolling using
   * cursor keys. After the JIT warms up, this super-minor bottleneck vanishes.
   *
   * @param key The variable name to transform, neither blank nor {@code null}.
   * @return The transformed variable name.
   */
  @Override
  public String apply( final String key ) {
    assert key != null;
    assert key.length() > 0;
    assert !key.isBlank();

    mVarName.setLength( 0 );

    //final var rVarName = new StringBuilder( key.length() + 3 );
    mVarName.append( "v" );
    mVarName.append( KEY_SEPARATOR_R );
    mVarName.append( key );

    // The 3 is for v$ + first char, which cannot be a separator.
    for( int i = mVarName.length() - 1; i >= 3; i-- ) {
      if( mVarName.charAt( i ) == KEY_SEPARATOR_DEF ) {
        mVarName.setCharAt( i, KEY_SEPARATOR_R );
      }
    }

    return mVarName.toString();
  }
}
