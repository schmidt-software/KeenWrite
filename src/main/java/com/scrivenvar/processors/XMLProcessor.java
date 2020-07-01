/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.processors;

import com.scrivenvar.Services;
import com.scrivenvar.service.Snitch;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.trans.XPathException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.sf.saxon.tree.util.ProcInstParser.getPseudoAttribute;

/**
 * Transforms an XML document. The XML document must have a stylesheet specified
 * as part of its processing instructions, such as:
 *
 * <code>xml-stylesheet type="text/xsl" href="markdown.xsl"</code>
 * <p>
 * The XSL must transform the XML document into Markdown, or another format
 * recognized by the next link on the chain.
 */
public class XMLProcessor extends AbstractProcessor<String>
    implements ErrorListener {

  private final Snitch snitch = Services.load( Snitch.class );

  private XMLInputFactory xmlInputFactory;
  private TransformerFactory transformerFactory;
  private Transformer transformer;

  private Path path;

  /**
   * Constructs an XML processor that can transform an XML document into another
   * format based on the XSL file specified as a processing instruction. The
   * path must point to the directory where the XSL file is found, which implies
   * that they must be in the same directory.
   *
   * @param processor Next link in the processing chain.
   * @param path      The path to the XML file content to be processed.
   */
  public XMLProcessor( final Processor<String> processor, final Path path ) {
    super( processor );
    setPath( path );
  }

  /**
   * Transforms the given XML text into another form (typically Markdown).
   *
   * @param text The text to transform, can be empty, cannot be null.
   * @return The transformed text, or empty if text is empty.
   */
  @Override
  public String process( final String text ) {
    try {
      return text.isEmpty() ? text : transform( text );
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  /**
   * Performs an XSL transformation on the given XML text. The XML text must
   * have a processing instruction that points to the XSL template file to use
   * for the transformation.
   *
   * @param text The text to transform.
   * @return The transformed text.
   */
  private String transform( final String text ) throws Exception {
    // Extract the XML stylesheet processing instruction.
    final String template = getXsltFilename( text );
    final Path xsl = getXslPath( template );

    try(
        final StringWriter output = new StringWriter( text.length() );
        final StringReader input = new StringReader( text ) ) {

      // Listen for external file modification events.
      getSnitch().listen( xsl );

      getTransformer( xsl ).transform(
          new StreamSource( input ),
          new StreamResult( output )
      );

      return output.toString();
    }
  }

  /**
   * Returns an XSL transformer ready to transform an XML document using the
   * XSLT file specified by the given path. If the path is already known then
   * this will return the associated transformer.
   *
   * @param xsl The path to an XSLT file.
   * @return A transformer that will transform XML documents using the given
   * XSLT file.
   * @throws TransformerConfigurationException Could not instantiate the
   *                                           transformer.
   */
  private Transformer getTransformer( final Path xsl )
      throws TransformerConfigurationException {
    if( this.transformer == null ) {
      this.transformer = createTransformer( xsl );
    }

    return this.transformer;
  }

  /**
   * Creates a configured transformer ready to run.
   *
   * @param xsl The stylesheet to use for transforming XML documents.
   * @return The edited XML document transformed into another format (usually
   * markdown).
   * @throws TransformerConfigurationException Could not create the transformer.
   */
  protected Transformer createTransformer( final Path xsl )
      throws TransformerConfigurationException {
    final Source xslt = new StreamSource( xsl.toFile() );

    return getTransformerFactory().newTransformer( xslt );
  }

  private Path getXslPath( final String filename ) {
    final Path xmlPath = getPath();
    final File xmlDirectory = xmlPath.toFile().getParentFile();

    return Paths.get( xmlDirectory.getPath(), filename );
  }

  /**
   * Given XML text, this will use a StAX pull reader to obtain the XML
   * stylesheet processing instruction. This will throw a parse exception if the
   * href pseudo-attribute filename value cannot be found.
   *
   * @param xml The XML containing an xml-stylesheet processing instruction.
   * @return The href pseudo-attribute value.
   * @throws XMLStreamException Could not parse the XML file.
   */
  private String getXsltFilename( final String xml )
      throws XMLStreamException, XPathException {

    String result = "";

    try( final StringReader sr = new StringReader( xml ) ) {
      boolean found = false;
      int count = 0;
      final XMLEventReader reader = createXMLEventReader( sr );

      // If the processing instruction wasn't found in the first 10 lines,
      // fail fast. This should iterate twice through the loop.
      while( !found && reader.hasNext() && count++ < 10 ) {
        final XMLEvent event = reader.nextEvent();

        if( event.isProcessingInstruction() ) {
          final ProcessingInstruction pi = (ProcessingInstruction) event;
          final String target = pi.getTarget();

          if( "xml-stylesheet".equalsIgnoreCase( target ) ) {
            result = getPseudoAttribute( pi.getData(), "href" );
            found = true;
          }
        }
      }
    }

    return result;
  }

  private XMLEventReader createXMLEventReader( final Reader reader )
      throws XMLStreamException {
    return getXMLInputFactory().createXMLEventReader( reader );
  }

  private synchronized XMLInputFactory getXMLInputFactory() {
    if( this.xmlInputFactory == null ) {
      this.xmlInputFactory = createXMLInputFactory();
    }

    return this.xmlInputFactory;
  }

  private XMLInputFactory createXMLInputFactory() {
    return XMLInputFactory.newInstance();
  }

  private synchronized TransformerFactory getTransformerFactory() {
    if( this.transformerFactory == null ) {
      this.transformerFactory = createTransformerFactory();
    }

    return this.transformerFactory;
  }

  /**
   * Returns a high-performance XSLT 2 transformation engine.
   *
   * @return An XSL transforming engine.
   */
  private TransformerFactory createTransformerFactory() {
    final TransformerFactory factory = new TransformerFactoryImpl();

    // Bubble problems up to the user interface, rather than standard error.
    factory.setErrorListener( this );

    return factory;
  }

  /**
   * Called when the XSL transformer issues a warning.
   *
   * @param ex The problem the transformer encountered.
   */
  @Override
  public void warning( final TransformerException ex ) {
    throw new RuntimeException( ex );
  }

  /**
   * Called when the XSL transformer issues an error.
   *
   * @param ex The problem the transformer encountered.
   */
  @Override
  public void error( final TransformerException ex ) {
    throw new RuntimeException( ex );
  }

  /**
   * Called when the XSL transformer issues a fatal error, which is probably
   * a bit over-dramatic a method name.
   *
   * @param ex The problem the transformer encountered.
   */
  @Override
  public void fatalError( final TransformerException ex ) {
    throw new RuntimeException( ex );
  }

  private void setPath( final Path path ) {
    this.path = path;
  }

  private Path getPath() {
    return this.path;
  }

  private Snitch getSnitch() {
    return this.snitch;
  }
}
