/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events.impl;

import com.keenwrite.service.events.Notification;
import com.keenwrite.service.events.Notifier;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;

import java.nio.file.Path;

import static com.keenwrite.Constants.ICON_DIALOG_NODE;
import static com.keenwrite.Messages.get;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonType.*;

/**
 * Provides the ability to notify the user of events that need attention,
 * such as prompting the user to confirm closing when there are unsaved changes.
 */
public final class DefaultNotifier implements Notifier {

  @Override
  public Notification createNotification(
      final String title,
      final String message,
      final Object... args ) {
    return new DefaultNotification( title, message, args );
  }

  @Override
  public void alert(
      final Window parent,
      final Path path,
      final String titleKey,
      final String messageKey,
      final Exception ex ) {
    final var message = createNotification(
        get( titleKey ), get( messageKey ), path, ex.getMessage()
    );

    createError( parent, message ).showAndWait();
  }

  @Override
  public Alert createConfirmation(
      final Window parent, final Notification message ) {
    final var alert = createAlertDialog( parent, CONFIRMATION, message );

    alert.getButtonTypes().setAll( YES, NO, CANCEL );

    return alert;
  }

  @Override
  public Alert createError( final Window parent, final Notification message ) {
    return createAlertDialog( parent, ERROR, message );
  }

  private Alert createAlertDialog(
      final Window parent,
      final AlertType alertType,
      final Notification message ) {
    final var alert = new Alert( alertType );

    alert.setDialogPane( new ButtonOrderPane() );
    alert.setTitle( message.getTitle() );
    alert.setHeaderText( null );
    alert.setContentText( message.getContent() );
    alert.initOwner( parent );
    alert.setGraphic( ICON_DIALOG_NODE );

    return alert;
  }
}
