/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Convenience class for pairing two objects together; this is a synonym for
 * {@link Map.Entry}.
 *
 * @param <K> The type of key to store in this pair.
 * @param <V> The type of value to store in this pair.
 */
public class Pair<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {
  /**
   * Associates a new key-value pair.
   *
   * @param key   The key for this key-value pairing.
   * @param value The value for this key-value pairing.
   */
  public Pair( final K key, final V value ) {
    super( key, value );
  }
}
