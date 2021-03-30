/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.io.MediaType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.util.ProtocolScheme;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.IMAGE_SVG_XML;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_DIR;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_ORDER;
import static com.keenwrite.util.FileUtils.createTemporaryFile;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.nodes.Document.OutputSettings.Syntax;

/**
 * Responsible for making the body of an HTML document complete by wrapping
 * it with html and body elements. This doesn't have to be super-efficient
 * because it's not run in real-time.
 */
public final class XhtmlProcessor extends ExecutorProcessor<String> {
  private final static Pattern BLANK =
    compile( "\\p{Blank}", UNICODE_CHARACTER_CLASS );

  private final ProcessorContext mContext;

  public XhtmlProcessor(
    final Processor<String> successor, final ProcessorContext context ) {
    super( successor );

    assert context != null;

    mContext = context;
  }

  @Override
  public String apply( final String html ) {
    final var doc = parse( html );
    doc.outputSettings().syntax( Syntax.xml );

    for( final var img : doc.getElementsByTag( "img" ) ) {
      try {
        final var imageFile = exportImage( img.attr( "src" ) );

        img.attr( "src", imageFile.toString() );
      } catch( final Exception ex ) {
        clue( ex );
      }
    }

    return doc.html();
  }

  /**
   * For a given src URI, this method will attempt to normalize it such that a
   * third-party application can find the file. Normalization could entail
   * downloading from the Internet or finding a suitable file name extension.
   *
   * @param src A path, local or remote, to a partial or complete file name.
   * @return A local file system path to the source path.
   * @throws Exception Could not read from, write to, or find a file.
   */
  private Path exportImage( final String src ) throws Exception {
    MediaType mediaType;
    Path imageFile = null;
    InputStream svgIn;

    final var protocol = ProtocolScheme.getProtocol( src );

    if( protocol.isRemote() ) {
      final var url = new URL( src );
      final var conn = url.openConnection();
      conn.setUseCaches( false );

      final var type = conn.getContentType();
      mediaType = MediaType.valueFrom( type );
      svgIn = conn.getInputStream();

      if( mediaType != IMAGE_SVG_XML ) {
        // Download into temporary directory.
        imageFile = createTemporaryFile( mediaType );
        copy( svgIn, imageFile, REPLACE_EXISTING );
        svgIn.close();
      }
    }
    else {
      final var extensions = " " + getImageOrder().trim();
      var imagePath = getImagePath();
      var found = false;

      // By including " " in the extensions, the first element returned
      // will be the empty string. Thus the first extension to try is the
      // file's default extension. Subsequent iterations will try to find
      // a file that has a name matching one of the preferred extensions.
      for( final var extension : BLANK.split( extensions ) ) {
        final var filename = format(
          "%s%s%s", src, extension.isBlank() ? "" : ".", extension );
        imageFile = Path.of( imagePath, filename );

        if( imageFile.toFile().exists() ) {
          found = true;
          break;
        }
      }

      if( !found ) {
        imagePath = getDocumentDir().toString();
        imageFile = Path.of( imagePath, src );

        if( !imageFile.toFile().exists() ) {
          throw new FileNotFoundException( imageFile.toString() );
        }
      }
    }

    return imageFile;
  }

  private String getImagePath() {
    return getWorkspace().fileProperty( KEY_IMAGES_DIR ).get().toString();
  }

  private String getImageOrder() {
    return getWorkspace().stringProperty( KEY_IMAGES_ORDER ).get();
  }

  /**
   * Returns the absolute path to the document being edited, which can be used
   * to find files included using relative paths.
   *
   * @return The directory containing the edited file.
   */
  private Path getDocumentDir() {
    return mContext.getBaseDir();
  }

  private Workspace getWorkspace() {
    return mContext.getWorkspace();
  }
}
