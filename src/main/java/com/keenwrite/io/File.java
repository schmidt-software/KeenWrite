/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
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
   * Constructs a new instance based on an existing {@link java.io.File}
   * instance.
   *
   * @param file The absolute path from this object is used for construction.
   */
  public File( final java.io.File file ) {
    this( file.getAbsolutePath() );
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
