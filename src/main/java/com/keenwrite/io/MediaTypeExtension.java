/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.util.List;

import static com.keenwrite.io.MediaType.*;
import static java.util.List.of;

/**
 * Responsible for associating file extensions with {@link MediaType} instances.
 * Insertion order must be maintained because the first element in the list
 * represents the file name extension that corresponds to its icon.
 */
public enum MediaTypeExtension {
  MEDIA_APP_ACAD( APP_ACAD, of( "dwg" ) ),
  MEDIA_APP_PDF( APP_PDF ),
  MEDIA_APP_PS( APP_PS, of( "ps" ) ),
  MEDIA_APP_EPS( APP_EPS ),
  MEDIA_APP_ZIP( APP_ZIP ),

  MEDIA_AUDIO_MP3( AUDIO_MP3 ),
  MEDIA_AUDIO_BASIC( AUDIO_BASIC, of( "au" ) ),
  MEDIA_AUDIO_WAV( AUDIO_WAV, of( "wav" ) ),

  MEDIA_FONT_OTF( FONT_OTF ),
  MEDIA_FONT_TTF( FONT_TTF ),

  MEDIA_IMAGE_APNG( IMAGE_APNG ),
  MEDIA_IMAGE_BMP( IMAGE_BMP ),
  MEDIA_IMAGE_GIF( IMAGE_GIF ),
  MEDIA_IMAGE_JPEG( IMAGE_JPEG,
                    of( "jpg", "jpe", "jpeg", "jfif", "pjpeg", "pjp" ) ),
  MEDIA_IMAGE_PNG( IMAGE_PNG ),
  MEDIA_IMAGE_PSD( IMAGE_PHOTOSHOP, of( "psd" ) ),
  MEDIA_IMAGE_SVG( IMAGE_SVG_XML, of( "svg" ) ),
  MEDIA_IMAGE_TIFF( IMAGE_TIFF, of( "tiff", "tif" ) ),
  MEDIA_IMAGE_WEBP( IMAGE_WEBP ),
  MEDIA_IMAGE_X_BITMAP( IMAGE_X_BITMAP, of( "xbm" ) ),
  MEDIA_IMAGE_X_PIXMAP( IMAGE_X_PIXMAP, of( "xpm" ) ),

  MEDIA_VIDEO_MNG( VIDEO_MNG, of( "mng" ) ),

  MEDIA_TEXT_MARKDOWN( TEXT_MARKDOWN, of(
    "md", "markdown", "mdown", "mdtxt", "mdtext", "mdwn", "mkd", "mkdown",
    "mkdn" ) ),
  MEDIA_TEXT_PLAIN( TEXT_PLAIN, of( "txt", "asc", "ascii", "text", "utxt" ) ),
  MEDIA_TEXT_R_MARKDOWN( TEXT_R_MARKDOWN, of( "Rmd" ) ),
  MEDIA_TEXT_XHTML( TEXT_XHTML, of( "xhtml" ) ),
  MEDIA_TEXT_XML( TEXT_XML ),
  MEDIA_TEXT_YAML( TEXT_YAML, of( "yaml", "yml" ) ),

  MEDIA_UNDEFINED( UNDEFINED, of( "undefined" ) );

  private final MediaType mMediaType;
  private final List<String> mExtensions;

  /**
   * Several media types have only one corresponding standard file name
   * extension; this constructor calls {@link MediaType#getSubtype()} to obtain
   * said extension. Some {@link MediaType}s have a single extension but their
   * assigned IANA name differs (e.g., {@code svg} maps to {@code svg+xml})
   * and thus must not use this constructor.
   *
   * @param mediaType The {@link MediaType} containing only one extension.
   */
  MediaTypeExtension( final MediaType mediaType ) {
    this( mediaType, of( mediaType.getSubtype() ) );
  }

  /**
   * Constructs an association of file name extensions to a single {@link
   * MediaType}.
   *
   * @param mediaType  The {@link MediaType} to associate with the given
   *                   file name extensions.
   * @param extensions The file name extensions used to lookup a corresponding
   *                   {@link MediaType}.
   */
  MediaTypeExtension(
    final MediaType mediaType, final List<String> extensions ) {
    assert mediaType != null;
    assert extensions != null;
    assert !extensions.isEmpty();

    mMediaType = mediaType;
    mExtensions = extensions;
  }

  /**
   * Returns the first file name extension in the list of file names given
   * at construction time.
   *
   * @return The one file name to rule them all.
   */
  public String getExtension() {
    return mExtensions.get( 0 );
  }

  /**
   * Returns the {@link MediaTypeExtension} that matches the given media type.
   *
   * @param mediaType The media type to find.
   * @return The correlated value or {@link #MEDIA_UNDEFINED} if not found.
   */
  public static MediaTypeExtension valueFrom( final MediaType mediaType ) {
    for( final var type : values() ) {
      if( type.isMediaType( mediaType ) ) {
        return type;
      }
    }

    return MEDIA_UNDEFINED;
  }

  boolean isMediaType( final MediaType mediaType ) {
    return mMediaType == mediaType;
  }

  /**
   * Returns the {@link MediaType} associated with the given file name
   * extension. The extension must not contain a period.
   *
   * @param extension File name extension, case insensitive, {@code null}-safe.
   * @return The associated {@link MediaType} as defined by IANA.
   */
  static MediaType getMediaType( final String extension ) {
    final var sanitized = sanitize( extension );

    for( final var mediaType : MediaTypeExtension.values() ) {
      if( mediaType.isType( sanitized ) ) {
        return mediaType.getMediaType();
      }
    }

    return UNDEFINED;
  }

  private boolean isType( final String sanitized ) {
    for( final var extension : mExtensions ) {
      if( extension.equalsIgnoreCase( sanitized ) ) {
        return true;
      }
    }

    return false;
  }

  private static String sanitize( final String extension ) {
    return extension == null ? "" : extension.toLowerCase();
  }

  private MediaType getMediaType() {
    return mMediaType;
  }
}
