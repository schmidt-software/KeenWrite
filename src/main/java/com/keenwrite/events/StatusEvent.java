/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.MainApp;

import java.util.List;
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
public final class StatusEvent implements AppEvent {
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
    assert message != null;
    mMessage = message;
    mProblem = null;
  }

  public StatusEvent( final Throwable problem ) {
    this( "", problem );
  }

  public StatusEvent( final String message, final Throwable problem ) {
    assert message != null;
    assert problem != null;
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

  @Override
  public String toString() {
    return format( "%s%s%s",
                   mMessage,
                   mMessage.isBlank() ? "" : " ",
                   mProblem == null ? "" : toEnglish( mProblem ) );
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
    assert problem != null;

    // Subclasses of RuntimeException must be subject to Englishification.
    if( problem.getClass().equals( RuntimeException.class ) ) {
      final var cause = problem.getCause();
      return cause == null ? problem.getMessage() : cause.getMessage();
    }

    final var className = problem.getClass().getSimpleName();
    final var words = join( " ", className.split( ENGLISHIFY ) );
    return format( "(%s: %s)", words.toLowerCase(), problem.getMessage() );
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
    fire( get( STATUS_BAR_OK, "OK" ) );
  }

  /**
   * Notifies listeners of a series of messages. This is useful when providing
   * users feedback of how third-party executables have failed.
   *
   * @param messages The lines of text to display.
   */
  public static void clue( final List<String> messages ) {
    messages.forEach( StatusEvent::fire );
  }

  /**
   * Notifies listeners of an error.
   *
   * @param key The message bundle key to look up.
   * @param t   The exception that caused the error.
   */
  public static void clue( final String key, final Throwable t ) {
    fire( get( key ), t );
  }

  /**
   * Notifies listeners of a custom message.
   *
   * @param key  The property key having a value to populate with arguments.
   * @param args The placeholder values to substitute into the key's value.
   */
  public static void clue( final String key, final Object... args ) {
    fire( get( key, args ) );
  }

  /**
   * Notifies listeners of an exception occurs that warrants the user's
   * attention.
   *
   * @param problem The exception with a message to display to the user.
   */
  public static void clue( final Throwable problem ) {
    fire( problem );
  }

  private static void fire( final String message ) {
    new StatusEvent( message ).publish();
  }

  private static void fire( final Throwable problem ) {
    new StatusEvent( problem ).publish();
  }

  private static void fire(
    final String message, final Throwable problem ) {
    new StatusEvent( message, problem ).publish();
  }
}
