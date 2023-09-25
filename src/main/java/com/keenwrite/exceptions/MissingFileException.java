/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.exceptions;

import java.io.FileNotFoundException;

import static com.keenwrite.Messages.get;

/**
 * Responsible for informing the user when a file cannot be found.
 * This avoids duplicating the error message prefix.
 */
public final class MissingFileException extends FileNotFoundException {
  /**
   * Constructs a new {@link MissingFileException} using the given path.
   *
   * @param uri The path to the file resource that could not be found.
   */
  public MissingFileException( final String uri ) {
    super( get( "Main.status.error.file.missing", uri ) );
  }
}
