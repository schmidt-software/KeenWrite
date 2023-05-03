/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.typesetting.Typesetter;

import static com.keenwrite.Bootstrap.APP_TITLE_ABBR;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.TEXT_XML;
import static com.keenwrite.typesetting.Typesetter.Mutator;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.writeString;

/**
 * Responsible for using a typesetting engine to convert an XHTML document
 * into a PDF file. This must not be run from the JavaFX thread.
 */
public final class PdfProcessor extends ExecutorProcessor<String> {
  private final ProcessorContext mProcessorContext;

  public PdfProcessor( final ProcessorContext context ) {
    assert context != null;
    mProcessorContext = context;
  }

  /**
   * Converts a document by calling a third-party application to typeset the
   * given XHTML document.
   *
   * @param xhtml The document to convert to a PDF file.
   * @return {@code null} because there is no valid return value from generating
   * a PDF file.
   */
  public String apply( final String xhtml ) {
    try {
      clue( "Main.status.typeset.create" );
      final var context = mProcessorContext;
      final var parent = context.getTargetPath().getParent();
      final var document =
        TEXT_XML.createTempFile( APP_TITLE_ABBR, parent );
      final var typesetter = Typesetter
        .builder()
        .with( Mutator::setAutoRemove, context.getAutoRemove() )
        .with( Mutator::setSourcePath, writeString( document, xhtml ) )
        .with( Mutator::setTargetPath, context.getTargetPath() )
        .with( Mutator::setThemesPath, context.getThemesDir() )
        .with( Mutator::setImagesPath, context.getImagesDir() )
        .with( Mutator::setCachesPath, context.getCachesPath() )
        .with( Mutator::setFontsPath, context.getFontsDir() )
        .build();

      typesetter.typeset();

      // Smote the temporary file after typesetting the document.
      if( typesetter.autoRemove() ) {
        deleteIfExists( document );
      }
    } catch( final Exception ex ) {
      // Typesetter runtime exceptions will pass up the call stack.
      clue( "Main.status.typeset.failed", ex );
    }

    // Do not continue processing (the document was typeset into a binary).
    return null;
  }
}
