/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.exceptions;

import java.io.FileNotFoundException;

import static com.keenwrite.Messages.get;

/**
 * Responsible for informing the user when a file cannot be found.
 * This avoids duplicating the error message prefix.
 */
public class MissingFileException extends FileNotFoundException {
  /**
   * Constructs a new {@link MissingFileException} using the given path.
   *
   * @param uri The path to the file resource that could not be found.
   */
  public MissingFileException( final String uri ) {
    super( get( "Main.status.error.file.missing", uri ) );
  }
}
