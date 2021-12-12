/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import java.io.File;
import java.nio.file.Path;

import static java.lang.String.format;
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
   * Looks up the {@link ExportFormat} based on the given format type and
   * subtype combination.
   *
   * @param type    The type to find.
   * @param subtype The subtype to find (for HTML).
   * @return An object that defines the export format according to the given
   * parameters.
   * @throws IllegalArgumentException Could not determine the type and
   *                                  subtype combination.
   */
  public static ExportFormat valueFrom(
    final String type,
    final String subtype ) throws IllegalArgumentException {
    assert type != null;
    assert subtype != null;

    return switch( type.trim().toLowerCase() ) {
      case "html" -> "svg".equalsIgnoreCase( subtype.trim() )
        ? HTML_TEX_SVG
        : HTML_TEX_DELIMITED;
      case "md" -> MARKDOWN_PLAIN;
      case "pdf" -> APPLICATION_PDF;
      default -> throw new IllegalArgumentException( format(
        "Unrecognized format type and subtype: '%s' and '%s'", type, subtype
      ) );
    };
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

  public Path toExportPath( final Path path ) {
    return toExportFilename( path ).toPath();
  }
}
