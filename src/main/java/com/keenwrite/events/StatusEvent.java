/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import static com.keenwrite.Constants.STATUS_BAR_OK;
import static com.keenwrite.Messages.get;
import static com.keenwrite.events.Bus.post;

/**
 * Responsible for collating all information about an application issue. The
 * issues can be exceptions, state problems, parsing errors, and so forth.
 */
public class StatusEvent {
  /**
   * Indicates that there are no issues to bring to the user's attention.
   */
  public static final StatusEvent OK =
    new StatusEvent( get( STATUS_BAR_OK, "OK" ) );

  /**
   * Detailed information about a problem.
   */
  private final String mMessage;

  /**
   * Constructs a new event that contains a problem description to help the
   * user resolve an issue encountered while using the appliation.
   *
   * @param message The human-readable message, typically displayed on-screen.
   */
  public StatusEvent( final String message ) {
    assert message != null;
    mMessage = message;
  }

  /**
   * Submits this event to the {@link Bus}.
   */
  public void fire() {
    post( this );
  }

  /**
   * Returns the message used to construct the event.
   *
   * @return The message for this event.
   */
  public String toString() {
    return mMessage;
  }
}
