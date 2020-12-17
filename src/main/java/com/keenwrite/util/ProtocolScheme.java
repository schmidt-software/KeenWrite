/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

/**
 * Represents the type of data encoding scheme used for a universal resource
 * indicator. Prefer to use the {@code is*} methods to check equality because
 * there are cases where the protocol represents more than one possible type
 * (e.g., a Java Archive is a file, so comparing {@link #FILE} directly could
 * lead to incorrect results).
 */
public enum ProtocolScheme {
  /**
   * Denotes a local file.
   */
  FILE,
  /**
   * Denotes either HTTP or HTTPS.
   */
  HTTP,
  /**
   * Denotes Java archive file.
   */
  JAR,
  /**
   * Could not determine schema (or is not supported by the application).
   */
  UNKNOWN;

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

  /**
   * Determines the protocol scheme for a given string.
   *
   * @param protocol A string representing data encoding protocol scheme.
   * @return {@link #UNKNOWN} if the protocol is unrecognized, otherwise a
   * valid value from this enumeration.
   */
  public static ProtocolScheme valueFrom( final String protocol ) {
    final var sanitized = protocol == null ? "" : protocol.toUpperCase();

    for( final var scheme : values() ) {
      // This will match HTTP/HTTPS as well as FILE*, which may be inaccurate.
      if( sanitized.startsWith( scheme.name() ) ) {
        return scheme;
      }
    }

    return UNKNOWN;
  }

  /**
   * Determines the protocol scheme for a given {@link File}.
   *
   * @param file A file having a URI that contains a protocol scheme.
   * @return {@link #UNKNOWN} if the protocol is unrecognized, otherwise a
   * valid value from this enumeration.
   */
  public static ProtocolScheme valueFrom( final File file ) {
    return valueFrom( file.toURI() );
  }

  /**
   * Determines the protocol scheme for a given {@link URI}.
   *
   * @param uri A URI that contains a protocol scheme.
   * @return {@link #UNKNOWN} if the protocol is unrecognized, otherwise a
   * valid value from this enumeration.
   */
  public static ProtocolScheme valueFrom( final URI uri ) {
    try {
      return valueFrom( uri.toURL() );
    } catch( final Exception ex ) {
      return UNKNOWN;
    }
  }

  /**
   * Determines the protocol scheme for a given {@link URL}.
   *
   * @param url A {@link URL} that contains a protocol scheme.
   * @return {@link #UNKNOWN} if the protocol is unrecognized, otherwise a
   * valid value from this enumeration.
   */
  public static ProtocolScheme valueFrom( final URL url ) {
    return valueFrom( url.getProtocol() );
  }

  /**
   * Answers {@code true} if the given protocol is for a local file, which
   * includes a JAR file.
   *
   * @return {@code false} the protocol is not a local file reference.
   */
  public boolean isFile() {
    return this == FILE || this == JAR;
  }

  /**
   * Answers {@code true} if the given protocol is either HTTP or HTTPS.
   *
   * @return {@code true} the protocol is either HTTP or HTTPS.
   */
  public boolean isHttp() {
    return this == HTTP;
  }

  /**
   * Answers {@code true} if the given protocol is for a Java archive file.
   *
   * @return {@code false} the protocol is not a Java archive file.
   */
  public boolean isJar() {
    return this == JAR;
  }
}
