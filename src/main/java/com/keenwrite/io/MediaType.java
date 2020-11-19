/* Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.io;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static java.lang.String.format;

/**
 * Defines various file formats and format contents.
 *
 * @see
 * <a href="https://www.iana.org/assignments/media-types/media-types.xhtml">IANA
 * Media Types</a>
 */
public enum MediaType {
  APP_VENDOR_PROJECT(
      TypeName.APPLICATION, format( "vnd.%s.project", APP_TITLE_LOWERCASE )
  ),

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
  IMAGE_SVG_XML( "svg+xml" ),
  IMAGE_T38( "t38" ),
  IMAGE_TIFF( "tiff" ),
  IMAGE_WEBP( "webp" ),
  IMAGE_WMF( "wmf" ),

  TEXT_HTML( TypeName.TEXT, "html" ),
  TEXT_MARKDOWN( TypeName.TEXT, "markdown" ),
  TEXT_YAML( TypeName.TEXT, "yaml" ),

  UNDEFINED( TypeName.UNDEFINED, "undefined" );

  /**
   * The IANA-defined types.
   */
  public enum TypeName {
    APPLICATION,
    IMAGE,
    TEXT,
    UNDEFINED
  }

  /**
   * The fully qualified IANA-defined media type.
   */
  private final String mMediaType;

  /**
   * The IANA-defined type.
   */
  private final TypeName mTypeName;

  /**
   * Constructs an instance using the default type name of "image".
   *
   * @param subtype The image subtype.
   */
  MediaType( final String subtype ) {
    this( TypeName.IMAGE, subtype );
  }

  MediaType( final TypeName typeName, final String subtype ) {
    mTypeName = typeName;
    mMediaType = typeName.toString().toLowerCase() + '/' + subtype;
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
   * Returns the IANA-defined type and sub-type.
   *
   * @return The unique media type identifier.
   */
  public String toString() {
    return mMediaType;
  }
}
