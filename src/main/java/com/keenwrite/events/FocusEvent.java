/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events;

/**
 * Collates information about an object that has gained focus. This is typically
 * used by text resource editors (such as text editors and definition editors).
 */
public class FocusEvent<T> implements AppEvent {
  private final T mNode;

  protected FocusEvent( final T node ) {
    mNode = node;
  }

  /**
   * This method is used to help update the UI whenever a component has gained
   * input focus.
   *
   * @return The object that has gained focus.
   */
  public T get() {
    return mNode;
  }
}
