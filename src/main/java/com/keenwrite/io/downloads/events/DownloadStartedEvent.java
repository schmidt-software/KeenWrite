/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.io.downloads.events;

import java.net.URL;

/**
 * Collates information about a document that has started downloading.
 */
public class DownloadStartedEvent extends DownloadEvent {

  public DownloadStartedEvent( final URL url ) {
    super( url );
  }

  public static void fire( final URL url ) {
    new DownloadStartedEvent( url ).publish();
  }
}
