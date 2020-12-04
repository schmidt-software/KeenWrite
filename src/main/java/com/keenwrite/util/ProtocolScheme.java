/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

/**
 * Represents the type of data encoding scheme used for a universal resource
 * indicator.
 */
public enum ProtocolScheme {
  /**
   * Denotes either HTTP or HTTPS.
   */
  HTTP,
  /**
   * Denotes a local file.
   */
  FILE,
  /**
   * Could not determine schema (or is not supported by the application).
   */
  UNKNOWN;

  /**
   * Answers {@code true} if the given protocol is either HTTP or HTTPS.
   *
   * @return {@code true} the protocol is either HTTP or HTTPS.
   */
  public boolean isHttp() {
    return this == HTTP;
  }

  /**
   * Answers {@code true} if the given protocol is for a local file.
   *
   * @return {@code true} the protocol is for a local file reference.
   */
  public boolean isFile() {
    return this == FILE;
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
