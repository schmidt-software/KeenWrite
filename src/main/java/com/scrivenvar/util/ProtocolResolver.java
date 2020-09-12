package com.scrivenvar.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static com.scrivenvar.Constants.DEFINITION_PROTOCOL_UNKNOWN;

/**
 * Responsible for determining the protocol of a resource.
 */
public class ProtocolResolver {
  private static final String SCHEME_HTTP = "http";
  private static final String SCHEME_FILE = "file";

  /**
   * Answers {@code true} if the given protocol is either HTTP or HTTPS.
   *
   * @param protocol The protocol to compare against the web URI scheme.
   * @return {@code true} the protocol is either HTTP or HTTPS.
   */
  public static boolean isHttp( final String protocol ) {
    return sanitize( protocol ).startsWith( SCHEME_HTTP );
  }

  /**
   * Answers {@code true} if the given protocol is for a local file.
   *
   * @param protocol The protocol to compare against the file URI scheme.
   * @return {@code true} the protocol is for a local file reference.
   */
  public static boolean isFile( String protocol ) {
    return sanitize( protocol ).startsWith( SCHEME_FILE );
  }

  /**
   * Returns the protocol for a given URI or filename.
   *
   * @param resource Determine the protocol for this URI or filename.
   * @return The protocol for the given resource.
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

  /**
   * Returns an empty string if the given string to sanitize is {@code null},
   * otherwise the given string in lowercase.
   *
   * @param s The string to sanitize, may be {@code null}.
   * @return A non-{@code null} string.
   */
  private static String sanitize( final String s ) {
    return s == null ? "" : s.toLowerCase();
  }
}
