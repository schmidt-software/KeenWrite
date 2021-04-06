/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.io.HttpFacade;
import com.keenwrite.preferences.Workspace;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_DIR;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_ORDER;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
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
    Path imageFile = null;

    final var protocol = getProtocol( src );

    // Download remote resources into temporary files.
    if( protocol.isRemote() ) {
      final var response = HttpFacade.httpGet( src);
      final var mediaType = response.getMediaType();

      imageFile = mediaType.createTemporaryFile( APP_TITLE_LOWERCASE );

      try( final var image = response.getInputStream() ) {
        copy( image, imageFile, REPLACE_EXISTING );
      }

      // Strip comments, superfluous whitespace, DOCTYPE, and XML declarations.
      if( mediaType.isSvg() ) {
        sanitize( imageFile );
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

  /**
   * Remove whitespace, comments, and XML/DOCTYPE declarations to make
   * processing work with ConTeXt.
   *
   * @param path The SVG file to process.
   * @throws Exception The file could not be processed.
   */
  private void sanitize( final Path path )
    throws Exception {
    final var file = path.toFile();

    final var dbf = DocumentBuilderFactory.newInstance();
    dbf.setIgnoringComments( true );
    dbf.setIgnoringElementContentWhitespace( true );

    final var db = dbf.newDocumentBuilder();
    final var document = db.parse( file );

    final var tf = TransformerFactory.newInstance();
    final var transformer = tf.newTransformer();

    final var source = new DOMSource( document );
    final var result = new StreamResult( file );
    transformer.setOutputProperty( OMIT_XML_DECLARATION, "yes" );
    transformer.setOutputProperty( INDENT, "no" );
    transformer.transform( source, result );
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
