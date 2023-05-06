/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Responsible for converting various data types into other representations.
 */
public final class DataTypeConverter {
  private static final byte[] HEX = "0123456789ABCDEF".getBytes( US_ASCII );

  /**
   * Returns a hexadecimal number that represents the bit sequences provided
   * in the given array of bytes.
   *
   * @param bytes The bytes to convert to a hexadecimal string.
   * @return An uppercase-encoded hexadecimal number.
   */
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

  /**
   * Hashes a string using the SHA-1 algorithm.
   *
   * @param s The string to has.
   * @return The hashed string.
   * @throws NoSuchAlgorithmException Could not find the SHA-1 algorithm.
   */
  public static byte[] hash( final String s ) throws NoSuchAlgorithmException {
    final var digest = MessageDigest.getInstance( "SHA-1" );
    return digest.digest( s.getBytes( UTF_8 ) );
  }
}
