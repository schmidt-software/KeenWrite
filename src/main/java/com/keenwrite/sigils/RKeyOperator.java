/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import com.keenwrite.collections.BoundedCache;

/**
 * Converts dot-separated variable names into names compatible with R. That is,
 * {@code variable.name.qualified} becomes {@code v$variable$name$qualified}.
 */
public final class RKeyOperator extends SigilKeyOperator {
  private static final char KEY_SEPARATOR_DEF = '.';
  private static final char KEY_SEPARATOR_R = '$';

  /** Minor optimization to avoid recreating an object. */
  private final StringBuilder mVarName = new StringBuilder( 128 );

  /** Optimization to avoid re-converting variable names into R format. */
  private final BoundedCache<String, String> mVariables = new BoundedCache<>(
    2048
  );

  /**
   * Constructs a new instance capable of converting dot-separated variable
   * names into R's dollar-symbol-separated names.
   */
  public RKeyOperator() {
    // The keys are not delimited.
    super( "", "" );
  }

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
    assert !key.isBlank();

    return mVariables.computeIfAbsent( key, this::convert );
  }

  private String convert( final String key ) {
    mVarName.setLength( 0 );
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
