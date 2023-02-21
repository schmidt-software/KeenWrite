/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.keenwrite.io.MediaType.*;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;

/**
 * Associates file signatures with IANA-defined {@link MediaType}s. See:
 * <a href="https://www.garykessler.net/library/file_sigs.html">
 * Gary Kessler's List
 * </a>,
 * <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">
 * Wikipedia's List
 * </a>, and
 * <a href="https://github.com/veniware/Space-Maker/blob/master/FileSignatures.cs">
 * Space Maker's List
 * </a>
 */
public class MediaTypeSniffer {
  /**
   * The maximum buffer size of magic bytes to analyze.
   */
  private static final int BUFFER = 12;

  /**
   * The media type data can have any value at a corresponding offset.
   */
  private static final int ANY = -1;

  /**
   * Denotes there are fewer than {@link #BUFFER} bytes to compare.
   */
  private static final int EOS = -2;

  private static final Map<int[], MediaType> FORMAT = new LinkedHashMap<>();

  private static void put( final int[] data, final MediaType mediaType ) {
    FORMAT.put( data, mediaType );
  }

  /* The insertion order attempts to approximate the real-world likelihood of
   * encountering particular file formats in an application.
   */
  static {
    //@formatter:off
    put( ints( 0x3C, 0x73, 0x76, 0x67, 0x20 ), IMAGE_SVG_XML );
    put( ints( 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A ), IMAGE_PNG );
    put( ints( 0xFF, 0xD8, 0xFF, 0xE0 ), IMAGE_JPEG );
    put( ints( 0xFF, 0xD8, 0xFF, 0xEE ), IMAGE_JPEG );
    put( ints( 0xFF, 0xD8, 0xFF, 0xE1, ANY, ANY, 0x45, 0x78, 0x69, 0x66, 0x00 ), IMAGE_JPEG );
    put( ints( 0x3C, 0x21 ), TEXT_HTML );
    put( ints( 0x3C, 0x68, 0x74, 0x6D, 0x6C ), TEXT_HTML );
    put( ints( 0x3C, 0x68, 0x65, 0x61, 0x64 ), TEXT_HTML );
    put( ints( 0x3C, 0x62, 0x6F, 0x64, 0x79 ), TEXT_HTML );
    put( ints( 0x3C, 0x48, 0x54, 0x4D, 0x4C ), TEXT_HTML );
    put( ints( 0x3C, 0x48, 0x45, 0x41, 0x44 ), TEXT_HTML );
    put( ints( 0x3C, 0x42, 0x4F, 0x44, 0x59 ), TEXT_HTML );
    put( ints( 0x3C, 0x3F, 0x78, 0x6D, 0x6C, 0x20 ), TEXT_XML );
    put( ints( 0xFE, 0xFF, 0x00, 0x3C, 0x00, 0x3f, 0x00, 0x78 ), TEXT_XML );
    put( ints( 0xFF, 0xFE, 0x3C, 0x00, 0x3F, 0x00, 0x78, 0x00 ), TEXT_XML );
    put( ints( 0x47, 0x49, 0x46, 0x38 ), IMAGE_GIF );
    put( ints( 0x42, 0x4D ), IMAGE_BMP );
    put( ints( 0x49, 0x49, 0x2A, 0x00 ), IMAGE_TIFF );
    put( ints( 0x4D, 0x4D, 0x00, 0x2A ), IMAGE_TIFF );
    put( ints( 0x52, 0x49, 0x46, 0x46, ANY, ANY, ANY, ANY, 0x57, 0x45, 0x42, 0x50 ), IMAGE_WEBP );
    put( ints( 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E ), APP_PDF );
    put( ints( 0x25, 0x21, 0x50, 0x53, 0x2D, 0x41, 0x64, 0x6F, 0x62, 0x65, 0x2D ), APP_EPS );
    put( ints( 0x25, 0x21, 0x50, 0x53 ), APP_PS );
    put( ints( 0x38, 0x42, 0x50, 0x53, 0x00, 0x01 ), IMAGE_PHOTOSHOP );
    put( ints( 0xFF, 0xFB, 0x30 ), AUDIO_MP3 );
    put( ints( 0x49, 0x44, 0x33 ), AUDIO_MP3 );
    put( ints( 0x8A, 0x4D, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A ), VIDEO_MNG );
    put( ints( 0x23, 0x64, 0x65, 0x66 ), IMAGE_X_BITMAP );
    put( ints( 0x21, 0x20, 0x58, 0x50, 0x4D, 0x32 ), IMAGE_X_PIXMAP );
    put( ints( 0x2E, 0x73, 0x6E, 0x64 ), AUDIO_SIMPLE );
    put( ints( 0x64, 0x6E, 0x73, 0x2E ), AUDIO_SIMPLE );
    put( ints( 0x52, 0x49, 0x46, 0x46 ), AUDIO_WAV );
    put( ints( 0x50, 0x4B ), APP_ZIP );
    put( ints( 0x41, 0x43, ANY, ANY, ANY, ANY, 0x00, 0x00, 0x00, 0x00, 0x00 ), APP_ACAD );
    put( ints( 0xCA, 0xFE, 0xBA, 0xBE ), APP_JAVA );
    put( ints( 0xAC, 0xED ), APP_JAVA_OBJECT );
    //@formatter:on
  }

