/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events.impl;

import com.keenwrite.service.events.Notification;
import com.keenwrite.service.events.Notifier;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;

/**
 * Provides the ability to notify the user of events that need attention,
 * such as prompting the user to confirm closing when there are unsaved changes.
 */
public final class DefaultNotifier implements Notifier {

  /**
   * Contains all the information that the user needs to know about a problem.
   *
   * @param title   The context for the message.
   * @param message The message content (formatted with the given args).
   * @param args    Parameters for the message content.
   * @return A notification instance, never null.
   */
  @Override
  public Notification createNotification(
      final String title,
      final String message,
      final Object... args ) {
    return new DefaultNotification( title, message, args );
  }

  private Alert createAlertDialog(
      final Window parent,
      final AlertType alertType,
      final Notification message ) {

    final Alert alert = new Alert( alertType );

    alert.setDialogPane( new ButtonOrderPane() );
    alert.setTitle( message.getTitle() );
    alert.setHeaderText( null );
    alert.setContentText( message.getContent() );
    alert.initOwner( parent );

    return alert;
  }

  @Override
  public Alert createConfirmation( final Window parent,
                                   final Notification message ) {
    final Alert alert = createAlertDialog( parent, CONFIRMATION, message );

    alert.getButtonTypes().setAll( YES, NO, CANCEL );

    return alert;
  }

  @Override
  public Alert createError( final Window parent, final Notification message ) {
    return createAlertDialog( parent, ERROR, message );
  }
}
