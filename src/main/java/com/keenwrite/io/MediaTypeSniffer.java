package com.keenwrite.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.keenwrite.io.MediaType.*;
import static java.lang.System.arraycopy;

/**
 * Responsible for associating file signatures with IANA-defined
 * {@link MediaType} instances. For details see:
 * <ul>
 *   <li>
 *     <a href="https://www.garykessler.net/library/file_sigs.html">Kessler's List</a>
 *   </li>
 *   <li>
 *     <a href="https://en.wikipedia.org/wiki/List_of_file_signatures">Wikipedia's List</a>
 *   </li>
 *   <li>
 *     <a href="https://github.com/veniware/Space-Maker/blob/master/FileSignatures.cs">Space Maker's List</a>
 *   </li>
 * </ul>
 */
public class MediaTypeSniffer {
  private static final int FORMAT_LENGTH = 11;
  private static final int END_OF_DATA = -2;

  private static final Map<int[], MediaType> FORMAT = new LinkedHashMap<>();

  static {
    //@formatter:off
    FORMAT.put( ints( 0x3C, 0x73, 0x76, 0x67, 0x20 ), IMAGE_SVG_XML );
    FORMAT.put( ints( 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A ), IMAGE_PNG );
    FORMAT.put( ints( 0xFF, 0xD8, 0xFF, 0xE0 ), IMAGE_JPEG );
    FORMAT.put( ints( 0xFF, 0xD8, 0xFF, 0xEE ), IMAGE_JPEG );
    FORMAT.put( ints( 0xFF, 0xD8, 0xFF, 0xE1, -1, -1, 0x45, 0x78, 0x69, 0x66, 0x00 ), IMAGE_JPEG );
    FORMAT.put( ints( 0x49, 0x49, 0x2A, 0x00 ), IMAGE_TIFF );
    FORMAT.put( ints( 0x4D, 0x4D, 0x00, 0x2A ), IMAGE_TIFF );
    FORMAT.put( ints( 0x47, 0x49, 0x46, 0x38 ), IMAGE_GIF );
    FORMAT.put( ints( 0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E ), APP_PDF );
    FORMAT.put( ints( 0x25, 0x21, 0x50, 0x53, 0x2D, 0x41, 0x64, 0x6F, 0x62, 0x65, 0x2D ), APP_EPS );
    FORMAT.put( ints( 0x25, 0x21, 0x50, 0x53 ), APP_PS );
    FORMAT.put( ints( 0x38, 0x42, 0x50, 0x53, 0x00, 0x01 ), IMAGE_PHOTOSHOP );
    FORMAT.put( ints( 0x8A, 0x4D, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A ), VIDEO_MNG );
    FORMAT.put( ints( 0x42, 0x4D ), IMAGE_BMP );
    FORMAT.put( ints( 0xFF, 0xFB, 0x30 ), AUDIO_MP3 );
    FORMAT.put( ints( 0x49, 0x44, 0x33 ), AUDIO_MP3 );
    FORMAT.put( ints( 0x3C, 0x21 ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x68, 0x74, 0x6D, 0x6C ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x68, 0x65, 0x61, 0x64 ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x62, 0x6F, 0x64, 0x79 ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x48, 0x54, 0x4D, 0x4C ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x48, 0x45, 0x41, 0x44 ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x42, 0x4F, 0x44, 0x59 ), TEXT_HTML );
    FORMAT.put( ints( 0x3C, 0x3F, 0x78, 0x6D, 0x6C, 0x20 ), TEXT_XML );
    FORMAT.put( ints( 0xFE, 0xFF, 0x00, 0x3C, 0x00, 0x3f, 0x00, 0x78 ), TEXT_XML );
    FORMAT.put( ints( 0xFF, 0xFE, 0x3C, 0x00, 0x3F, 0x00, 0x78, 0x00 ), TEXT_XML );
    FORMAT.put( ints( 0x23, 0x64, 0x65, 0x66 ), IMAGE_X_BITMAP );
    FORMAT.put( ints( 0x21, 0x20, 0x58, 0x50, 0x4D, 0x32 ), IMAGE_X_PIXMAP );
    FORMAT.put( ints( 0x2E, 0x73, 0x6E, 0x64 ), AUDIO_BASIC );
    FORMAT.put( ints( 0x64, 0x6E, 0x73, 0x2E ), AUDIO_BASIC );
    FORMAT.put( ints( 0x52, 0x49, 0x46, 0x46 ), AUDIO_WAV );
    FORMAT.put( ints( 0x50, 0x4B ), APP_ZIP );
    FORMAT.put( ints( 0x41, 0x43, -1, -1, -1, -1, 0x00, 0x00, 0x00, 0x00, 0x00 ), APP_ACAD );
    FORMAT.put( ints( 0xCA, 0xFE, 0xBA, 0xBE ), APP_JAVA );
    FORMAT.put( ints( 0xAC, 0xED ), APP_JAVA_OBJECT );
    //@formatter:on
  }

