/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

import com.keenwrite.preferences.UserPreferences;

import java.util.function.UnaryOperator;

/**
 * Responsible for updating definition keys to use a machine-readable format
 * corresponding to the type of file being edited. This changes a definition
 * key name based on some criteria determined by the factory that creates
 * implementations of this interface.
 */
public abstract class SigilOperator implements UnaryOperator<String> {
  protected static UserPreferences getUserPreferences() {
    return UserPreferences.getInstance();
  }
}
