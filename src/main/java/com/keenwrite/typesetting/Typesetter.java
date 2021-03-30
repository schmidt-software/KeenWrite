/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import java.io.File;
import java.nio.file.Path;

import static com.keenwrite.util.FileUtils.canExecute;

/**
 * Represents the executable responsible for typesetting text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 */
public class Typesetter {
  private static final String TYPESETTER = "context";

  public Typesetter() {
  }

  public boolean isInstalled() {
    return canExecute( TYPESETTER );
  }

  /**
   * This will typeset the document using a new process.
   *
   * @param input  The input document to typeset.
   * @param output Path to the finished typeset document.
   */
  public void typeset( final Path input, final File output ) {

  }
}
