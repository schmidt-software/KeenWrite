/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import static com.keenwrite.sigils.YamlSigilOperator.KEY_SEPARATOR_DEF;

/**
 * Brackets variable names between {@link #PREFIX} and {@link #SUFFIX} sigils.
 */
public class RSigilOperator extends SigilOperator {
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
   *
   * @param key The variable name to transform, can be empty but not null.
   * @return The transformed variable name.
   */
  public String entoken( final String key ) {
    return "v$" + mAntecedent.detoken( key )
                             .replace( KEY_SEPARATOR_DEF, KEY_SEPARATOR_R );
  }
}
