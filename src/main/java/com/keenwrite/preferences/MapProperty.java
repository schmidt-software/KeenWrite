/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Map;

/**
 * Responsible for wrapping a {@link Map} as an observable {@link Property}.
 *
 * @param <K> The type of key to insert into the {@link Map}.
 * @param <V> The type of value to insert into the {@link Map}.
 */
public class MapProperty<K, V> extends SimpleObjectProperty<Map<K, V>> {

  /**
   * Use to instantiate a new {@link Property} that wraps a {@link Map}.
   *
   * @param map The {@link Map} to wrap as an observable {@link Property}.
   */
  public MapProperty( final Map<K, V> map ) {
    super( map );
  }

  @Override
  public String toString() {
    return get().toString();
  }
}
