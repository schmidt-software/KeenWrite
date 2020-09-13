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
package com.scrivenvar.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.scrivenvar.util.ProtocolScheme.UNKNOWN;

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

    return ProtocolScheme.valueFrom( protocol );
  }

  /**
   * Returns the protocol for a given file.
   *
   * @param file Determine the protocol for this file.
   * @return The protocol for the given file.
   */
  private static String getProtocol( final File file ) {
    String result;

    try {
      result = file.toURI().toURL().getProtocol();
    } catch( final MalformedURLException ex ) {
      // Value guaranteed to avoid identification as a standard protocol.
      result = UNKNOWN.toString();
    }

    return result;
  }
}
