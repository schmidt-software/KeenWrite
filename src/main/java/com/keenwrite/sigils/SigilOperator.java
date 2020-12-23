/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import java.util.function.UnaryOperator;

/**
 * Responsible for updating definition keys to use a machine-readable format
 * corresponding to the type of file being edited. This changes a definition
 * key name based on some criteria determined by the factory that creates
 * implementations of this interface.
 */
public abstract class SigilOperator implements UnaryOperator<String> {
  private final Tokens mTokens;

  SigilOperator( final Tokens tokens ) {
    mTokens = tokens;
  }

  /**
   * Removes start and stop definition key delimiters from the given key. This
   * method does not check for delimiters, only that there are sufficient
   * characters to remove from either end of the given key.
   *
   * @param key The key adorned with start and stop tokens.
   * @return The given key with the delimiters removed.
   */
  String detoken( final String key ) {
    return key;
  }

  String getBegan() {
    return mTokens.getBegan();
  }

  String getEnded() {
    return mTokens.getEnded();
  }

  /**
   * Wraps the given key in the began and ended tokens. This may perform any
   * preprocessing necessary to ensure the transformation happens.
   *
   * @param key The variable name to transform.
   * @return The given key with tokens to delimit it (from the edited text).
   */
  public abstract String entoken( final String key );
}
