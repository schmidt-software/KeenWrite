/* Copyright 2024 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.util;

import org.mozilla.universalchardet.UniversalDetector;

import java.nio.charset.Charset;

import static com.keenwrite.constants.Constants.DEFAULT_CHARSET;
import static java.nio.charset.Charset.forName;
import static java.util.Locale.ENGLISH;

/**
 * Wraps the {@link UniversalDetector} with to provide enhanced abilities
 * and bug fixes (if needed).
 */
public class EncodingDetector {

  private final UniversalDetector mDetector;

  public EncodingDetector() {
    mDetector = new UniversalDetector( null );
  }

  /**
   * Returns the character set for the constructed input. This will coerce
   * both US-ASCII and TIS620 to UTF-8.
   *
   * @param bytes The textual content having an as yet unknown encoding.
   * @return The character encoding for the given bytes.
   */
  public Charset detect( final byte[] bytes ) {
    mDetector.handleData( bytes, 0, bytes.length );
    mDetector.dataEnd();

    final String detectedCharset = mDetector.getDetectedCharset();

    // TODO: Revert when the issue has been fixed.
    // https://github.com/albfernandez/juniversalchardet/issues/35
    return switch( detectedCharset ) {
      case null -> DEFAULT_CHARSET;
      case "US-ASCII", "TIS620" -> DEFAULT_CHARSET;
      default -> forName( detectedCharset.toUpperCase( ENGLISH ) );
    };
  }
}
