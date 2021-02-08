/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.tree;

import javafx.util.StringConverter;

/**
 * Responsible for converting objects to and from string instances. The
 * tree items contain only strings, so this effectively is a string-to-string
 * converter, which allows the implementation to retain its generics.
 */
public class TreeItemConverter extends StringConverter<String> {

  @Override
  public String toString( final String object ) {
    return sanitize( object );
  }

  @Override
  public String fromString( final String string ) {
    return sanitize( string );
  }

  private String sanitize( final String string ) {
    return string == null ? "" : string;
  }
}
