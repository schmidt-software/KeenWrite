/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.io.File;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Provides controls for processor behaviour when transforming input documents.
 */
public enum ExportFormat {

  /**
   * For HTML exports, encode TeX as SVG.
   */
  HTML_TEX_SVG( ".html" ),

  /**
   * For HTML exports, encode TeX using {@code $} delimiters, suitable for
   * rendering by an external TeX typesetting engine (or online with KaTeX).
   */
  HTML_TEX_DELIMITED( ".html" ),

  /**
   * Indicates that the processors should export to a Markdown format.
   */
  MARKDOWN_PLAIN( ".out.md" ),

  /**
   * Indicates no special export format is to be created. No extension is
   * applicable.
   */
  NONE( "" );

  /**
   * Preferred file name extension for the given file type.
   */
  private final String mExtension;

  ExportFormat( final String extension ) {
    mExtension = extension;
  }

  /**
   * Returns the given file renamed with the extension that matches this
   * {@link ExportFormat} extension.
   *
   * @param file The file to rename.
   * @return The renamed version of the given file.
   */
  public File toExportFilename( final File file ) {
    return new File( removeExtension( file.getName() ) + mExtension );
  }
}
