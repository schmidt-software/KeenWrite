/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events;

import java.io.IOException;
import java.net.URI;

import static com.keenwrite.events.StatusEvent.clue;

/**
 * Collates information about a URL requested to be opened.
 */
public class HyperlinkOpenEvent implements AppEvent {
  private final URI mUri;

  private HyperlinkOpenEvent( final URI uri ) {
    mUri = uri;
  }

  /**
   * Requests to open the default browser at the given location.
   *
   * @param uri The location to open.
   */
  public static void fire( final URI uri )
    throws IOException {
    new HyperlinkOpenEvent( uri ).publish();
  }

  /**
   * Requests to open the default browser at the given location.
   *
   * @param uri The location to open.
   */
  public static void fire( final String uri ) {
    try {
      fire( new URI( uri ) );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Returns the requested resource to be opened.
   *
   * @return A reference that can be opened in a web browser.
   */
  public URI getUri() {
    return mUri;
  }
}