  /**
   * Returns the {@link MediaType} for a given set of bytes.
   *
   * @param data Binary data to compare against the list of known formats.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   */
  public static MediaType getMediaType( final byte[] data ) {
    assert data != null;
    assert data.length > 0;

    final var source = new int[]{
      0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
      0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
      0xFF, 0XFF, EOS
    };

    for( int i = 0; i < Math.min( data.length, source.length ); i++ ) {
      source[ i ] = data[ i ] & 0xFF;
    }

    for( final var key : FORMAT.keySet() ) {
      int i = -1;
      boolean matches = true;

      while( ++i < BUFFER && key[ i ] != EOS && matches ) {
        matches = key[ i ] == source[ i ] || key[ i ] == ANY;
      }

      if( matches ) {
        return FORMAT.get( key );
      }
    }

    return UNDEFINED;
  }

  /**
   * Convenience method to return the probed media type for the given
   * {@link SysFile} instance by delegating to
   * {@link #getMediaType(InputStream)}.
   *
   * @param file File to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link File}.
   */
  public static MediaType getMediaType( final File file )
    throws IOException {
    try( final var fis = new FileInputStream( file ) ) {
      return getMediaType( fis );
    }
  }

  /**
   * Convenience method to return the probed media type for the given
   * {@link BufferedInputStream} instance. <strong>This resets the stream
   * pointer</strong> making the call idempotent. Prefer calling this
   * method when operating on streams to avoid advancing the stream.
   *
   * @param bis Data source to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the stream.
   */
  public static MediaType getMediaType( final BufferedInputStream bis )
    throws IOException {
    bis.mark( BUFFER );
    final var result = getMediaType( (InputStream) bis );
    bis.reset();

    return result;
  }

  /**
   * Returns the probed media type for the given {@link InputStream} instance.
   * The caller is responsible for closing the stream. <strong>This advances
   * the stream.</strong> Use {@link #getMediaType(BufferedInputStream)} to
   * perform a non-destructive read.
   *
   * @param is Data source to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link InputStream}.
   */
  private static MediaType getMediaType( final InputStream is )
    throws IOException {
    final var input = new byte[ BUFFER ];
    final var count = is.read( input, 0, BUFFER );

    if( count > 1 ) {
      final var available = new byte[ count ];
      arraycopy( input, 0, available, 0, count );
      return getMediaType( available );
    }

    return UNDEFINED;
  }

  /**
   * Creates an integer array from the given data, padded with {@link #EOS}
   * values up to {@link #BUFFER} in length.
   *
   * @param data The input byte values to pad.
   * @return The data with padding.
   */
  private static int[] ints( final int... data ) {
    assert data != null;

    final var magic = new int[ data.length + 1 ];

    fill( magic, EOS );
    arraycopy( data, 0, magic, 0, data.length );

    return magic;
  }
}
