/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.collections;

import com.keenwrite.sigils.SigilKeyOperator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for interpolating key-value pairs in a map. That is, this will
 * iterate over all key-value pairs and replace keys wrapped in sigils
 * with corresponding definition value from the same map.
 */
public class InterpolatingMap extends ConcurrentHashMap<String, String> {
  private static final int GROUP_DELIMITED = 1;

  /**
   * Used to override the default initial capacity in {@link HashMap}.
   */
  private static final int INITIAL_CAPACITY = 1 << 8;

  private transient final SigilKeyOperator mOperator;

  /**
   * @param operator Contains the opening and closing sigils that mark
   *                 where variable names begin and end.
   */
  public InterpolatingMap( final SigilKeyOperator operator ) {
    super( INITIAL_CAPACITY );

    assert operator != null;
    mOperator = operator;
  }

  /**
   * @param operator Contains the opening and closing sigils that mark
   *                 where variable names begin and end.
   * @param m        The initial {@link Map} to copy into this instance.
   */
  public InterpolatingMap(
    final SigilKeyOperator operator, final Map<String, String> m ) {
    this( operator );
    putAll( m );
  }

  /**
   * Interpolates all values in the map that reference other values by way
   * of key names. Performs a non-greedy match of key names delimited by
   * definition tokens. This operation modifies the map directly.
   *
   * @return {@code this}
   */
  public InterpolatingMap interpolate() {
    for( final var k : keySet() ) {
      replace( k, interpolate( get( k ) ) );
    }

    return this;
  }

  /**
   * Given a value with zero or more key references, this will resolve all
   * the values, recursively. If a key cannot be de-referenced, the value will
   * contain the key name, including the original sigils.
   *
   * @param value    Value containing zero or more key references.
   * @return The given value with all embedded key references interpolated.
   */
  public String interpolate( String value ) {
    assert value != null;

    final var matcher = mOperator.match( value );

    while( matcher.find() ) {
      final var keyName = matcher.group( GROUP_DELIMITED );
      final var mapValue = get( keyName );

      if( mapValue != null ) {
        final var keyValue = interpolate( mapValue );
        value = value.replace( mOperator.apply( keyName ), keyValue );
      }
    }

    return value;
  }

  @Override
  public boolean equals( final Object o ) {
    if( this == o ) { return true; }
    if( o == null || getClass() != o.getClass() ) { return false; }
    if( !super.equals( o ) ) { return false; }
    final InterpolatingMap that = (InterpolatingMap) o;
    return Objects.equals( mOperator, that.mOperator );
  }

  @Override
  public int hashCode() {
    return Objects.hash( super.hashCode(), mOperator );
  }
}
