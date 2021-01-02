/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Responsible for constructing objects that would otherwise require
 * a long list of constructor parameters.
 * <p>
 * See <a href="https://stackoverflow.com/a/31754787/59087">source</a> for
 * details.
 * </p>
 *
 * @param <MT> The mutable definition for the type of object to build.
 * @param <IT> The immutable definition for the type of object to build.
 */
public class GenericBuilder<MT, IT> {
  /**
   * Provides the methods to use for setting object properties.
   */
  private final Supplier<MT> mMutable;

  /**
   * Calling {@link #build()} will instantiate the immutable instance using
   * the mutator.
   */
  private final Function<MT, IT> mImmutable;

  /**
   * Adds a modifier to call when building an instance.
   */
  private final List<Consumer<MT>> mModifiers = new ArrayList<>();

  /**
   * Constructs a new builder instance that is capable of populating values for
   * any type of object.
   *
   * @param mutator Provides methods to use for setting object properties.
   */
  protected GenericBuilder(
      final Supplier<MT> mutator, final Function<MT, IT> immutable ) {
    assert mutator != null;
    assert immutable != null;

    mMutable = mutator;
    mImmutable = immutable;
  }

  /**
   * Starting point for building an instance of a particular class.
   *
   * @param supplier Returns the instance to build.
   * @param <MT>     The type of class to build.
   * @return A new {@link GenericBuilder} capable of populating data for an
   * instance of the class provided by the {@link Supplier}.
   */
  public static <MT, IT> GenericBuilder<MT, IT> of(
      final Supplier<MT> supplier, final Function<MT, IT> immutable ) {
    return new GenericBuilder<>( supplier, immutable );
  }

  /**
   * Registers a new value with the builder.
   *
   * @param consumer Accepts a value to be set upon the built object.
   * @param value    The value to use when building.
   * @param <V>      The type of value used when building.
   * @return This {@link GenericBuilder} instance.
   */
  public <V> GenericBuilder<MT, IT> with(
      final BiConsumer<MT, V> consumer, final V value ) {
    mModifiers.add( instance -> consumer.accept( instance, value ) );
    return this;
  }

  /**
   * Instantiates then populates the immutable object to build.
   *
   * @return The newly built object.
   */
  public IT build() {
    final var value = mMutable.get();
    mModifiers.forEach( modifier -> modifier.accept( value ) );
    mModifiers.clear();
    return mImmutable.apply( value );
  }
}
