/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
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
