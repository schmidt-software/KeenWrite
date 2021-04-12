/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

/**
 * Responsible for creating an alternate execution path when a typesetter
 * cannot be found.
 */
public class TypesetterNotFoundException extends RuntimeException {
  /**
   * Constructs a new exception that indicates the typesetting engine cannot
   * be found anywhere along the PATH.
   *
   * @param name Typesetter executable file name.
   */
  public TypesetterNotFoundException( final String name ) {
    super( name );
  }
}
