/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

/**
 * Responsible for transforming a string into itself. This is used at the
 * end of a processing chain when no more processing is required.
 */
public class IdentityProcessor extends ExecutorProcessor<String> {
  public static final IdentityProcessor IDENTITY = new IdentityProcessor();

  /**
   * Constructs a new instance having no successor (the default successor is
   * {@code null}).
   */
  private IdentityProcessor() {
  }

  /**
   * Returns the given string without modification.
   *
   * @param s The string to return.
   * @return The value of s.
   */
  @Override
  public String apply( final String s ) {
    return s;
  }
}
