package com.keenwrite.io;

import java.util.Set;

import static com.keenwrite.io.MediaType.*;
import static java.util.Set.of;

/**
 * Responsible for associating file extensions with {@link MediaType} instances.
 */
enum MediaTypeExtensions {
  MEDIA_IMAGE_APNG( IMAGE_APNG, of( "apng" ) ),
  MEDIA_IMAGE_BMP( IMAGE_BMP, of( "bmp" ) ),
  MEDIA_IMAGE_GIF( IMAGE_GIF, of( "gif" ) ),
  MEDIA_IMAGE_ICO( IMAGE_X_ICON, of( "ico", ".cur" ) ),
  MEDIA_IMAGE_JPEG( IMAGE_JPEG, of( "jpg", "jpeg", "jfif", "pjpeg", "pjp" ) ),
  MEDIA_IMAGE_PNG( IMAGE_PNG, of( "png" ) ),
  MEDIA_IMAGE_SVG( IMAGE_SVG_XML, of( "svg" ) ),
  MEDIA_IMAGE_TIFF( IMAGE_TIFF, of( "tif", "tiff" ) ),
  MEDIA_IMAGE_WEBP( IMAGE_WEBP, of( "webp" ) ),

  MEDIA_TEXT_MARKDOWN( TEXT_MARKDOWN, of(
      "md", "markdown", "mdown", "mdtxt", "mdtext", "mdwn", "mkd", "mkdown",
      "mkdn", "text", "txt" ) ),
  MEDIA_TEXT_YAML( TEXT_YAML, of( "yaml", "yml" ) );

  private final MediaType mMediaType;
  private final Set<String> mExtensions;

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

    for( final var mediaType : values() ) {
      if( mediaType.isType( sanitized ) ) {
        return mediaType.getMediaType();
      }
    }

    return UNDEFINED;
  }

  private boolean isType( final String extension ) {
    return mExtensions.contains( sanitize( extension ) );
  }

  private static String sanitize( final String extension ) {
    return extension == null ? "" : extension.toLowerCase();
  }

  private MediaType getMediaType() {
    return mMediaType;
  }
}
