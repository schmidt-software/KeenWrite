/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.dom.DocumentParser;
import com.keenwrite.io.MediaTypeExtension;
import com.keenwrite.ui.heuristics.WordCounter;
import com.keenwrite.util.DataTypeConverter;
import com.whitemagicsoftware.keenquotes.parser.Contractions;
import com.whitemagicsoftware.keenquotes.parser.Curler;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.keenwrite.Bootstrap.APP_TITLE_ABBR;
import static com.keenwrite.dom.DocumentParser.*;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static com.whitemagicsoftware.keenquotes.lex.FilterType.FILTER_XML;
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
  private static final Curler sTypographer =
    new Curler( createContractions(), FILTER_XML, true );

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

    try {
      final var doc = parse( html );
      setMetaData( doc );

      visit( doc, "//img", node -> {
        try {
          final var attrs = node.getAttributes();
          final var attr = attrs.getNamedItem( "src" );

          if( attr != null ) {
            final var src = attr.getTextContent();
            final Path location;
            final Path imagesDir;

            // Download into a cache directory, which can be written to without
            // any possibility of overwriting local image files. Further, the
            // filenames are hashed as a second layer of protection.
            if( getProtocol( src ).isRemote() ) {
              location = downloadImage( src );
              imagesDir = getCachesPath();
            }
            else {
              location = resolveImage( src );
              imagesDir = getImagesPath();
            }

            final var relative = imagesDir.relativize( location );

            attr.setTextContent( relative.toString() );
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
    final var title = metadata.get( "title" );

    visit( doc, "/html/head", node -> {
      // Insert <title>text</title> inside <head>.
      node.appendChild( createElement( doc, "title", title ) );

      // Insert each <meta name=x content=y /> inside <head>.
      metadata.entrySet().forEach(
        entry -> node.appendChild( createMeta( doc, entry ) )
      );
    } );
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
      ( key, value ) -> {
        final var interpolated = map.interpolate( value );

        if( !interpolated.isEmpty() ) {
          result.put( key, interpolated );
        }
      }
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
   * Hashes the URL so that the number of files doesn't eat up disk space
   * over time. For static resources, a feature could be added to prevent
   * downloading the URL if the hashed filename already exists.
   *
   * @param src The source file's URL to download.
   * @return A {@link Path} to the local file containing the URL's contents.
   * @throws Exception Could not download or save the file.
   */
  private Path downloadImage( final String src ) throws Exception {
    final Path imageFile;
    final var cachesPath = getCachesPath();

    clue( "Main.status.image.xhtml.image.download", src );

    try( final var response = open( src ) ) {
      final var mediaType = response.getMediaType();

      final var ext = MediaTypeExtension.valueFrom( mediaType ).getExtension();
      final var hash = DataTypeConverter.toHex( DataTypeConverter.hash( src ) );
      final var id = hash.toLowerCase();

      imageFile = cachesPath.resolve( APP_TITLE_ABBR + id + '.' + ext );

      // Preserve image files if auto-remove is turned off.
      if( autoRemove() ) {
        imageFile.toFile().deleteOnExit();
      }

      try( final var image = response.getInputStream() ) {
        copy( image, imageFile, REPLACE_EXISTING );
      }

      if( mediaType.isSvg() ) {
        sanitize( imageFile );
      }
    }

    return imageFile;
  }

  private Path resolveImage( final String src ) throws Exception {
    var imagePath = getImagesPath();
    var found = false;

    Path imageFile = null;

    clue( "Main.status.image.xhtml.image.resolve", src );

    for( final var extension : getImageOrder() ) {
      final var filename = format(
        "%s%s%s", src, extension.isBlank() ? "" : ".", extension );
      imageFile = imagePath.resolve( filename );

      if( imageFile.toFile().exists() ) {
        found = true;
        break;
      }
    }

    if( !found ) {
      imagePath = getDocumentDir();
      imageFile = imagePath.resolve( src );

      if( !imageFile.toFile().exists() ) {
        final var filename = imageFile.toString();
        clue( "Main.status.image.xhtml.image.missing", filename );

        throw new FileNotFoundException( filename );
      }
    }

    clue( "Main.status.image.xhtml.image.found", imageFile.toString() );

    return imageFile;
  }

  private Path getImagesPath() {
    return mContext.getImagesPath();
  }

  private Path getCachesPath() {
    return mContext.getCachesPath();
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

  private boolean autoRemove() {
    return mContext.getAutoRemove();
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
  private static Contractions createContractions() {
    return new Contractions.Builder().build();
  }
}
