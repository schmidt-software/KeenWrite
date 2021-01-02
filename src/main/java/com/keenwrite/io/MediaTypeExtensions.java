/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.util.Set;

import static com.keenwrite.io.MediaType.*;
import static java.util.Set.of;

/**
 * Responsible for associating file extensions with {@link MediaType} instances.
 */
enum MediaTypeExtensions {
  MEDIA_FONT_OTF( FONT_OTF ),
  MEDIA_FONT_TTF( FONT_TTF ),

  MEDIA_IMAGE_APNG( IMAGE_APNG ),
  MEDIA_IMAGE_BMP( IMAGE_BMP ),
  MEDIA_IMAGE_GIF( IMAGE_GIF ),
  MEDIA_IMAGE_ICO( IMAGE_X_ICON, of( "ico", "cur" ) ),
  MEDIA_IMAGE_JPEG( IMAGE_JPEG, of( "jpg", "jpeg", "jfif", "pjpeg", "pjp" ) ),
  MEDIA_IMAGE_PNG( IMAGE_PNG ),
  MEDIA_IMAGE_SVG( IMAGE_SVG_XML, of( "svg" ) ),
  MEDIA_IMAGE_TIFF( IMAGE_TIFF, of( "tif", "tiff" ) ),
  MEDIA_IMAGE_WEBP( IMAGE_WEBP ),

  MEDIA_TEXT_MARKDOWN( TEXT_MARKDOWN, of(
    "md", "markdown", "mdown", "mdtxt", "mdtext", "mdwn", "mkd", "mkdown",
    "mkdn" ) ),
  MEDIA_TEXT_PLAIN( TEXT_PLAIN, of( "asc", "ascii", "txt", "text", "utxt" ) ),
  MEDIA_TEXT_R_MARKDOWN( TEXT_R_MARKDOWN, of( "Rmd" ) ),
  MEDIA_TEXT_R_XML( TEXT_R_XML, of( "Rxml" ) ),
  MEDIA_TEXT_YAML( TEXT_YAML, of( "yaml", "yml" ) );

  private final MediaType mMediaType;
  private final Set<String> mExtensions;

  /**
   * Several media types have only one corresponding standard file name
   * extension; this constructor calls {@link MediaType#getSubtype()} to obtain
   * said extension. Some {@link MediaType}s have a single extension but their
   * assigned IANA name differs (e.g., {@code svg} maps to {@code svg+xml})
   * and thus must not use this constructor.
   *
   * @param mediaType The {@link MediaType} containing only one extension.
   */
  MediaTypeExtensions( final MediaType mediaType ) {
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
  MediaTypeExtensions(
    final MediaType mediaType, final Set<String> extensions ) {
    assert mediaType != null;
    assert extensions != null;
    assert !extensions.isEmpty();

    mMediaType = mediaType;
    mExtensions = extensions;
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

    for( final var mediaType : MediaTypeExtensions.values() ) {
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
