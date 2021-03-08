/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map that removes the oldest entry once its capacity (cache size) has
 * been reached.
 *
 * @param <K> The type of key mapped to a value.
 * @param <V> The type of value mapped to a key.
 */
public final class BoundedCache<K, V> extends LinkedHashMap<K, V> {
  private final int mCacheSize;

  /**
   * Constructs a new instance having a finite size.
   *
   * @param cacheSize The maximum number of entries.
   */
  public BoundedCache( final int cacheSize ) {
    mCacheSize = cacheSize;
  }

  @Override
  protected boolean removeEldestEntry( final Map.Entry<K, V> eldest ) {
    return size() > mCacheSize;
  }
}