  private MediaTypeSniffer() {
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

    final var source = new int[]{
      0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF};

    for( int i = 0; i < data.length; i++ ) {
      source[ i ] = data[ i ] & 0xFF;
    }

    for( final var key : FORMAT.keySet() ) {
      int i = -1;
      boolean matches = true;

      while( ++i < FORMAT_LENGTH && key[ i ] != END_OF_DATA && matches ) {
        matches = key[ i ] == source[ i ] || key[ i ] == -1;
      }

      if( matches ) {
        return FORMAT.get( key );
      }
    }

    return UNDEFINED;
  }

  /**
   * Convenience method to return the probed media type for the given
   * {@link Path} instance by delegating to {@link #getMediaType(InputStream)}.
   *
   * @param path Path to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link File}.
   */
  public static MediaType getMediaType( final Path path ) throws IOException {
    return getMediaType( path.toFile() );
  }

  /**
   * Convenience method to return the probed media type for the given
   * {@link File} instance by delegating to {@link #getMediaType(InputStream)}.
   *
   * @param file File to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link File}.
   */
  public static MediaType getMediaType( final java.io.File file )
    throws IOException {
    try( final var fis = new FileInputStream( file ) ) {
      return getMediaType( fis );
    }
  }

  /**
   * Convenience method to return the probed media type for the given
   * {@link BufferedInputStream} instance. <strong>This resets the stream
   * pointer</strong> making the call idempotent. Users of this class should
   * prefer to call this method when operating on streams to avoid advancing
   * the stream.
   *
   * @param bis Data source to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link File}.
   */
  public static MediaType getMediaType( final BufferedInputStream bis )
    throws IOException {
    bis.mark( FORMAT_LENGTH );
    final var result = getMediaType( (InputStream) bis );
    bis.reset();

    return result;
  }

  /**
   * Helper method to return the probed media type for the given
   * {@link InputStream} instance. The caller is responsible for closing
   * the stream. <strong>This advances the stream pointer.</strong>
   *
   * @param is Data source to ascertain the {@link MediaType}.
   * @return The IANA-defined {@link MediaType}, or
   * {@link MediaType#UNDEFINED} if indeterminate.
   * @throws IOException Could not read from the {@link InputStream}.
   * @see #getMediaType(BufferedInputStream) to perform a non-destructive
   * read.
   */
  private static MediaType getMediaType( final InputStream is )
    throws IOException {
    final var input = new byte[ FORMAT_LENGTH ];
    final var count = is.read( input, 0, FORMAT_LENGTH );

    if( count > 1 ) {
      final var available = new byte[ count ];
      arraycopy( input, 0, available, 0, count );
      return getMediaType( available );
    }

    return UNDEFINED;
  }

  /**
   * Creates an array of integers from the given data, padded with {@link
   * #END_OF_DATA} values up to {@link #FORMAT_LENGTH}.
   *
   * @param data The input byte values to pad.
   * @return The data with padding.
   */
  private static int[] ints( final int... data ) {
    final var magic = new int[ FORMAT_LENGTH ];
    int i = -1;
    while( ++i < data.length ) {
      magic[ i ] = data[ i ];
    }

    while( i < FORMAT_LENGTH ) {
      magic[ i++ ] = END_OF_DATA;
    }

    return magic;
  }
}
