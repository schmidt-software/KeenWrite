/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.nio.file.Path;

/**
 * Provides the application with a uniform way to notify the user of events.
 */
public interface Notifier {

  ButtonType YES = ButtonType.YES;
  ButtonType NO = ButtonType.NO;
  ButtonType CANCEL = ButtonType.CANCEL;

  /**
   * Constructs an alert message text for a modal alert dialog.
   *
   * @param parent     The window responsible for the child dialog.
   * @param path       The path to a file that was not actionable.
   * @param titleKey   The dialog box message title.
   * @param messageKey The dialog box message content (needs formatting).
   * @param ex         The problem that requires user attention.
   */
  void alert(
      Window parent,
      Path path,
      String titleKey,
      String messageKey,
      Exception ex );

  /**
   * Constructs an alert message text for a modal alert dialog.
   *
   * @param parent The window responsible for the child dialog.
   * @param path   The path to a file that was not actionable.
   * @param key    Prefix for both title and message key.
   * @param ex     The problem that requires user attention.
   */
  default void alert(
      Window parent,
      Path path,
      String key,
      Exception ex ) {
    alert( parent, path, key + ".title", key + ".message", ex );
  }

  /**
   * Contains all the information that the user needs to know about a problem.
   *
   * @param title   The dialog box message title (i.e., the error context).
   * @param message The message content (formatted with the given args).
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
}
