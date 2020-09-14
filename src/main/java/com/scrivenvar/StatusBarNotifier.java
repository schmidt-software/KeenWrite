/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar;

import com.scrivenvar.service.events.Notifier;
import org.controlsfx.control.StatusBar;

import static com.scrivenvar.Constants.STATUS_BAR_OK;
import static com.scrivenvar.Messages.get;
import static javafx.application.Platform.runLater;

/**
 * Responsible for passing notifications about exceptions (or other error
 * messages) through the application. Once the Event Bus is implemented, this
 * class can go away.
 */
public class StatusBarNotifier {
  private static final String OK = get( STATUS_BAR_OK, "OK" );

  private static final Notifier sNotifier = Services.load( Notifier.class );
  private static StatusBar sStatusBar;

  public static void setStatusBar( final StatusBar statusBar ) {
    sStatusBar = statusBar;
  }

  /**
   * Resets the status bar to a default message.
   */
  public static void clearAlert() {
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
  public static void alert( final String key ) {
    update( get( key ) );
  }

  /**
   * Updates the status bar with a custom message.
   *
   * @param key  The property key having a value to populate with arguments.
   * @param args The placeholder values to substitute into the key's value.
   */
  public static void alert( final String key, final Object... args ) {
    update( get( key, args ) );
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param ex The exception with a message that the user should know about.
   */
  public static void alert( final Exception ex ) {
    update( ex.getMessage() );
  }

  /**
   * Updates the status bar to show the given message.
   *
   * @param s The message to show in the status bar.
   */
  private static void update( final String s ) {
    runLater(
        () -> {
          final var i = s.indexOf( '\n' );
          sStatusBar.setText( s.substring( 0, i > 0 ? i : s.length() ) );
        }
    );
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
}
