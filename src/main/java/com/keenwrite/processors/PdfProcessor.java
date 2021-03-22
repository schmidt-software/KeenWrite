/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import java.io.File;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for using a typesetting engine to convert an XHTML document
 * into a PDF file.
 */
public final class PdfProcessor extends ExecutorProcessor<String> {
  private static final String TYPESETTER = "context";

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
    if( canExecute( TYPESETTER ) ) {
      System.out.println( "CONTEXT IS CONFIGURED" );
      System.out.println( "EXPORT AS: " + mExportPath );
    }

    return null;
  }

  @SuppressWarnings( "SameParameterValue" )
  private boolean canExecute( final String exe ) {
    final var paths = getenv( "PATH" ).split( quote( pathSeparator ) );
    return Stream.of( paths ).map( Paths::get ).anyMatch(
      path -> {
        final var p = path.resolve( exe );
        final var extensions = new String[]{"", ".com", ".exe", ".bat", ".cmd"};
        var found = false;

        for( final var extension : extensions ) {
          final var f = new File( p.toString() + extension );

          if( f.exists() && f.canExecute() ) {
            found = true;
            break;
          }
        }

        return found;
      }
    );
  }
}
