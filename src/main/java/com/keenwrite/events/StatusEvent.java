/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.MainApp;

import java.util.stream.Collectors;

import static com.keenwrite.Constants.NEWLINE;
import static com.keenwrite.Constants.STATUS_BAR_OK;
import static com.keenwrite.Messages.get;
import static java.util.Arrays.stream;

/**
 * Collates information about an application issue. The issues can be
 * exceptions, state problems, parsing errors, and so forth.
 */
public class StatusEvent implements AppEvent {
  /**
   * Indicates that there are no issues to bring to the user's attention.
   */
  private static final StatusEvent OK =
    new StatusEvent( get( STATUS_BAR_OK, "OK" ) );

  /**
   * Detailed information about a problem.
   */
  private final String mMessage;

  /**
   * Provides stack trace information that isolates the cause.
   */
  private final Throwable mProblem;

  /**
   * Constructs a new event that contains a problem description to help the
   * user resolve an issue encountered while using the application.
   *
   * @param message The human-readable message, typically displayed on-screen.
   */
  public StatusEvent( final String message ) {
    this( message, null );
  }

  /**
   * Constructs a new event that contains a problem description to help the
   * user resolve an issue encountered while using the application.
   *
   * @param message The human-readable message, typically displayed on-screen.
   * @param problem Stack trace to pin-point the problem, may be {@code null}.
   */
  public StatusEvent( final String message, final Throwable problem ) {
    assert message != null;
    mMessage = message;
    mProblem = problem;
  }

  /**
   * Returns the stack trace information for the issue encountered. This is
   * optional because usually a status message isn't an application error.
   *
   * @return Optional stack trace to pin-point the problem area in the code.
   */
  public String getProblem() {
    // 256 is arbitrary; stack traces shouldn't be much larger.
    final var sb = new StringBuilder( 256 );
    final var trace = mProblem;

    if( trace != null ) {
      sb.append( trace.getMessage().trim() ).append( NEWLINE );
      stream( trace.getStackTrace() )
        .takeWhile( StatusEvent::filter )
        .limit( 10 )
        .collect( Collectors.toList() )
        .forEach( e -> sb.append( e.toString() ).append( NEWLINE ) );
    }

    return sb.toString();
  }

  private static boolean filter( final StackTraceElement e ) {
    final var clazz = e.getClassName();
    return clazz.contains( MainApp.class.getPackageName() ) ||
      clazz.startsWith( "org.renjin" );
  }

  /**
   * Returns the message used to construct the event.
   *
   * @return The message for this event.
   */
  public String toString() {
    return mMessage;
  }

  /**
   * Resets the status bar to a default message.
   */
  public static void clue() {
    OK.fire();
  }

  /**
   * Updates the status bar with a custom message.
   *
   * @param key  The property key having a value to populate with arguments.
   * @param args The placeholder values to substitute into the key's value.
   */
  public static void clue( final String key, final Object... args ) {
    fireStatusEvent( get( key, args ) );
  }

  /**
   * Update the status bar with a pre-parsed message and exception.
   *
   * @param message The custom message to log.
   * @param problem The exception that triggered the status update.
   */
  public static void clue( final String message, final Throwable problem ) {
    fireStatusEvent( message, problem );
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param problem The exception with a message to display to the user.
   */
  public static void clue( final Throwable problem ) {
    fireStatusEvent( problem.getMessage() );
  }

  private static void fireStatusEvent( final String message ) {
    new StatusEvent( message ).fire();
  }

  private static void fireStatusEvent( final String message, final Throwable problem ) {
    new StatusEvent( message, problem ).fire();
  }
}
