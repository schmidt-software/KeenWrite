/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
  public static ProtocolScheme valueFrom( String protocol ) {
    ProtocolScheme result = UNKNOWN;
    protocol = sanitize( protocol );

    for( final var scheme : values() ) {
      // This will match HTTP/HTTPS as well as FILE*, which may be inaccurate.
      if( protocol.startsWith( scheme.name() ) ) {
        result = scheme;
        break;
      }
    }

    return result;
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
