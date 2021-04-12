/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.typesetting.Typesetter;

import java.io.IOException;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.TEXT_XML;
import static java.nio.file.Files.writeString;

/**
 * Responsible for using a typesetting engine to convert an XHTML document
 * into a PDF file. This must not be run from the JavaFX thread.
 */
public final class PdfProcessor extends ExecutorProcessor<String> {
  private final ProcessorContext mContext;

  public PdfProcessor( final ProcessorContext context ) {
    assert context != null;
    mContext = context;
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
      clue( get( "Main.status.typeset.create" ) );
      final var document = TEXT_XML.createTemporaryFile( APP_TITLE_LOWERCASE );
      final var pathInput = writeString( document, xhtml );
      final var pathOutput = mContext.getExportPath();
      final var typesetter = new Typesetter( mContext.getWorkspace() );

      typesetter.typeset( pathInput, pathOutput );
    } catch( final IOException | InterruptedException ex ) {
      // Typesetter runtime exceptions will pass up the call stack.
      clue( get( "Main.status.typeset.failed" ), ex );
    }

    // Do not continue processing (the document was typeset into a binary).
    return null;
  }
}
