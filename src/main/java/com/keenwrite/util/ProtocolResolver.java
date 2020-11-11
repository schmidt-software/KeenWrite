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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static com.keenwrite.util.ProtocolScheme.UNKNOWN;

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
      final URI uri = new URI( resource );
      return ProtocolScheme.valueFrom(
          uri.isAbsolute()
              ? uri.getScheme()
              : new URL( resource ).getProtocol()
      );
    } catch( final Exception e ) {
      // Using double-slashes is a short-hand to instruct the browser to
      // reference a resource using the parent URL's security model. This
      // is known as a protocol-relative URL.
      return resource.startsWith( "//" )
          ? ProtocolScheme.HTTP
          : getProtocol( new File( resource ) );
    }
  }

  /**
   * Returns the protocol for a given file.
   *
   * @param file Determine the protocol for this file.
   * @return The protocol for the given file, or {@link ProtocolScheme#UNKNOWN}
   * if the protocol cannot be determined.
   */
  private static ProtocolScheme getProtocol( final File file ) {
    try {
      return ProtocolScheme.valueFrom( file.toURI().toURL().getProtocol() );
    } catch( final MalformedURLException ex ) {
      // Return a protocol guaranteed to be undefined.
      return UNKNOWN;
    }
  }
}
