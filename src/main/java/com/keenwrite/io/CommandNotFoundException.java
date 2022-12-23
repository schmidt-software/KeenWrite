/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Indicates a command could not be found to run.
 */
public class CommandNotFoundException extends FileNotFoundException {
  /**
   * Creates a new exception indicating that the given command could not be
   * found (or executed).
   *
   * @param command The binary file's command name that could not be run.
   */
  public CommandNotFoundException( final String command ) {
    super( command );
  }

  /**
   * Creates a new exception indicating that the given command could not be
   * found (or executed).
   *
   * @param file The binary file's command name that could not be run.
   */
  public CommandNotFoundException( final File file ) {
    this( file.getAbsolutePath() );
  }
}
