/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import java.util.Map;

/**
 * Responsible for common behaviour across all text replacer implementations.
 */
public abstract class AbstractTextReplacer implements TextReplacer {

  /**
   * Default (empty) constructor.
   */
  protected AbstractTextReplacer() {
  }

  protected String[] keys( final Map<String, String> map ) {
    return map.keySet().toArray( new String[ 0 ] );
  }

  protected String[] values( final Map<String, String> map ) {
    return map.values().toArray( new String[ 0 ] );
  }
}
