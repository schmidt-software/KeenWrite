/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.service.events.Notifier;
import org.controlsfx.control.StatusBar;

import static com.keenwrite.Constants.STATUS_BAR_OK;
import static com.keenwrite.Messages.get;
import static javafx.application.Platform.runLater;

/**
 * Responsible for passing notifications about exceptions (or other error
 * messages) through the application. Once the Event Bus is implemented, this
 * class can go away.
 */
public class StatusBarNotifier {
  private static final String OK = get( STATUS_BAR_OK, "OK" );

  private static final Notifier sNotifier = Services.load( Notifier.class );
  private static final StatusBar sStatusBar = new StatusBar();

  /**
   * Resets the status bar to a default message.
   */
  public static void clearClue() {
    // Don't burden the repaint thread if there's no status bar change.
    if( !OK.equals( sStatusBar.getText() ) ) {
      update( OK );
    }
  }

  /**
   * Updates the status bar with a custom message.
   *
   * @param key The resource bundle key associated with a message (typically
   *            to inform the user about an error).
   */
  public static void clue( final String key ) {
    update( get( key ) );
  }

  /**
   * Updates the status bar with a custom message.
   *
   * @param key  The property key having a value to populate with arguments.
   * @param args The placeholder values to substitute into the key's value.
   */
  public static void clue( final String key, final Object... args ) {
    update( get( key, args ) );
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param t The exception with a message that the user should know about.
   */
  public static void clue( final Throwable t ) {
    update( t.getMessage() );
  }

  /**
   * Returns the global {@link Notifier} instance that can be used for opening
   * pop-up alert messages.
   *
   * @return The pop-up {@link Notifier} dispatcher.
   */
  public static Notifier getNotifier() {
    return sNotifier;
  }

  public static StatusBar getStatusBar() {
    return sStatusBar;
  }

  /**
   * Updates the status bar to show the first line of the given message.
   *
   * @param message The message to show in the status bar.
   */
  private static void update( final String message ) {
    runLater(
        () -> {
          final var s = message == null ? "" : message;
          final var i = s.indexOf( '\n' );
          sStatusBar.setText( s.substring( 0, i > 0 ? i : s.length() ) );
        }
    );
  }
}
