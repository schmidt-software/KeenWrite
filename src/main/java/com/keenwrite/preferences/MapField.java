/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.util.BindingMode;

/**
 * Responsible for binding a form field to a map of values that, ultimately,
 * users may edit.
 *
 * @param <K> The type of key to store in the map.
 * @param <V> The type of value to store in the map.
 */
public class MapField<K, V> extends Field<MapField<K, V>> {

  private final MapProperty<K, V> mMapProperty;

  public static <K, V> MapField<K, V> ofMapType( final MapProperty<K, V> map ) {
    return new MapField<>( map );
  }

  private MapField( final MapProperty<K, V> mapProperty ) {
    assert mapProperty != null;

    mMapProperty = mapProperty;
  }

  public MapProperty<K, V> mapProperty() {
    return mMapProperty;
  }

  @Override
  public void setBindingMode( final BindingMode bindingMode ) {
    System.out.println( "BIND TO: " + bindingMode );
  }

  /**
   * Answers whether the user input is valid.
   *
   * @return {@code true} Users may provide any strings.
   */
  @Override
  protected boolean validate() {
    return true;
  }

  @Override
  public void persist() {
    System.out.println( "PURSIST: " + mMapProperty );
    System.out.println( mMapProperty.get() );
  }

  @Override
  public void reset() {
    System.out.println( "RESET" );
  }
}
