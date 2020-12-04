/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import javafx.beans.property.StringProperty;

import static com.keenwrite.sigils.YamlSigilOperator.KEY_SEPARATOR_DEF;

/**
 * Brackets variable names between {@link #PREFIX} and {@link #SUFFIX} sigils.
 */
public class RSigilOperator extends SigilOperator {
  public static final char KEY_SEPARATOR_R = '$';

  public static final String PREFIX = "`r#";
  public static final char SUFFIX = '`';

  private final StringProperty mDelimiterBegan =
      getUserPreferences().rDelimiterBeganProperty();
  private final StringProperty mDelimiterEnded =
      getUserPreferences().rDelimiterEndedProperty();

  /**
   * Returns the given string R-escaping backticks prepended and appended. This
   * is not null safe. Do not pass null into this method.
   *
   * @param key The string to adorn with R token delimiters.
   * @return "`r#" + delimiterBegan + variableName+ delimiterEnded + "`".
   */
  @Override
  public String apply( final String key ) {
    assert key != null;

    return PREFIX
        + mDelimiterBegan.getValue()
        + entoken( key )
        + mDelimiterEnded.getValue()
        + SUFFIX;
  }

  /**
   * Transforms a definition key (bracketed by token delimiters) into the
   * expected format for an R variable key name.
   *
   * @param key The variable name to transform, can be empty but not null.
   * @return The transformed variable name.
   */
  public static String entoken( final String key ) {
    return "v$" +
        YamlSigilOperator.detoken( key )
                         .replace( KEY_SEPARATOR_DEF, KEY_SEPARATOR_R );
  }
}
