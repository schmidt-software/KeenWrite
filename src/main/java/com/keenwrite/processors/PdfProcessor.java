/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.typesetting.Typesetter;

import java.io.File;
import java.io.IOException;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.APP_PDF;
import static com.keenwrite.util.FileUtils.createTemporaryFile;

/**
 * Responsible for using a typesetting engine to convert an XHTML document
 * into a PDF file.
 */
public final class PdfProcessor extends ExecutorProcessor<String> {
  private static final Typesetter sTypesetter = new Typesetter();

  private final File mExportPath;

  public PdfProcessor( final File exportPath ) {
    assert exportPath != null;
    mExportPath = exportPath;
  }

  /**
   * Converts a document by calling a third-party library to typeset the given
   * XHTML document.
   *
   * @param xhtml The document to convert to a PDF file.
   * @return {@code null} because there is no valid return value from generating
   * a PDF file.
   */
  public String apply( final String xhtml ) {
    try {
      final var document = createTemporaryFile( APP_PDF );
      sTypesetter.typeset( document, mExportPath );
    } catch( final IOException ex ) {
      clue( ex );
    }

    return null;
  }
}
