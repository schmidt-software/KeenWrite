/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.io.File;
import java.util.EventObject;

/**
 * Responsible for indicating that a file has been modified by the file system.
 */
public class FileEvent extends EventObject {

  /**
   * Constructs a new event that indicates the source of a file system event.
   *
   * @param file The {@link File} that has succumb to a file system event.
   */
  public FileEvent( final File file ) {
    super( file );
  }

  /**
   * Returns the source as an instance of {@link File}.
   *
   * @return The {@link File} being watched.
   */
  public File getFile() {
    return (File) getSource();
  }
}
