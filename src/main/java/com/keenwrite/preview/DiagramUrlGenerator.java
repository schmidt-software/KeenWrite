/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

import static java.lang.String.format;
import static java.util.Base64.getUrlEncoder;

/**
 * Responsible for transforming text-based diagram descriptions into URLs
 * that the HTML renderer can embed as SVG images.
 */
public class DiagramUrlGenerator {
  private DiagramUrlGenerator() {
  }

  /**
   * Returns a URL that can be embedded as the {@code src} attribute to an HTML
   * {@code img} tag.
   *
   * @param server  Name of server to use for diagram conversion.
   * @param diagram Diagram type (e.g., Graphviz, Block, PlantUML).
   * @param text    Diagram text that conforms to the diagram type.
   * @return A secure URL string to use as an image {@code src} attribute.
   */
  public static String toUrl(
    final String server, final String diagram, final String text ) {
    return format(
      "https://%s/%s/svg/%s", server, diagram, encode( text )
    );
  }

  /**
   * Convert the plain-text version of the diagram into a URL-encoded value
   * suitable for passing to a web server using an HTTP GET request.
   *
   * @param text The diagram text to encode.
   * @return The URL-encoded (and compressed) version of the text.
   */
  private static String encode( final String text ) {
    return getUrlEncoder().encodeToString(
      compress( text.getBytes( StandardCharsets.UTF_8 ) )
    );
  }

  /**
   * Compresses a sequence of bytes using ZLIB format.
   *
   * @param source The data to compress.
   * @return A lossless, compressed sequence of bytes.
   */
  private static byte[] compress( byte[] source ) {
    final var deflater = new Deflater();
    deflater.setInput( source );
    deflater.finish();

    final var compressed = new byte[ Short.MAX_VALUE ];
    final var size = deflater.deflate( compressed );
    final var result = new byte[ size ];

    System.arraycopy( compressed, 0, result, 0, size );

    return result;
  }
}
