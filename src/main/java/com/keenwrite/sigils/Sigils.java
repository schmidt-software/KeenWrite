/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Convenience class for pairing a start and an end sigil together.
 */
public final class Sigils extends SimpleImmutableEntry<String, String> {

  /**
   * Associates a new key-value pair.
   *
   * @param began The starting sigil.
   * @param ended The ending sigil.
   */
  public Sigils( final String began, final String ended ) {
    super( began, ended );
  }

  /**
   * @return The opening sigil token.
   */
  public String getBegan() {
    return getKey();
  }

  /**
   * @return The closing sigil token, or the empty string if none set.
   */
  public String getEnded() {
    return getValue();
  }
}
