/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import javafx.beans.property.StringProperty;

import java.util.AbstractMap.SimpleImmutableEntry;

/**
 * Convenience class for pairing a start and an end sigil together.
 */
public final class Sigils
  extends SimpleImmutableEntry<StringProperty, StringProperty> {

  /**
   * Associates a new key-value pair.
   *
   * @param began The starting sigil.
   * @param ended The ending sigil.
   */
  public Sigils( final StringProperty began, final StringProperty ended ) {
    super( began, ended );
  }

  /**
   * @return The opening sigil token.
   */
  public String getBegan() {
    return getKey().get();
  }

  /**
   * @return The closing sigil token, or the empty string if none set.
   */
  public String getEnded() {
    return getValue().get();
  }
}
