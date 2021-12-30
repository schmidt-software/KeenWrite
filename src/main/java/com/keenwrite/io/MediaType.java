/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.keenwrite.io.MediaType.TypeName.*;
import static com.keenwrite.io.MediaTypeExtension.getMediaType;
import static java.io.File.createTempFile;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Defines various file formats and format contents.
 *
 * @see
 * <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA
 * Media Types</a>
 */
public enum MediaType {
  APP_DOCUMENT_OUTLINE( APPLICATION, "x-document-outline" ),
  APP_DOCUMENT_STATISTICS( APPLICATION, "x-document-statistics" ),
  APP_FILE_MANAGER( APPLICATION, "x-file-manager" ),

  APP_ACAD( APPLICATION, "acad" ),
  APP_JAVA_OBJECT( APPLICATION, "x-java-serialized-object" ),
  APP_JAVA( APPLICATION, "java" ),
  APP_PS( APPLICATION, "postscript" ),
  APP_EPS( APPLICATION, "eps" ),
  APP_PDF( APPLICATION, "pdf" ),
  APP_ZIP( APPLICATION, "zip" ),

  /*
   * Standard font types.
   */
  FONT_OTF( "otf" ),
  FONT_TTF( "ttf" ),

  /*
   * Standard image types.
   */
  IMAGE_APNG( "apng" ),
  IMAGE_ACES( "aces" ),
  IMAGE_AVCI( "avci" ),
  IMAGE_AVCS( "avcs" ),
  IMAGE_BMP( "bmp" ),
  IMAGE_CGM( "cgm" ),
  IMAGE_DICOM_RLE( "dicom_rle" ),
  IMAGE_EMF( "emf" ),
  IMAGE_EXAMPLE( "example" ),
  IMAGE_FITS( "fits" ),
  IMAGE_G3FAX( "g3fax" ),
  IMAGE_GIF( "gif" ),
  IMAGE_HEIC( "heic" ),
  IMAGE_HEIF( "heif" ),
  IMAGE_HEJ2K( "hej2k" ),
  IMAGE_HSJ2( "hsj2" ),
  IMAGE_X_ICON( "x-icon" ),
  IMAGE_JLS( "jls" ),
  IMAGE_JP2( "jp2" ),
  IMAGE_JPEG( "jpeg" ),
  IMAGE_JPH( "jph" ),
  IMAGE_JPHC( "jphc" ),
  IMAGE_JPM( "jpm" ),
  IMAGE_JPX( "jpx" ),
  IMAGE_JXR( "jxr" ),
  IMAGE_JXRA( "jxrA" ),
  IMAGE_JXRS( "jxrS" ),
  IMAGE_JXS( "jxs" ),
  IMAGE_JXSC( "jxsc" ),
  IMAGE_JXSI( "jxsi" ),
  IMAGE_JXSS( "jxss" ),
  IMAGE_KTX( "ktx" ),
  IMAGE_KTX2( "ktx2" ),
  IMAGE_NAPLPS( "naplps" ),
  IMAGE_PNG( "png" ),
  IMAGE_PHOTOSHOP( "photoshop" ),
  IMAGE_SVG_XML( "svg+xml" ),
  IMAGE_T38( "t38" ),
  IMAGE_TIFF( "tiff" ),
  IMAGE_WEBP( "webp" ),
  IMAGE_WMF( "wmf" ),
  IMAGE_X_BITMAP( "x-xbitmap" ),
  IMAGE_X_PIXMAP( "x-xpixmap" ),

  /*
   * Standard audio types.
   */
  AUDIO_BASIC( AUDIO, "basic" ),
  AUDIO_MP3( AUDIO, "mp3" ),
  AUDIO_WAV( AUDIO, "x-wav" ),

  /*
   * Standard video types.
   */
  VIDEO_MNG( VIDEO, "x-mng" ),

  /*
   * Document types for editing or displaying documents, mix of standard and
   * application-specific. The order that these are declared reflect in the
   * ordinal value used during comparisons.
   */
  TEXT_YAML( TEXT, "yaml" ),
  TEXT_PLAIN( TEXT, "plain" ),
  TEXT_MARKDOWN( TEXT, "markdown" ),
  TEXT_R_MARKDOWN( TEXT, "R+markdown" ),
  TEXT_HTML( TEXT, "html" ),
  TEXT_XHTML( TEXT, "xhtml+xml" ),
  TEXT_XML( TEXT, "xml" ),

  /*
   * When all other lights go out.
   */
  UNDEFINED( TypeName.UNDEFINED, "undefined" );

  /**
   * The IANA-defined types.
   */
  public enum TypeName {
    APPLICATION,
    AUDIO,
    IMAGE,
    TEXT,
    UNDEFINED,
    VIDEO
  }

  /**
   * The fully qualified IANA-defined media type.
   */
  private final String mMediaType;

  /**
   * The IANA-defined type name.
   */
  private final TypeName mTypeName;

  /**
   * The IANA-defined subtype name.
   */
  private final String mSubtype;

  /**
   * Constructs an instance using the default type name of "image".
   *
   * @param subtype The image subtype name.
   */
  MediaType( final String subtype ) {
    this( IMAGE, subtype );
  }

