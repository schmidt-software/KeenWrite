package com.scrivenvar.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static com.scrivenvar.Constants.DEFINITION_PROTOCOL_UNKNOWN;

/**
 * Responsible for determining the protocol of a resource.
 */
public class ProtocolResolver {
  /**
   * Returns the protocol for a given URI or filename.
   *
   * @param resource Determine the protocol for this URI or filename.
   * @return The protocol for the given source.
   */
  public static String getProtocol( final String resource ) {
    String protocol;

    try {
      final URI uri = new URI( resource );

      if( uri.isAbsolute() ) {
        protocol = uri.getScheme();
      }
      else {
        final URL url = new URL( resource );
        protocol = url.getProtocol();
      }
    } catch( final Exception e ) {
      // Could be HTTP, HTTPS?
      if( resource.startsWith( "//" ) ) {
        throw new IllegalArgumentException( "Relative context: " + resource );
      }
      else {
        final File file = new File( resource );
        protocol = getProtocol( file );
      }
    }

    return protocol;
  }

  /**
   * Returns the protocol for a given file.
   *
   * @param file Determine the protocol for this file.
   * @return The protocol for the given file.
   */
  public static String getProtocol( final File file ) {
    String result;

    try {
      result = file.toURI().toURL().getProtocol();
    } catch( final Exception e ) {
      result = DEFINITION_PROTOCOL_UNKNOWN;
    }

    return result;
  }
}
