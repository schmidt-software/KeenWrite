/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.MediaType;
import com.keenwrite.io.MediaTypeExtension;

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

  /**
   * Looks up the {@link ExportFormat} based on the given path and subtype.
   *
   * @param path     The type to find.
   * @param modifier The subtype to find (for HTML).
   * @return An object to control the output file format.
   * @throws IllegalArgumentException The type/subtype could not be found.
   */
  public static ExportFormat valueFrom( final Path path, final String modifier )
    throws IllegalArgumentException {
    assert path != null;

    return valueFrom( MediaType.fromFilename( path ), modifier );
  }

  /**
   * Looks up the {@link ExportFormat} based on the given path and subtype.
   *
   * @param extension The type to find.
   * @param modifier  The subtype to find (for HTML).
   * @return An object to control the output file format.
   * @throws IllegalArgumentException The type/subtype could not be found.
   */
  public static ExportFormat valueFrom(
    final String extension, final String modifier )
    throws IllegalArgumentException {
    assert extension != null;

    return valueFrom( MediaTypeExtension.fromExtension( extension ), modifier );
  }

  /**
   * Looks up the {@link ExportFormat} based on the given path and subtype.
   *
   * @param type     The media type to find.
   * @param modifier The subtype to find (for HTML).
   * @return An object to control the output file format.
   * @throws IllegalArgumentException The type/subtype could not be found.
   */
  public static ExportFormat valueFrom(
    final MediaType type, final String modifier ) {
    return switch( type ) {
      case TEXT_HTML, TEXT_XHTML -> "svg".equalsIgnoreCase( modifier.trim() )
        ? HTML_TEX_SVG
        : HTML_TEX_DELIMITED;
      case APP_PDF -> APPLICATION_PDF;
      case TEXT_XML -> XHTML_TEX;
      default -> throw new IllegalArgumentException( format(
        "Unrecognized format type and subtype: '%s' and '%s'", type, modifier
      ) );
    };
  }

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
