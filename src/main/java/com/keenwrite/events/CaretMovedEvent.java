/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.editors.common.Caret;

/**
 * Responsible for notifying when the caret has moved, which includes giving
 * focus to a different editor.
 */
public class CaretMovedEvent implements AppEvent {
  private final Caret mCaret;

  private CaretMovedEvent( final Caret caret ) {
    assert caret != null;
    mCaret = caret;
  }

  public static void fire( final Caret caret ) {
    new CaretMovedEvent( caret ).publish();
  }

  public Caret getCaret() {
    return mCaret;
  }
}
