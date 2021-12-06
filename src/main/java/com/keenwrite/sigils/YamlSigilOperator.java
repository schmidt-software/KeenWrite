/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

/**
 * Responsible for bracketing definition keys with token delimiters.
 */
public final class YamlSigilOperator extends SigilOperator {
  public YamlSigilOperator( final Sigils sigils ) {
    super( sigils );
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
