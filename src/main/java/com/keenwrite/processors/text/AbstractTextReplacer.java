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
  private String[] mKeys = new String[ 0 ];

  /**
   * Optimization: Cache values until the map changes.
   */
  private String[] mValues = new String[ 0 ];

  /**
   * Optimization: Detect when the map changes.
   */
  private int mMapHash;

  private final Object mMutex = new Object();

  /**
   * Default (empty) constructor.
   */
  protected AbstractTextReplacer() { }

  protected String[] keys( final Map<String, String> map ) {
    synchronized( mMutex ) {
      updateCache( map );
      return mKeys;
    }
  }

  protected String[] values( final Map<String, String> map ) {
    synchronized( mMutex ) {
      updateCache( map );
      return mValues;
    }
  }

  private void updateCache( final Map<String, String> map ) {
    synchronized( mMutex ) {
      if( map.hashCode() != mMapHash ) {
        mKeys = map.keySet().toArray( new String[ 0 ] );
        mValues = map.values().toArray( new String[ 0 ] );
        mMapHash = map.hashCode();
      }
    }
  }
}
