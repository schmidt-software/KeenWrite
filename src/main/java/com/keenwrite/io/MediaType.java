package com.keenwrite.io;

/**
 * Defines various file formats and format contents.
 *
 * @see
 * <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA
 * Media Types</a>
 */
public enum MediaType {
  UNDEFINED( "" ),
  TEXT_MARKDOWN( "text/markdown" ),
  IMAGE_APNG( "image/apng" ),
  IMAGE_ACES( "image/aces" ),
  IMAGE_AVCI( "image/avci" ),
  IMAGE_AVCS( "image/avcs" ),
  IMAGE_BMP( "image/bmp" ),
  IMAGE_CGM( "image/cgm" ),
  IMAGE_DICOM_RLE( "image/dicom_rle" ),
  IMAGE_EMF( "image/emf" ),
  IMAGE_EXAMPLE( "image/example" ),
  IMAGE_FITS( "image/fits" ),
  IMAGE_G3FAX( "image/g3fax" ),
  IMAGE_GIF( "image/gif" ),
  IMAGE_HEIC( "image/heic" ),
  IMAGE_HEIF( "image/heif" ),
  IMAGE_HEJ2K( "image/hej2k" ),
  IMAGE_HSJ2( "image/hsj2" ),
  IMAGE_X_ICON( "image/x-icon" ),
  IMAGE_JLS( "image/jls" ),
  IMAGE_JP2( "image/jp2" ),
  IMAGE_JPEG( "image/jpeg" ),
  IMAGE_JPH( "image/jph" ),
  IMAGE_JPHC( "image/jphc" ),
  IMAGE_JPM( "image/jpm" ),
  IMAGE_JPX( "image/jpx" ),
  IMAGE_JXR( "image/jxr" ),
  IMAGE_JXRA( "image/jxrA" ),
  IMAGE_JXRS( "image/jxrS" ),
  IMAGE_JXS( "image/jxs" ),
  IMAGE_JXSC( "image/jxsc" ),
  IMAGE_JXSI( "image/jxsi" ),
  IMAGE_JXSS( "image/jxss" ),
  IMAGE_KTX( "image/ktx" ),
  IMAGE_KTX2( "image/ktx2" ),
  IMAGE_NAPLPS( "image/naplps" ),
  IMAGE_PNG( "image/png" ),
  IMAGE_SVG_XML( "image/svg+xml" ),
  IMAGE_T38( "image/t38" ),
  IMAGE_TIFF( "image/tiff" ),
  IMAGE_WEBP( "image/webp" ),
  IMAGE_WMF( "image/wmf" );

  /**
   * The IANA-defined type and sub-type.
   */
  private final String mMediaType;

  MediaType( final String mediaType ) {
    mMediaType = mediaType;
  }

  /**
   * Returns the IANA-defined type and sub-type.
   *
   * @return The unique media type identifier.
   */
  public String toString() {
    return mMediaType;
  }
}
