/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import org.greenrobot.eventbus.EventBus;

/**
 * Responsible for delegating interactions to the event bus library. This
 * class decouples the rest of the application from a particular event bus
 * implementation.
 */
public class Bus {
  private static final EventBus sEventBus = EventBus.getDefault();

  public static <Subscriber> void register( final Subscriber subscriber ) {
    sEventBus.register( subscriber );
  }

  public static <Subscriber> void unregister( final Subscriber subscriber ) {
    sEventBus.unregister( subscriber );
  }

  public static <Event> void post( final Event event ) {
    sEventBus.post( event );
  }
}
