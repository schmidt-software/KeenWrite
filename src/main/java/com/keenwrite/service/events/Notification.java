/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.service.events;

/**
 * Represents a message that contains a title and content.
 */
public interface Notification {

  /**
   * Alert title.
   *
   * @return A non-null string to use as alert message title.
   */
  String getTitle();

  /**
   * Alert message content.
   *
   * @return A non-null string that contains information for the user.
   */
  String getContent();
}
