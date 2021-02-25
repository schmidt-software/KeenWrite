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

  public boolean isLocked() {
    return mLocked;
  }

  private static void fire( final boolean locked ) {
    new ScrollLockEvent( locked ).fire();
  }

  private static boolean getScrollLockKeyStatus() {
    return getDefaultToolkit().getLockingKeyState( VK_SCROLL_LOCK );
  }
}
