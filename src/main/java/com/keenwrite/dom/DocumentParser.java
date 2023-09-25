/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.dom;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.SysFile.toFile;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static javax.xml.transform.OutputKeys.*;
import static javax.xml.xpath.XPathConstants.NODESET;

/**
 * Responsible for initializing an XML parser.
 */
public class DocumentParser {
  private static final String LOAD_EXTERNAL_DTD =
    "http://apache.org/xml/features/nonvalidating/load-external-dtd";
  private static final String INDENT_AMOUNT =
    "{http://xml.apache.org/xslt}indent-amount";

  private static final ByteArrayOutputStream sWriter =
    new ByteArrayOutputStream( 65536 );
  private static final OutputStreamWriter sOutput =
    new OutputStreamWriter( sWriter, UTF_8 );

  /**
   * Caches {@link XPathExpression}s to avoid re-compiling.
   */
  private static final Map<String, XPathExpression> sXpaths = new HashMap<>();

  private static final DocumentBuilderFactory sDocumentFactory;
  private static DocumentBuilder sDocumentBuilder;
  private static Transformer sTransformer;
  private static final XPath sXpath = XPathFactory.newInstance().newXPath();

  public static final DOMImplementation sDomImplementation;

  static {
    sDocumentFactory = DocumentBuilderFactory.newInstance();

    sDocumentFactory.setValidating( false );
    sDocumentFactory.setAttribute( LOAD_EXTERNAL_DTD, false );
    sDocumentFactory.setNamespaceAware( true );
    sDocumentFactory.setIgnoringComments( true );
    sDocumentFactory.setIgnoringElementContentWhitespace( true );

    DOMImplementation domImplementation;

    try {
      sDocumentBuilder = sDocumentFactory.newDocumentBuilder();
      domImplementation = sDocumentBuilder.getDOMImplementation();
      sTransformer = TransformerFactory.newInstance().newTransformer();

      // Ensure Unicode characters (emojis) are encoded correctly.
      sTransformer.setOutputProperty( ENCODING, UTF_16.toString() );
      sTransformer.setOutputProperty( OMIT_XML_DECLARATION, "yes" );
      sTransformer.setOutputProperty( METHOD, "xml" );
      sTransformer.setOutputProperty( INDENT, "no" );
      sTransformer.setOutputProperty( INDENT_AMOUNT, "2" );
    } catch( final Exception ex ) {
      clue( ex );
      domImplementation = sDocumentBuilder.getDOMImplementation();
    }

    sDomImplementation = domImplementation;
  }

  public static Document newDocument() {
    return sDocumentBuilder.newDocument();
  }

  /**
   * Creates a new document object model based on the given XML document
   * string. This will return an empty document if the document could not
   * be parsed.
   *
   * @param xml The document text to convert into a DOM.
   * @return The DOM that represents the given XML data.
   */
  public static Document parse( final String xml ) {
    assert xml != null;

    final var input = new InputSource();

    try( final var reader = new StringReader( xml ) ) {
      input.setEncoding( UTF_8.toString() );
      input.setCharacterStream( reader );

      return sDocumentBuilder.parse( input );
    } catch( final Throwable t ) {
      clue( t );

      return sDocumentBuilder.newDocument();
    }
  }

  /**
   * Parses the given file contents into a document object model.
   *
   * @param doc The source XML document to parse.
   * @return The file as a document object model.
   * @throws IOException  Could not open the document.
   * @throws SAXException Could not read the XML file content.
   */
  public static Document parse( final File doc )
    throws IOException, SAXException {
    assert doc != null;

    try( final var in = new FileInputStream( doc ) ) {
      return parse( in );
    }
  }

  /**
   * Parses the given file contents into a document object model. Callers
   * must close the stream.
   *
   * @param doc The source XML document to parse.
   * @return The {@link InputStream} converted to a document object model.
   * @throws IOException  Could not open the document.
   * @throws SAXException Could not read the XML file content.
   */
  public static Document parse( final InputStream doc )
    throws IOException, SAXException {
    assert doc != null;

    return sDocumentBuilder.parse( doc );
  }

  /**
   * Allows an operation to be applied for every node in the document that
   * matches a given tag name pattern.
   *
   * @param document Document to traverse.
   * @param xpath    Document elements to find via {@link XPath} expression.
   * @param consumer The consumer to call for each matching document node.
   */
  public static void visit(
    final Document document,
    final CharSequence xpath,
    final Consumer<Node> consumer ) {
    assert document != null;
    assert consumer != null;

    try {
      final var expr = compile( xpath );
      final var nodeSet = expr.evaluate( document, NODESET );

      if( nodeSet instanceof NodeList nodes ) {
        for( int i = 0, len = nodes.getLength(); i < len; i++ ) {
          consumer.accept( nodes.item( i ) );
        }
      }
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  public static Node createMeta(
    final Document document, final Map.Entry<String, String> entry ) {
    assert document != null;
    assert entry != null;

    final var node = document.createElement( "meta" );

    node.setAttribute( "name", entry.getKey() );
    node.setAttribute( "content", entry.getValue() );

    return node;
  }

  public static Node createElement(
    final Document doc, final String nodeName, final String nodeValue ) {
    assert doc != null;
    assert nodeName != null;
    assert !nodeName.isBlank();

    final var node = doc.createElement( nodeName );

    if( nodeValue != null ) {
      node.setTextContent( nodeValue );
    }

    return node;
  }

  public static String toString( final Document xhtml ) {
    assert xhtml != null;

    try( final var writer = new StringWriter() ) {
      final var result = new StreamResult( writer );

      transform( xhtml, result );

      return writer.toString();
    } catch( final Exception ex ) {
      clue( ex );
      return "";
    }
  }

  public static String transform( final Element root )
    throws IOException, TransformerException {
    assert root != null;

    try( final var writer = new StringWriter() ) {
      transform( root.getOwnerDocument(), new StreamResult( writer ) );

      return writer.toString();
    }
  }

  /**
   * Remove whitespace, comments, and XML/DOCTYPE declarations to make
   * processing work with ConTeXt.
   *
   * @param path The SVG file to process.
   * @throws Exception The file could not be processed.
   */
  public static void sanitize( final Path path ) throws Exception {
    assert path != null;

    // Preprocessing the SVG image is a single-threaded operation, no matter
    // how many SVG images are in the document to typeset.
    sWriter.reset();

    final var target = new StreamResult( sOutput );
    final var source = sDocumentBuilder.parse( toFile( path ) );

    transform( source, target );
    write( path, sWriter.toByteArray() );
  }

  /**
   * Converts a string into an {@link XPathExpression}, which may be used to
   * extract elements from a {@link Document} object model.
   *
   * @param cs The string to convert to an {@link XPathExpression}.
   * @return {@code null} if there was an error compiling the xpath.
   */
  public static XPathExpression compile( final CharSequence cs ) {
    assert cs != null;

    final var xpath = cs.toString();

    return sXpaths.computeIfAbsent( xpath, k -> {
      try {
        return sXpath.compile( xpath );
      } catch( final XPathExpressionException ex ) {
        clue( ex );
        return null;
      }
    } );
  }

  /**
   * Streams an instance of {@link Document} as a plain text XML document.
   *
   * @param src The source document to transform.
   * @param dst The destination location to write the transformed version.
   * @throws TransformerException Could not transform the document.
   */
  private static void transform( final Document src, final StreamResult dst )
    throws TransformerException {
    sTransformer.transform( new DOMSource( src ), dst );
  }

  /**
   * Use the {@code static} constants and methods, not an instance, at least
   * until an iterable sub-interface is written.
   */
  private DocumentParser() { }
}
