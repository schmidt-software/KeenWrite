/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.io.MediaType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.util.ProtocolScheme;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaTypeExtension.valueFrom;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_DIR;
import static com.keenwrite.util.ProtocolScheme.isRemote;
import static java.io.File.createTempFile;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.nodes.Document.OutputSettings.Syntax;

/**
 * Responsible for making the body of an HTML document complete by wrapping
 * it with html and body elements. This doesn't have to be super-efficient
 * because it's not run in real-time.
 */
public final class XhtmlProcessor extends ExecutorProcessor<String> {
  private final Workspace mWorkspace;

  public XhtmlProcessor( final Workspace workspace ) {
    mWorkspace = workspace;
  }

  @Override
  public String apply( final String html ) {
    final var doc = parse( html );
    doc.outputSettings().syntax( Syntax.xml );

    for( final var img : doc.getElementsByTag( "img" ) ) {
      final var src = img.attr( "src" );

      try {
        final var protocol = ProtocolScheme.getProtocol( src );
        final File imageFile;

        if( protocol.isRemote() ) {
          final var url = new URL( src );
          final var conn = url.openConnection();
          conn.setUseCaches( false );

          final var type = conn.getContentType();
          final var media = MediaType.valueFrom( type );

          try( final var in = conn.getInputStream() ) {
            imageFile = createTemporaryFile( media );
            copy( in, imageFile.toPath(), REPLACE_EXISTING );
          }
        }
        else {
          imageFile = Path.of( getImagePath(), src ).toFile();
        }

        img.attr( "src", imageFile.getAbsolutePath() );
      } catch( final Exception ex ) {
        clue( ex );
      }
    }

    return doc.html();
  }

  private String getImagePath() {
    return mWorkspace.fileProperty( KEY_IMAGES_DIR ).get().toString();
  }

  private static File createTemporaryFile( final MediaType media )
    throws IOException {
    final var file = createTempFile(
      APP_TITLE_LOWERCASE, '.' + valueFrom( media ).getExtension() );
    file.deleteOnExit();
    return file;
  }
}
