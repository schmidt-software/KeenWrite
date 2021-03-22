/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.io.File;
import java.nio.file.Path;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Provides controls for processor behaviour when transforming input documents.
 */
public enum ExportFormat {

  /**
   * For HTML exports, encode TeX as SVG. Treat image links relatively.
   */
  HTML_TEX_SVG( ".html" ),

  /**
   * For HTML exports, encode TeX using {@code $} delimiters, suitable for
   * rendering by an external TeX typesetting engine (or online with KaTeX).
   * Treat image links relatively.
   */
  HTML_TEX_DELIMITED( ".html" ),

  /**
   * For XHTML exports, encode TeX using {@code $} delimiters.
   */
  XHTML_TEX( ".xml" ),

  /**
   * Indicates that the processors should export to a Markdown format.
   * Treat image links relatively.
   */
  MARKDOWN_PLAIN( ".out.md" ),

  /**
   * Exports as PDF file format.
   */
  APPLICATION_PDF( ".pdf" ),

  /**
   * Indicates no special export format is to be created. No extension is
   * applicable. Image links must use absolute directories.
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
   * Returns the given {@link File} with its extension replaced by one that
   * matches this {@link ExportFormat} extension.
   *
   * @param file The file to perform an extension swap.
   * @return The given file with its extension replaced.
   */
  public File toExportFilename( final File file ) {
    return new File( removeExtension( file.getName() ) + mExtension );
  }

  /**
   * Delegates to {@link #toExportFilename(File)} after converting the given
   * {@link Path} to an instance of {@link File}.
   *
   * @param path The {@link Path} to convert to a {@link File}.
   * @return The given path with its extension replaced.
   */
  public File toExportFilename( final Path path ) {
    return toExportFilename( path.toFile() );
  }
}
