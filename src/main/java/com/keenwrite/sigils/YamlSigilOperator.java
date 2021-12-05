/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

/**
 * Brackets definition keys with token delimiters.
 */
public final class YamlSigilOperator extends SigilOperator {
  public static final char KEY_SEPARATOR_DEF = '.';

  public YamlSigilOperator( final Sigils sigils ) {
    super( sigils );
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
   * Removes start and stop definition key delimiters from the given key.
   *
   * @param key The key that may have start and stop tokens.
   * @return The given key with the delimiters removed.
   */
  public String detoken( final String key ) {
    final var began = getBegan();
    final var ended = getEnded();
    final int bLength = began.length();
    final int eLength = ended.length();
    final var bIndex = key.indexOf( began );
    final var eIndex = key.indexOf( ended, bIndex );
    final var kLength = key.length();

    return key.substring(
      bIndex == -1 ? 0 : bLength, eIndex == -1 ? kLength : kLength - eLength );
  }
}
