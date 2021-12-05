/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.ui.outline.DocumentOutline;

/**
 * Collates information about a caret event, which is typically triggered when
 * the user double-clicks in the {@link DocumentOutline}.
 */
public class CaretNavigationEvent implements AppEvent {
  /**
   * Absolute document offset.
   */
  private final int mOffset;

  private CaretNavigationEvent( final int offset ) {
    mOffset = offset;
  }

  /**
   * Publishes an event that requests moving the caret to the given offset.
   *
   * @param offset Move the caret to this document offset.
   */
  public static void fire( final int offset ) {
    new CaretNavigationEvent( offset ).publish();
  }

  public int getOffset() {
    return mOffset;
  }
}
