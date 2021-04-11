/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.MainApp;

import java.util.stream.Collectors;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.NEWLINE;
import static com.keenwrite.constants.Constants.STATUS_BAR_OK;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;

/**
 * Collates information about an application issue. The issues can be
 * exceptions, state problems, parsing errors, and so forth.
 */
public class StatusEvent implements AppEvent {
  private static final String PACKAGE_NAME = MainApp.class.getPackageName();

  private static final String ENGLISHIFY =
    "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])";

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
      stream( trace.getStackTrace() )
        .takeWhile( StatusEvent::filter )
        .limit( 10 )
        .collect( Collectors.toList() )
        .forEach( e -> sb.append( e.toString() ).append( NEWLINE ) );
    }

    return sb.toString();
  }

  public String getException() {
    return mProblem == null ? "" : toEnglish( mProblem );
  }

  private static boolean filter( final StackTraceElement e ) {
    final var clazz = e.getClassName();
    return clazz.contains( PACKAGE_NAME ) ||
      clazz.contains( "org.renjin." ) ||
      clazz.contains( "sun." ) ||
      clazz.contains( "flexmark." ) ||
      clazz.contains( "java." );
  }

  /**
   * Separates the exception class name from TitleCase into lowercase,
   * space-separated words. This makes the exception look a little more like
   * English. Any {@link RuntimeException} instances passed into this method
   * will have the cause extracted, if possible.
   *
   * @param problem The exception that triggered the status event change.
   * @return A human-readable message with the exception name and the
   * exception's message.
   */
  private static String toEnglish( Throwable problem ) {
    if( problem instanceof RuntimeException &&
      (problem = problem.getCause()) == null ) {
      return "";
    }

    final var className = problem.getClass().getSimpleName();
    final var words = join( " ", className.split( ENGLISHIFY ) );
    return format( " (%s: %s)", words.toLowerCase(), problem.getMessage() );
  }

  /**
   * Returns the message used to construct the event.
   *
   * @return The message for this event.
   */
  public String getMessage() {
    return mMessage;
  }

  /**
   * Resets the status bar to a default message. Indicates that there are no
   * issues to bring to the user's attention.
   */
  public static void clue() {
    fireStatusEvent( get( STATUS_BAR_OK, "OK" ) );
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
    fireStatusEvent( problem.getMessage(), problem );
  }

  private static void fireStatusEvent( final String message ) {
    new StatusEvent( message ).fire();
  }

  private static void fireStatusEvent(
    final String message, final Throwable problem ) {
    new StatusEvent( message, problem ).fire();
  }
}
