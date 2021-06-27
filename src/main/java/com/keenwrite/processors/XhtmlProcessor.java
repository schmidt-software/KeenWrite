/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.dom.DocumentParser;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.ui.heuristics.WordCounter;
import javafx.beans.property.StringProperty;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.dom.DocumentParser.createMeta;
import static com.keenwrite.dom.DocumentParser.walk;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.HttpFacade.httpGet;
import static com.keenwrite.preferences.WorkspaceKeys.*;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.regex.Pattern.UNICODE_CHARACTER_CLASS;
import static java.util.regex.Pattern.compile;
import static org.w3c.dom.Node.TEXT_NODE;

/**
 * Responsible for making an XHTML document complete by wrapping it with html
 * and body elements. This doesn't have to be super-efficient because it's
 * not run in real-time.
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

  /**
   * Responsible for producing a well-formed XML document complete with
   * metadata (title, author, keywords, copyright, and date).
   *
   * @param html The HTML document to transform into an XHTML document.
   * @return The transformed HTML document.
   */
  @Override
  public String apply( final String html ) {
    clue( "Main.status.typeset.xhtml" );

    final var decorated =
      "<html><head><title>untitled</title></head><body>" +
        html +
        "</body></html>";
    final var doc = DocumentParser.parse( decorated );
    setMetaData( doc );

    walk( doc, "img", node -> {
      try {
        final var attrs = node.getAttributes();

        if( attrs != null ) {
          final var attr = attrs.getNamedItem( "src" );

          if( attr != null ) {
            final var imageFile = exportImage( attr.getTextContent() );

            attr.setTextContent( imageFile.toString() );
          }
        }
      } catch( final Exception ex ) {
        clue( ex );
      }
    } );

    //Typographer.curl( doc );

    return DocumentParser.toString( doc );
  }

  /**
   * Applies the metadata fields to the document.
   *
   * @param doc The document to adorn with metadata.
   */
  private void setMetaData( final Document doc ) {
    final var metadata = createMetaData( doc );

    walk( doc, "title", node -> node.setTextContent( getTitle() ) );
    walk( doc, "head", node ->
      metadata.entrySet()
              .forEach( entry -> node.appendChild( createMeta( doc, entry ) ) )
    );
  }

  /**
   * Generates document metadata, including word count.
   *
   * @param doc The document containing the text to tally.
   * @return A map of metadata key/value pairs.
   */
  private Map<String, String> createMetaData( final Document doc ) {
    return Map.of(
      "author", getAuthor(),
      "byline", getByline(),
      "address", getAddress(),
      "phone", getPhone(),
      "email", getEmail(),
      "count", getWordCount( doc ),
      "keywords", getKeywords(),
      "copyright", getCopyright(),
      "date", getDate()
    );
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
      final var response = httpGet( src );
      final var mediaType = response.getMediaType();

      imageFile = mediaType.createTemporaryFile( APP_TITLE_LOWERCASE );

      try( final var image = response.getInputStream() ) {
        copy( image, imageFile, REPLACE_EXISTING );
      }

      // Strip comments, superfluous whitespace, DOCTYPE, and XML declarations.
      if( mediaType.isSvg() ) {
        DocumentParser.sanitize( imageFile );
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
    return getWorkspace().toFile( KEY_IMAGES_DIR ).toString();
  }

  private String getImageOrder() {
    return getWorkspace().toString( KEY_IMAGES_ORDER );
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

  private Locale getLocale() { return getWorkspace().getLocale(); }

  private String getTitle() {
    return resolve( KEY_DOC_TITLE );
  }

  private String getAuthor() {
    return resolve( KEY_DOC_AUTHOR );
  }

  private String getByline() {
    return resolve( KEY_DOC_BYLINE );
  }

  private String getAddress() {
    return resolve( KEY_DOC_ADDRESS ).replaceAll( "\n", "\\\\\\break{}" );
  }

  private String getPhone() {
    return resolve( KEY_DOC_PHONE );
  }

  private String getEmail() {
    return resolve( KEY_DOC_EMAIL );
  }

  private String getWordCount( final Document doc ) {
    final var sb = new StringBuilder( 65536 * 10 );

    walk( doc, "*", node -> {
      if( node.getNodeType() == TEXT_NODE && node.getTextContent() != null ) {
        sb.append( node.getTextContent() );
      }
    } );

    return valueOf( WordCounter.create( getLocale() ).count( sb.toString() ) );
  }

  private String getKeywords() {
    return resolve( KEY_DOC_KEYWORDS );
  }

  private String getCopyright() {
    return resolve( KEY_DOC_COPYRIGHT );
  }

  private String getDate() {
    return resolve( KEY_DOC_DATE );
  }

  private String resolve( final Key key ) {
    return replace( asString( key ), mContext.getResolvedMap() );
  }

  private String asString( final Key key ) {
    return stringProperty( key ).get();
  }

  private StringProperty stringProperty( final Key key ) {
    return getWorkspace().stringProperty( key );
  }
}
