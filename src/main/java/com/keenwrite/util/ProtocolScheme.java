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

  /**
   * Determines the protocol scheme for a given string.
   *
   * @param protocol A string representing data encoding protocol scheme.
   * @return {@link #UNKNOWN} if the protocol is unrecognized, otherwise a
   * valid value from this enumeration.
   */
  public static ProtocolScheme valueFrom( final String protocol ) {
    final var sanitized = sanitize( protocol );

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
   * Returns an empty string if the given string to sanitize is {@code null},
   * otherwise the given string in uppercase. Uppercase is used to align with
   * the enum name.
   *
   * @param s The string to sanitize, may be {@code null}.
   * @return A non-{@code null} string.
   */
  private static String sanitize( final String s ) {
    return s == null ? "" : s.toUpperCase();
  }
}
