/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.preview.HtmlPanelImpl;

import java.net.URI;

/**
 * Collates information about a file requested to be opened. This can be called
 * when the user clicks a hyperlink in the {@link HtmlPanelImpl}.
 */
public class FileOpenEvent implements AppEvent {
  private final URI mUri;

  private FileOpenEvent( final URI uri ) {
    assert uri != null;
    mUri = uri;
  }

  /**
   * Fires a new file open event using the given {@link URI} instance.
   *
   * @param uri The instance of {@link URI} to open as a file in a text editor.
   */
  public static void fireFileOpenEvent( final URI uri ) {
    new FileOpenEvent( uri ).fire();
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
