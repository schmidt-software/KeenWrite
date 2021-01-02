/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.replaceEach;

/**
 * Replaces text using Apache's StringUtils.replaceEach method.
 */
public class StringUtilsReplacer extends AbstractTextReplacer {

  /**
   * Default (empty) constructor.
   */
  protected StringUtilsReplacer() {
  }

  @Override
  public String replace( final String text, final Map<String, String> map ) {
    return replaceEach( text, keys( map ), values( map ) );
  }
}
