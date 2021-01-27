/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.events.StatusEvent;
import com.keenwrite.ui.logging.LogView;

import static com.keenwrite.Messages.get;

/**
 * Responsible for passing notifications about exceptions (or other error
 * messages) through the application. Once the Event Bus is implemented, this
 * class can go away.
 */
public final class StatusNotifier {

  private static final LogView sLogView = new LogView();

  /**
   * Resets the status bar to a default message.
   */
  public static void clue() {
    StatusEvent.OK.fire();
  }

  /**
   * Updates the status bar with a custom message.
   *
   * @param key  The property key having a value to populate with arguments.
   * @param args The placeholder values to substitute into the key's value.
   */
  public static void clue( final String key, final Object... args ) {
    final var message = get( key, args );
    update( message );
    sLogView.log( message );
  }

  /**
   * Update the status bar with a pre-parsed message and exception.
   *
   * @param message The custom message to log.
   * @param t       The exception that triggered the status update.
   */
  public static void clue( final String message, final Throwable t ) {
    update( message );
    sLogView.log( message, t );
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param t The exception with a message that the user should know about.
   */
  public static void clue( final Throwable t ) {
    update( t.getMessage() );
    sLogView.log( t );
  }

  public static void update( final String message ) {
    new StatusEvent( message ).fire();
  }

  public static void viewIssues() {
    sLogView.view();
  }
}
