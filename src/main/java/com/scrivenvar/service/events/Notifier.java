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
package com.scrivenvar.service.events;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Observer;

/**
 * Provides the application with a uniform way to notify the user of events.
 */
public interface Notifier {

  ButtonType YES = ButtonType.YES;
  ButtonType NO = ButtonType.NO;
  ButtonType CANCEL = ButtonType.CANCEL;

  /**
   * Notifies the user of a problem.
   *
   * @param message The problem description.
   */
  void notify( final String message );

  /**
   * Notifies the user about the exception.
   *
   * @param ex The exception containing a message to show to the user.
   */
  default void notify( final Exception ex ) {
    assert ex != null;

    log( ex );
    notify( ex.getMessage() );
  }

  /**
   * Writes the exception to a log file. The log file should be written
   * in the System's temporary directory.
   *
   * @param ex The exception to show in the status bar and log to a file.
   */
  default void log( final Exception ex ) {
    try(
        final FileWriter fw = new FileWriter( getLogPath(), true );
        final PrintWriter pw = new PrintWriter( fw )
    ) {
      ex.printStackTrace( pw );
    } catch( final IOException ioe ) {
      // The notify method will display the message on the status
      // bar.
    }
  }

  /**
   * Returns the fully qualified path to the log file to write to when
   * an exception occurs.
   *
   * @return Location of the log file for writing unexpected exceptions.
   */
  File getLogPath();

  /**
   * Causes any displayed notifications to disappear.
   */
  void clear();

  /**
   * Constructs a default alert message text for a modal alert dialog.
   *
   * @param title   The dialog box message title.
   * @param message The dialog box message content (needs formatting).
   * @param args    The arguments to the message content that must be formatted.
   * @return The message suitable for building a modal alert dialog.
   */
  Notification createNotification(
      String title,
      String message,
      Object... args );

  /**
   * Creates an alert of alert type error with a message showing the cause of
   * the error.
   *
   * @param parent  Dialog box owner (for modal purposes).
   * @param message The error message, title, and possibly more details.
   * @return A modal alert dialog box ready to display using showAndWait.
   */
  Alert createError( Window parent, Notification message );

  /**
   * Creates an alert of alert type confirmation with Yes/No/Cancel buttons.
   *
   * @param parent  Dialog box owner (for modal purposes).
   * @param message The message, title, and possibly more details.
   * @return A modal alert dialog box ready to display using showAndWait.
   */
  Alert createConfirmation( Window parent, Notification message );

  /**
   * Adds an observer to the list of objects that receive notifications about
   * error messages to be presented to the user.
   *
   * @param observer The observer instance to notify.
   */
  void addObserver( Observer observer );
}
