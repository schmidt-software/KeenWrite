/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import java.util.Map;

/**
 * Responsible for common behaviour across all text replacer implementations.
 */
public abstract class AbstractTextReplacer implements TextReplacer {
  /**
   * Optimization: Cache keys until the map changes.
   */
  private String[] mKeys;

  /**
   * Optimization: Cache values until the map changes.
   */
  private String[] mValues;

  /**
   * Optimization: Detect when the map changes.
   */
  private int mMapHash;

  /**
   * Default (empty) constructor.
   */
  protected AbstractTextReplacer() {
  }

  protected String[] keys( final Map<String, String> map ) {
    updateCache( map );

    return mKeys;
  }

  protected String[] values( final Map<String, String> map ) {
    updateCache( map );

    return mValues;
  }

  private void updateCache( final Map<String, String> map ) {
    if( map.hashCode() != mMapHash ) {
      mKeys = map.keySet().toArray( new String[ 0 ] );
      mValues = map.values().toArray( new String[ 0 ] );
      mMapHash = map.hashCode();
    }
  }
}
