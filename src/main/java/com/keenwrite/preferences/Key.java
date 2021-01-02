/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

/**
 * Responsible for creating a type hierarchy of preference storage keys.
 */
public class Key {
  private final Key mParent;
  private final String mName;

  private Key( final Key parent, final String name ) {
    mParent = parent;
    mName = name;
  }

  /**
   * Returns a new key with no parent.
   *
   * @param name The key name, never {@code null}.
   * @return The new {@link Key} instance with a name but no parent.
   */
  public static Key key( final String name ) {
    assert name != null && !name.isEmpty();
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
    assert name != null && !name.isEmpty();
    return new Key( parent, name );
  }

  private Key parent() {
    return mParent;
  }

  private String name() {
    return mName;
  }

  /**
   * Returns a dot-separated path representing the key's name.
   *
   * @return The recursively derived dot-separated key name.
   */
  @Override
  public String toString() {
    return parent() == null ? name() : parent().toString() + '.' + name();
  }
}
