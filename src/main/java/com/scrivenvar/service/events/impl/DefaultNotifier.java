/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.service.events.impl;

import static com.scrivenvar.Constants.STATUS_BAR_DEFAULT;
import com.scrivenvar.service.events.Notification;
import com.scrivenvar.service.events.Notifier;
import java.util.Observable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import static javafx.scene.control.Alert.AlertType.CONFIRMATION;
import static javafx.scene.control.Alert.AlertType.ERROR;
import javafx.stage.Window;

/**
 * Provides the ability to notify the user of problems.
 *
 * @author White Magic Software, Ltd.
 */
public final class DefaultNotifier extends Observable
  implements Notifier {

  public DefaultNotifier() {
  }

  /**
   * Notifies all observer instances of the given message.
   *
   * @param message The text to display to the user.
   */
  @Override
  public void notify( final String message ) {
    setChanged();
    notifyObservers( message );
  }

  /**
   * Contains all the information that the user needs to know about a problem.
   *
   * @param title The context for the message.
   * @param message The message content (formatted with the given args).
   * @param args Parameters for the message content.
   *
   * @return
   */
  @Override
  public Notification createNotification(
    final String title,
    final String message,
    final Object... args ) {
    return new DefaultNotification( title, message, args );
  }

  @Override
  public void clear() {
    setChanged();
    notifyObservers( STATUS_BAR_DEFAULT );
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
  public Alert createConfirmation( final Window parent, final Notification message ) {
    final Alert alert = createAlertDialog( parent, CONFIRMATION, message );

    alert.getButtonTypes().setAll( YES, NO, CANCEL );

    return alert;
  }

  @Override
  public Alert createError( final Window parent, final Notification message ) {
    return createAlertDialog( parent, ERROR, message );
  }
}
