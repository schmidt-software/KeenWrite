/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.dom.DocumentParser;
import com.keenwrite.ui.heuristics.WordCounter;
import com.whitemagicsoftware.keenquotes.Contractions;
import com.whitemagicsoftware.keenquotes.Converter;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.keenwrite.Bootstrap.APP_TITLE_LOWERCASE;
import static com.keenwrite.dom.DocumentParser.createMeta;
import static com.keenwrite.dom.DocumentParser.visit;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.HttpFacade.httpGet;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static com.whitemagicsoftware.keenquotes.Converter.CHARS;
import static com.whitemagicsoftware.keenquotes.ParserFactory.ParserType.PARSER_XML;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.nio.file.Files.copy;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Responsible for making an XHTML document complete by wrapping it with html
 * and body elements. This doesn't have to be super-efficient because it's
 * not run in real-time.
 */
public final class XhtmlProcessor extends ExecutorProcessor<String> {
  private final static Converter sTypographer = new Converter(
    lex -> clue( lex.toString() ), contractions(), CHARS, PARSER_XML );

  private final ProcessorContext mContext;

  /**
   * Adorns the given document with {@code html}, {@code head}, and
   * {@code body} elements.
   *
   * @param html The document to decorate.
   * @return A document with a typical HTML structure.
   */
  private static String decorate( final String html ) {
    return
      "<html><head><title> </title><meta charset='utf8'/></head><body>"
        + html
        + "</body></html>";
  }

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

    try {
      final var doc = DocumentParser.parse( decorate( html ) );
      setMetaData( doc );

      visit( doc, "//img", node -> {
        try {
          final var attrs = node.getAttributes();

          final var attr = attrs.getNamedItem( "src" );

          if( attr != null ) {
            final var imageFile = exportImage( attr.getTextContent() );

            attr.setTextContent( imageFile.toString() );
          }
        } catch( final Exception ex ) {
          clue( ex );
        }
      } );

      final var document = DocumentParser.toString( doc );
      final var curl = mContext.getCurlQuotes();

      return curl ? sTypographer.apply( document ) : document;
    } catch( final Exception ex ) {
      clue( ex );
    }

    return html;
  }

  /**
   * Applies the metadata fields to the document.
   *
   * @param doc The document to adorn with metadata.
   */
  private void setMetaData( final Document doc ) {
    final var metadata = createMetaDataMap( doc );
    visit( doc, "/html/head", node ->
      metadata.entrySet()
              .forEach( entry -> node.appendChild( createMeta( doc, entry ) ) )
    );

    final var title = metadata.get( "title" );
    if( title != null ) {
      visit( doc, "/html/head/title", node -> node.setTextContent( title ) );
    }
  }

  /**
   * Generates document metadata, including word count.
   *
   * @param doc The document containing the text to tally.
   * @return A map of metadata key/value pairs.
   */
  private Map<String, String> createMetaDataMap( final Document doc ) {
    final var result = new LinkedHashMap<String, String>();
    final var metadata = getMetadata();
    final var map = mContext.getInterpolatedDefinitions();

    metadata.forEach(
      ( key, value ) -> result.put( key, map.interpolate( value ) )
    );
    result.put( "count", wordCount( doc ) );

    return result;
  }

  /**
   * The metadata is in list form because the user interface for entering the
   * key-value pairs is a table, which requires a generic {@link List} rather
   * than a generic {@link Map}.
   *
   * @return The document metadata.
   */
  private Map<String, String> getMetadata() {
    return mContext.getMetadata();
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
      try( final var response = httpGet( src ) ) {
        final var mediaType = response.getMediaType();

        imageFile = mediaType.createTemporaryFile( APP_TITLE_LOWERCASE );

        try( final var image = response.getInputStream() ) {
          copy( image, imageFile, REPLACE_EXISTING );
        }

        // Strip comments, superfluous whitespace, DOCTYPE, and XML
        // declarations.
        if( mediaType.isSvg() ) {
          DocumentParser.sanitize( imageFile );
        }
      }
    }
    else {
      final var extensions = getImageOrder();
      var imagePath = getImagePath();
      var found = false;

      for( final var extension : extensions ) {
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
    return mContext.getImageDir().toString();
  }

  /**
   * By including an "empty" extension, the first element returned
   * will be the empty string. Thus, the first extension to try is the
   * file's default extension. Subsequent iterations will try to find
   * a file that has a name matching one of the preferred extensions.
   *
   * @return A list of extensions, including an empty string at the start.
   */
  private Iterable<String> getImageOrder() {
    return mContext.getImageOrder();
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

  private Locale getLocale() {
    return mContext.getLocale();
  }

  private String wordCount( final Document doc ) {
    final var sb = new StringBuilder( 65536 * 10 );

    visit(
      doc,
      "//*[normalize-space( text() ) != '']",
      node -> sb.append( node.getTextContent() )
    );

    return valueOf( WordCounter.create( getLocale() ).count( sb.toString() ) );
  }

  /**
   * Creates contracts with a custom set of unambiguous strings.
   *
   * @return List of contractions to use for curling straight quotes.
   */
  private static Contractions contractions() {
    final var builder = new Contractions.Builder();
    return builder.withBeganUnambiguous( List.of( "bout" ) ).build();
  }
}
