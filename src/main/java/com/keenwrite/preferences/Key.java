/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import java.util.Stack;
import java.util.function.Consumer;

/**
 * Responsible for creating a type hierarchy of preference storage keys.
 */
public class Key {
  private final Key mParent;
  private final String mName;

  /**
   * Returns a new key with no parent.
   *
   * @param name The key name, never {@code null}.
   * @return The new {@link Key} instance with a name but no parent.
   */
  public static Key key( final String name ) {
    return key( null, name );
  }

  /**
   * Returns a new key with a given parent.
   *
   * @param parent The parent of this {@link Key}, or {@code null} if this is
   *               the topmost key in the chain.
   * @param name   The key name, never {@code null}.
   * @return The new {@link Key} instance with a name and parent.
   */
  public static Key key( final Key parent, final String name ) {
    return new Key( parent, name );
  }

  private Key( final Key parent, final String name ) {
    assert name != null;
    assert !name.isBlank();

    mParent = parent;
    mName = name;
  }

  /**
   * Answers whether more {@link Key}s exist above this one in the hierarchy.
   *
   * @return {@code true} means this {@link Key} has a parent {@link Key}.
   */
  public boolean hasParent() {
    return mParent != null;
  }

  /**
   * Visits every key in the hierarchy, starting at the topmost {@link Key} and
   * ending with the current {@link Key}.
   *
   * @param consumer  Receives the name of each visited node.
   * @param separator Characters to insert between each node.
   */
  public void walk( final Consumer<String> consumer, final String separator ) {
    var key = this;

    final var stack = new Stack<String>();

    while( key != null ) {
      stack.push( key.name() );
      key = key.parent();
    }

    var sep = "";

    while( !stack.empty() ) {
      consumer.accept( sep + stack.pop() );
      sep = separator;
    }
  }

  public void walk( final Consumer<String> consumer ) {
    walk( consumer, "" );
  }

  public Key parent() {
    return mParent;
  }

  public String name() {
    return mName;
  }

  /**
   * Returns a dot-separated path representing the key's name.
   *
   * @return The dot-separated key name.
   */
  @Override
  public String toString() {
    final var sb = new StringBuilder( 128 );

    walk( sb::append, "." );

    return sb.toString();
  }
}
