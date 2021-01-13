/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import static com.keenwrite.sigils.YamlSigilOperator.KEY_SEPARATOR_DEF;

/**
 * Brackets variable names between {@link #PREFIX} and {@link #SUFFIX} sigils.
 */
public final class RSigilOperator extends SigilOperator {
  public static final char KEY_SEPARATOR_R = '$';

  public static final String PREFIX = "`r#";
  public static final char SUFFIX = '`';

  /**
   * Definition variables are inserted into the document before R variables,
   * so this is required to reformat the definition variable suitable for R.
   */
  private final SigilOperator mAntecedent;

  public RSigilOperator( final Tokens tokens, final SigilOperator antecedent ) {
    super( tokens );

    mAntecedent = antecedent;
  }

  /**
   * Returns the given string R-escaping backticks prepended and appended. This
   * is not null safe. Do not pass null into this method.
   *
   * @param key The string to adorn with R token delimiters.
   * @return PREFIX + delimiterBegan + variableName+ delimiterEnded + SUFFIX.
   */
  @Override
  public String apply( final String key ) {
    assert key != null;
    return PREFIX + getBegan() + entoken( key ) + getEnded() + SUFFIX;
  }

  /**
   * Transforms a definition key (bracketed by token delimiters) into the
   * expected format for an R variable key name.
   * <p>
   * The algorithm to entoken a definition name is faster than
   * {@link String#replace(char, char)}. Faster still would be to cache the
   * values, but that would mean managing the cache when the user changes
   * the beginning and ending of the R delimiters. This code gives about a
   * 2% performance boost when scrolling using cursor keys. After the JIT
   * warms up, this super-minor bottleneck vanishes.
   * </p>
   *
   * @param key The variable name to transform, can be empty but not null.
   * @return The transformed variable name.
   */
  public String entoken( final String key ) {
    final var detokened = new StringBuilder( key.length() );
    detokened.append( "v$" );
    detokened.append( mAntecedent.detoken( key ) );

    // The 3 is for "v$X" where X cannot be a period.
    for( int i = detokened.length() - 1; i >= 3; i-- ) {
      if( detokened.charAt( i ) == KEY_SEPARATOR_DEF ) {
        detokened.setCharAt( i, KEY_SEPARATOR_R );
      }
    }

    return detokened.toString();
  }
}
