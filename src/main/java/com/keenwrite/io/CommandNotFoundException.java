/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.FileNotFoundException;

/**
 * Indicates a command could not be found to run.
 */
public class CommandNotFoundException extends FileNotFoundException {
  public CommandNotFoundException( final String command ) {
    super( command );
  }
}
