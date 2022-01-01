/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.sigils;

/**
 * Responsible for defining sigils used within property files.
 */
public class PropertyKeyOperator extends SigilKeyOperator {
  public static final String BEGAN = "${";
  public static final String ENDED = "}";

  /**
   * Constructs a new {@link SigilKeyOperator} subclass with <code>${</code>
   * and <code>}</code> used for the beginning and ending sigils.
   */
  public PropertyKeyOperator() {
    super( BEGAN, ENDED );
  }
}
