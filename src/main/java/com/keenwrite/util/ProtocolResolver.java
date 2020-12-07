/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static com.keenwrite.util.ProtocolScheme.HTTP;
import static com.keenwrite.util.ProtocolScheme.valueFrom;

/**
 * Responsible for determining the protocol of a resource.
 */
public class ProtocolResolver {
  /**
   * Returns the protocol for a given URI or filename.
   *
   * @param resource Determine the protocol for this URI or filename.
   * @return The protocol for the given resource.
   */
  public static ProtocolScheme getProtocol( final String resource ) {
    try {
      final var uri = new URI( resource );
      return uri.isAbsolute()
          ? valueFrom( uri )
          : valueFrom( new URL( resource ) );
    } catch( final Exception ex ) {
      // Using double-slashes is a short-hand to instruct the browser to
      // reference a resource using the parent URL's security model. This
      // is known as a protocol-relative URL.
      return resource.startsWith( "//" )
          ? HTTP
          : valueFrom( new File( resource ) );
    }
  }
}
