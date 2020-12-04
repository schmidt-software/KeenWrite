/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Provides the application with a uniform way to notify the user of events.
 */
public interface Notifier {

  ButtonType YES = ButtonType.YES;
  ButtonType NO = ButtonType.NO;
  ButtonType CANCEL = ButtonType.CANCEL;

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
}
