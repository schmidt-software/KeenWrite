/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import static com.keenwrite.events.Bus.post;

/**
 * Marker interface for all application events.
 */
public interface AppEvent {

  /**
   * Submits this event to the {@link Bus}.
   */
  default void publish() {
    post( this );
  }
}