  /**
   * Constructs an instance using an IANA-defined type and subtype pair.
   *
   * @param typeName The media type's type name.
   * @param subtype  The media type's subtype name.
   */
  MediaType( final TypeName typeName, final String subtype ) {
    mTypeName = typeName;
    mSubtype = subtype;
    mMediaType = typeName.toString().toLowerCase() + '/' + subtype;
  }

  /**
   * Returns the {@link MediaType} associated with the given file.
   *
   * @param file Has a file name that may contain an extension associated with
   *             a known {@link MediaType}.
   * @return {@link MediaType#UNDEFINED} if the extension has not been
   * assigned, otherwise the {@link MediaType} associated with this
   * {@link File}'s file name extension.
   */
  public static MediaType valueFrom( final File file ) {
    assert file != null;
    return fromFilename( file.getName() );
  }

  /**
   * Returns the {@link MediaType} associated with the given file name.
   *
   * @param filename The file name that may contain an extension associated
   *                 with a known {@link MediaType}.
   * @return {@link MediaType#UNDEFINED} if the extension has not been
   * assigned, otherwise the {@link MediaType} associated with this
   * URL's file name extension.
   */
  public static MediaType fromFilename( final String filename ) {
    assert filename != null;
    return getMediaType( getExtension( filename ) );
  }

  /**
   * Returns the {@link MediaType} associated with the path to a file.
   *
   * @param path Has a file name that may contain an extension associated with
   *             a known {@link MediaType}.
   * @return {@link MediaType#UNDEFINED} if the extension has not been
   * assigned, otherwise the {@link MediaType} associated with this
   * {@link File}'s file name extension.
   */
  public static MediaType valueFrom( final Path path ) {
    assert path != null;
    return valueFrom( path.toFile() );
  }

  /**
   * Determines the media type an IANA-defined, semi-colon-separated string.
   * This is often used after making an HTTP request to extract the type
   * and subtype from the content-type.
   *
   * @param header The content-type header value, may be {@code null}.
   * @return The data type for the resource or {@link MediaType#UNDEFINED} if
   * unmapped.
   */
  public static MediaType valueFrom( String header ) {
    if( header == null || header.isBlank() ) {
      return UNDEFINED;
    }

    // Trim off the character encoding.
    var i = header.indexOf( ';' );
    header = header.substring( 0, i == -1 ? header.length() : i );

    // Split the type and subtype.
    i = header.indexOf( '/' );
    i = i == -1 ? header.length() : i;
    final var type = header.substring( 0, i );
    final var subtype = header.substring( i + 1 );

    return valueFrom( type, subtype );
  }

  /**
   * Returns the {@link MediaType} for the given type and subtype names.
   *
   * @param type    The IANA-defined type name.
   * @param subtype The IANA-defined subtype name.
   * @return {@link MediaType#UNDEFINED} if there is no {@link MediaType} that
   * matches the given type and subtype names.
   */
  public static MediaType valueFrom(
    final String type, final String subtype ) {
    assert type != null;
    assert subtype != null;

    for( final var mediaType : values() ) {
      if( mediaType.equals( type, subtype ) ) {
        return mediaType;
      }
    }

    return UNDEFINED;
  }

  /**
   * Answers whether the given type and subtype names equal this enumerated
   * value. This performs a case-insensitive comparison.
   *
   * @param type    The type name to compare against this {@link MediaType}.
   * @param subtype The subtype name to compare against this {@link MediaType}.
   * @return {@code true} when the type and subtype name match.
   */
  public boolean equals( final String type, final String subtype ) {
    assert type != null;
    assert subtype != null;

    return mTypeName.name().equalsIgnoreCase( type ) &&
      mSubtype.equalsIgnoreCase( subtype );
  }

  /**
   * Answers whether the given {@link TypeName} matches this type name.
   *
   * @param typeName The {@link TypeName} to compare against the internal value.
   * @return {@code true} if the given value is the same IANA-defined type name.
   */
  public boolean isType( final TypeName typeName ) {
    return mTypeName == typeName;
  }

  /**
   * Answers whether this instance is a scalable vector graphic.
   *
   * @return {@code true} if this instance represents an SVG object.
   */
  public boolean isSvg() {
    return this == IMAGE_SVG_XML;
  }

  public boolean isUndefined() {
    return this == UNDEFINED;
  }

  /**
   * Returns the IANA-defined subtype classification. Primarily used by
   * {@link MediaTypeExtension} to initialize associations where the subtype
   * name and the file name extension have a 1:1 mapping.
   *
   * @return The IANA subtype value.
   */
  public String getSubtype() {
    return mSubtype;
  }

  /**
   * Creates a temporary {@link File} that starts with the given prefix. The
   * file will be deleted when the application exits.
   *
   * @param prefix The file name begins with this string (may be empty).
   * @return The fully qualified path to the temporary file.
   * @throws IOException Could not create the temporary file.
   */
  public Path createTemporaryFile( final String prefix ) throws IOException {
    assert prefix != null;

    final var file = createTempFile(
      prefix, '.' + MediaTypeExtension.valueFrom( this ).getExtension() );
    file.deleteOnExit();
    return file.toPath();
  }

  /**
   * Returns the IANA-defined type and sub-type.
   *
   * @return The unique media type identifier.
   */
  @Override
  public String toString() {
    return mMediaType;
  }
}
