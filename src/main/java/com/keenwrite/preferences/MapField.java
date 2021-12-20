package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.util.BindingMode;
import javafx.beans.property.ObjectProperty;

import java.util.Map;

/**
 * Responsible for binding a form field to a map of values that, ultimately,
 * users may edit.
 *
 * @param <K> The type of key to store in the map.
 * @param <V> The type of value to store in the map.
 */
public class MapField<K, V> extends Field<MapField<K, V>> {

  private final MapProperty<K, V> mMapProperty;

  public static <K, V> MapField<K, V> ofMapType(
    final ObjectProperty<Map<K, V>> binding ) {

    return new MapField<>( new MapProperty<>( binding.get() ) );
  }

  private MapField( final MapProperty<K, V> mapProperty ) {
    assert mapProperty != null;

    mMapProperty = mapProperty;
  }

  public MapProperty<K, V> mapProperty() {
    return mMapProperty;
  }

  @Override
  public void setBindingMode( final BindingMode newValue ) {
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
  }

  @Override
  public void reset() {
  }
}
