/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.io.downloads.events;

import java.net.URL;

public class DownloadFailedEvent extends DownloadEvent {

  private final int mResponseCode;

  /**
   * Constructs a new event that indicates downloading a file was not
   * successful.
   *
   * @param url          The {@link URL} that has triggered a download event.
   * @param responseCode The HTTP response code associated with the failure.
   */
  public DownloadFailedEvent( final URL url, final int responseCode ) {
    super( url );

    mResponseCode = responseCode;
  }

  public static void fire( final URL url, final int responseCode ) {
    new DownloadFailedEvent( url, responseCode ).publish();
  }

  /**
   * Returns the HTTP response code for a failed download.
   *
   * @return An HTTP response code.
   */
  public int getResponseCode() {
    return mResponseCode;
  }
}
