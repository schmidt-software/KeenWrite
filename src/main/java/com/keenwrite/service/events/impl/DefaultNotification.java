/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events.impl;

import com.keenwrite.service.events.Notification;

import java.text.MessageFormat;

/**
 * Responsible for alerting the user to prominent information.
 */
public class DefaultNotification implements Notification {

  private final String mTitle;
  private final String mContent;

  /**
   * Constructs default message text for a notification.
   *
   * @param title   The message title.
   * @param message The message content (needs formatting).
   * @param args    The arguments to the message content that must be formatted.
   */
  public DefaultNotification(
      final String title,
      final String message,
      final Object... args ) {
    mTitle = title;
    mContent = MessageFormat.format( message, args );
  }

  @Override
  public String getTitle() {
    return mTitle;
  }

  @Override
  public String getContent() {
    return mContent;
  }

}
