/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_SCROLL_LOCK;

/**
 * Collates information about the scroll lock status.
 */
public class ScrollLockEvent implements AppEvent {
  private final boolean mLocked;

  private ScrollLockEvent( final boolean locked ) {
    mLocked = locked;
  }

  /**
   * Fires a scroll lock event provided that the scroll lock key is in the
   * off state.
   *
   * @param locked The new locked status.
   */
  public static void fireScrollLockEvent( final boolean locked ) {
    // If the scroll lock key is off, allow the status to change.
    if( !getScrollLockKeyStatus() ) {
      fire( locked );
    }
  }

  /**
   * Fires a scroll lock event based on the current status of the scroll
   * lock key.
   */
  public static void fireScrollLockEvent() {
    fire( getScrollLockKeyStatus() );
  }

  /**
   * Answers whether the synchronized scrolling should be locked in place
   * (i.e., prevent sync scrolling).
   *
   * @return {@code true} when the user has locked the scrollbar position.
   */
  public boolean isLocked() {
    return mLocked;
  }

  private static void fire( final boolean locked ) {
    new ScrollLockEvent( locked ).fire();
  }

  /**
   * Returns the state of the scroll lock key.
   *
   * @return {@code true} when the scroll lock key is in the on state.
   */
  private static boolean getScrollLockKeyStatus() {
    return getDefaultToolkit().getLockingKeyState( VK_SCROLL_LOCK );
  }
}
