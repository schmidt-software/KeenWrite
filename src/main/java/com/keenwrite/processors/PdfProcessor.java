/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.typesetting.Typesetter;

import static com.keenwrite.Bootstrap.APP_TITLE_ABBR;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.TEXT_XML;
import static com.keenwrite.io.SysFile.normalize;
import static com.keenwrite.typesetting.Typesetter.Mutator;
import static com.keenwrite.util.Strings.sanitize;
import static java.nio.charset.StandardCharsets.UTF_8;
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

      final var parent = normalize( targetPath.toAbsolutePath().getParent() );

      final var document = TEXT_XML.createTempFile( APP_TITLE_ABBR, parent );
      final var sourcePath = writeString( document, xhtml, UTF_8 );
      clue( "Main.status.typeset.setting", "source", sourcePath );

      final var themeDir = normalize( context.getThemeDir() );
      clue( "Main.status.typeset.setting", "themes", themeDir );

      final var imageDir = normalize( context.getImageDir() );
      clue( "Main.status.typeset.setting", "images", imageDir );

      final var imageOrder = context.getImageOrder();
      clue( "Main.status.typeset.setting", "order", imageOrder );

      final var cacheDir = normalize( context.getCacheDir() );
      clue( "Main.status.typeset.setting", "caches", cacheDir );

      final var fontDir = normalize( context.getFontDir() );
      clue( "Main.status.typeset.setting", "fonts", fontDir );

      final var rWorkDir = normalize( context.getRWorkingDir() );
      clue( "Main.status.typeset.setting", "r-work", rWorkDir );

      final var enableMode = sanitize( context.getEnableMode() );
      clue( "Main.status.typeset.setting", "mode", enableMode );

      final var autoRemove = context.getAutoRemove();
      clue( "Main.status.typeset.setting", "purge", autoRemove );

      final var typesetter = Typesetter
        .builder()
        .with( Mutator::setTargetPath, targetPath )
        .with( Mutator::setSourcePath, sourcePath )
        .with( Mutator::setThemeDir, themeDir )
        .with( Mutator::setImageDir, imageDir )
        .with( Mutator::setCacheDir, cacheDir )
        .with( Mutator::setFontDir, fontDir )
        .with( Mutator::setEnableMode, enableMode )
        .with( Mutator::setAutoRemove, autoRemove )
        .build();

      try {
        typesetter.typeset();
      }
      finally {
        // Smote the temporary file after typesetting the document.
        if( typesetter.autoRemove() ) {
          deleteIfExists( document );
        }
      }
    } catch( final Exception ex ) {
      // Typesetter runtime exceptions will pass up the call stack.
      clue( "Main.status.typeset.failed", ex );
    }

    // Do not continue processing (the document was typeset into a binary).
    return null;
  }
}
