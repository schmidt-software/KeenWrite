/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import javafx.beans.property.SimpleObjectProperty;

/**
 * Responsible for firing the value changed event when the {@link #set(Object)}
 * method is called, even if the value being set is the same as the current
 * value held by the object property instance.
 * 
 * @param <T> The type of object contained by instances of this class.
 */
public class RefireObjectProperty<T> extends SimpleObjectProperty<T> {
  @Override
  public void set( final T value ) {
    final var same = get() == value;

    // Retain the old behaviour, which will fire iff the values differ. 
    super.set( value );

    // The default behaviour is to suppress firing changed events if
    // the current object property value is the same as the given value.
    // We override this behaviour to fire the change event anyway, but
    // iff they are the same object (to avoid firing twice).
    if( same ) {
      fireValueChangedEvent();
    }
  }
}
