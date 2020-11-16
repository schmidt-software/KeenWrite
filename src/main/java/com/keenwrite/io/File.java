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

import org.apache.commons.io.FilenameUtils;

/**
 * Extends {@link java.io.File} with {@link MediaType} detection.
 */
public class File extends java.io.File {

  /**
   * Constructs a new instance that refers to a file at the given path.
   *
   * @param pathname The pathname to a file resource, which may not exist.
   */
  public File( final String pathname ) {
    super( pathname );
  }

  /**
   * Returns the {@link MediaType} associated with this file.
   *
   * @return {@link MediaType#UNDEFINED} if the extension has not been
   * assigned, otherwise the {@link MediaType} associated with this
   * {@link File}'s file name extension.
   */
  public MediaType getMediaType() {
    return MediaTypeExtensions.getMediaType( getExtension() );
  }

  /**
   * Answers whether the given {@link MediaType} is the same as this
   * {@link MediaType} for this {@link File} instance.
   *
   * @param mediaType The other {@link MediaType} to compare.
   * @return {@code true} when the given {@link MediaType} matches the
   * type derived for this {@link File} instance.
   */
  public boolean isMediaType( final MediaType mediaType ) {
    return getMediaType() == mediaType;
  }

  /**
   * Convenience method to avoid instantiating a new instance of this class.
   * This uses the file name extension, but could be enhanced to look at the
   * file contents.
   *
   * @param filename Path to the file to ascertain its {@link MediaType}.
   * @return The {@link MediaType} for the given file.
   */
  public static MediaType getMediaType( final String filename ) {
    return MediaTypeExtensions.getMediaType( getExtension( filename ) );
  }

  private String getExtension() {
    return getExtension( getName() );
  }

  private static String getExtension( final String filename ) {
    return FilenameUtils.getExtension( filename );
  }
}
