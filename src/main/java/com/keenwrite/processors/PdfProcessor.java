/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.typesetting.Typesetter;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.TEXT_XML;
import static java.nio.file.Files.writeString;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Responsible for using a typesetting engine to convert an XHTML document
 * into a PDF file.
 */
public final class PdfProcessor extends ExecutorProcessor<String> {
  private static final ExecutorService sExecutor = newFixedThreadPool( 5 );

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
    final var exporter = new Exporter( xhtml );
    exporter.setOnRunning( e -> clue( get( "Main.status.typeset.create" ) ) );
    exporter.setOnSucceeded( e -> {
      clue( get( "Main.status.typeset.export" ) );

      final var pathOutput = mContext.getExportPath();
      final var pathInput = exporter.getValue();
      final var typesetter = new Typesetter( mContext.getWorkspace() );

      try {
        typesetter.typeset( pathInput, pathOutput );
      } catch( final Exception ex ) {
        clue( ex );
      }
    } );

    sExecutor.execute( exporter );

    // Do not continue processing (the document was typeset into a binary).
    return null;
  }

  /**
   * Responsible for exporting the active document to a file. That file is
   * then read and typeset by a third-party application.
   */
  private static class Exporter extends Task<Path> {
    private final String mXhtml;

    private Exporter( final String xhtml ) {
      mXhtml = xhtml;
    }

    @Override
    protected Path call() throws Exception {
      final var document = TEXT_XML.createTemporaryFile( APP_TITLE_LOWERCASE );
      return writeString( document, mXhtml );
    }
  }
}
