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
package com.scrivenvar.service.events;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Provides the application with a uniform way to notify the user of events.
 *
 * @author White Magic Software, Ltd.
 */
public interface NotifyService {
  public static final ButtonType YES = ButtonType.YES;
  public static final ButtonType NO = ButtonType.NO;
  public static final ButtonType CANCEL = ButtonType.CANCEL;

  /**
   * Called to set the window used as the parent for the alert dialogs.
   *
   * @param window
   */
  public void setWindow( Window window );

  /**
   * Constructs a default alert message text for a modal alert dialog.
   *
   * @param title The dialog box message title.
   * @param message The dialog box message content (needs formatting).
   * @param args The arguments to the message content that must be formatted.
   *
   * @return The message suitable for building a modal alert dialog.
   */
  public Notification createNotification(
    String title,
    String message,
    Object... args );

  /**
   * Creates an alert of alert type error with a message showing the cause of
   * the error.
   *
   * @param message The error message, title, and possibly more details.
   *
   * @return A modal alert dialog box ready to display using showAndWait.
   */
  public Alert createError( Notification message );

  /**
   * Creates an alert of alert type confirmation with Yes/No/Cancel buttons.
   *
   * @param message The message, title, and possibly more details.
   *
   * @return A modal alert dialog box ready to display using showAndWait.
   */
  public Alert createConfirmation( Notification message );
}
