/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.preview.HtmlPanel;

import java.net.URI;

/**
 * Collates information about a file requested to be opened. This can be called
 * when the user clicks a hyperlink in the {@link HtmlPanel}.
 */
public class FileOpenEvent implements AppEvent {
  private final URI mUri;

  /**
   * Creates a new event using an instance of {@link URI}.
   *
   * @param uri The instance of {@link URI} to open as a file in a text editor.
   */
  public FileOpenEvent( final URI uri ) {
    assert uri != null;
    mUri = uri;
  }

  /**
   * Returns the requested file name to be opened.
   *
   * @return A file reference that can be opened in a text editor.
   */
  public URI getUri() {
    return mUri;
  }
}
