/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.io.downloads.events;

import java.net.URL;

/**
 * Collates information about an HTTP connection that could not be established.
 */
public class DownloadConnectionFailedEvent extends DownloadEvent {

  private final Exception mEx;

  /**
   * Constructs a new event that tracks the status of downloading a file.
   *
   * @param url The {@link URL} that has triggered a download event.
   * @param ex  The reason the connection failed.
   */
  public DownloadConnectionFailedEvent(
    final URL url, final Exception ex ) {
    super( url );
    mEx = ex;
  }

  public static void fire( final URL url, final Exception ex ) {
    new DownloadConnectionFailedEvent( url, ex ).publish();
  }

  /**
   * Returns the {@link Exception} that caused this event to be published.
   *
   * @return The {@link Exception} encountered when establishing a connection.
   */
  public Exception getException() {
    return mEx;
  }
}
