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
  UNDEFINED( "" ),
  TEXT_MARKDOWN( "text/markdown" ),
  TEXT_YAML( "text/yaml" ),
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
  IMAGE_WMF( "image/wmf" ),
  APP_VENDOR_PROJECT(
      format( "application/vnd.%s.project", APP_TITLE_LOWERCASE ) );

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
