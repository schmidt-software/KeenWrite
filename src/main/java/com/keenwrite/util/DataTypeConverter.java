/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for converting various data types into other representations.
 */
public final class DataTypeConverter {
  private static final byte[] HEX = "0123456789ABCDEF".getBytes( US_ASCII );

  public static String toHex( final byte[] bytes ) {
    final var hexChars = new byte[ bytes.length * 2 ];
    final var len = bytes.length;

    for( var i = 0; i < len; i++ ) {
      final var digit = bytes[ i ] & 0xFF;

      hexChars[ (i << 1) ] = HEX[ digit >>> 4 ];
      hexChars[ (i << 1) + 1 ] = HEX[ digit & 0x0F ];
    }

    return new String( hexChars, UTF_8 );
  }
}
