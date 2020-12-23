/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

/**
 * Brackets definition keys with token delimiters.
 */
public class YamlSigilOperator extends SigilOperator {
  public static final char KEY_SEPARATOR_DEF = '.';

  public YamlSigilOperator( final Tokens tokens ) {
    super( tokens );
  }

  /**
   * Returns the given {@link String} verbatim because variables in YAML
   * documents and plain Markdown documents already have the appropriate
   * tokenizable syntax wrapped around the text.
   *
   * @param key Returned verbatim.
   */
  @Override
  public String apply( final String key ) {
    return key;
  }

  /**
   * Adds delimiters to the given key.
   *
   * @param key The key to adorn with start and stop definition tokens.
   * @return The given key bracketed by definition token symbols.
   */
  public String entoken( final String key ) {
    assert key != null;
    return getBegan() + key + getEnded();
  }

  /**
   * Removes start and stop definition key delimiters from the given key. This
   * method does not check for delimiters, only that there are sufficient
   * characters to remove from either end of the given key.
   *
   * @param key The key adorned with start and stop definition tokens.
   * @return The given key with the delimiters removed.
   */
  public String detoken( final String key ) {
    final int beganLen = getBegan().length();
    final int endedLen = getEnded().length();

    return key.length() > beganLen + endedLen
      ? key.substring( beganLen, key.length() - endedLen )
      : key;
  }
}
