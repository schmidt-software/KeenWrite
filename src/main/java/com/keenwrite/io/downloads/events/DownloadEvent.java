/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io.downloads.events;

import com.keenwrite.events.AppEvent;

import java.net.URL;
import java.time.Instant;

/**
 * The parent class to all download-related status events.
 */
public class DownloadEvent implements AppEvent {

  private final Instant mInstant = Instant.now();
  private final URL mUrl;

  /**
   * Constructs a new event that tracks the status of downloading a file.
   *
   * @param url The {@link URL} that has triggered a download event.
   */
  public DownloadEvent( final URL url ) {
    mUrl = url;
  }

  /**
   * Returns the download link as an instance of {@link URL}.
   *
   * @return The {@link URL} being downloaded.
   */
  public URL getUrl() {
    return mUrl;
  }

  /**
   * Returns the moment in time that this event was published.
   *
   * @return The published date and time.
   */
  public Instant when() {
    return mInstant;
  }
}
