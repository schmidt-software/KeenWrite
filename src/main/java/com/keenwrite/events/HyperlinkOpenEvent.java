/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
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
  public static void fireHyperlinkOpenEvent( final URI uri )
    throws IOException {
    new HyperlinkOpenEvent( uri ).fire();
  }

  /**
   * Requests to open the default browser at the given location.
   *
   * @param uri The location to open.
   */
  public static void fireHyperlinkOpenEvent( final String uri ) {
    try {
      fireHyperlinkOpenEvent( new URI( uri ) );
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
