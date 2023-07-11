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
      final var targetPath = context.getTargetPath();
      clue( "Main.status.typeset.setting", "target", targetPath );

      final var parent = targetPath.toAbsolutePath().getParent();

      final var document = TEXT_XML.createTempFile( APP_TITLE_ABBR, parent );
      final var sourcePath = writeString( document, xhtml );
      clue( "Main.status.typeset.setting", "source", sourcePath );

      final var themeDir = context.getThemeDir();
      clue( "Main.status.typeset.setting", "themes", themeDir );

      final var imageDir = context.getImageDir();
      clue( "Main.status.typeset.setting", "images", imageDir );

      final var cacheDir = context.getCacheDir();
      clue( "Main.status.typeset.setting", "caches", cacheDir );

      final var fontDir = context.getFontDir();
      clue( "Main.status.typeset.setting", "fonts", fontDir );

      final var autoRemove = context.getAutoRemove();
      clue( "Main.status.typeset.setting", "purge", autoRemove );

      final var rWorkDir = context.getRWorkingDir();
      clue( "Main.status.typeset.setting", "r-work", rWorkDir );

      final var imageOrder = context.getImageOrder();
      clue( "Main.status.typeset.setting", "order", imageOrder );

      final var typesetter = Typesetter
        .builder()
        .with( Mutator::setSourcePath, sourcePath )
        .with( Mutator::setTargetPath, targetPath )
        .with( Mutator::setThemeDir, themeDir )
        .with( Mutator::setImageDir, imageDir )
        .with( Mutator::setCacheDir, cacheDir )
        .with( Mutator::setFontDir, fontDir )
        .with( Mutator::setAutoRemove, autoRemove )
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
