/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import javafx.beans.property.SimpleStringProperty;

import java.util.function.UnaryOperator;

/**
 * Responsible for updating definition keys to use a machine-readable format
 * corresponding to the type of file being edited. This changes a definition
 * key name based on some criteria determined by the factory that creates
 * implementations of this interface.
 */
public class SigilOperator implements UnaryOperator<String> {
  private final Sigils mSigils;

  /**
   * Defines a new {@link SigilOperator} with the given sigils.
   *
   * @param began The sigil that denotes the start of a variable name.
   * @param ended The sigil that denotes the end of a variable name.
   */
  public SigilOperator( final String began, final String ended ) {
    this( new Sigils(
      new SimpleStringProperty( began ),
      new SimpleStringProperty( ended )
    ) );
  }

  SigilOperator( final Sigils sigils ) {
    mSigils = sigils;
  }

  /**
   * Returns the given {@link String} verbatim. Different implementations
   * can override to inject custom behaviours.
   *
   * @param key Returned verbatim.
   */
  @Override
  public String apply( final String key ) {
    return key;
  }

  /**
   * Wraps the given key in the began and ended tokens. This may perform any
   * preprocessing necessary to ensure the transformation happens.
   *
   * @param key The variable name to transform.
   * @return The given key with before/after sigils to delimit the key name.
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
   * @param key The key adorned with start and stop tokens.
   * @return The given key with the delimiters removed.
   */
  public String detoken( final String key ) {
    return key;
  }

  public Sigils getSigils() {
    return mSigils;
  }

  String getBegan() {
    return mSigils.getBegan();
  }

  String getEnded() {
    return mSigils.getEnded();
  }
}
