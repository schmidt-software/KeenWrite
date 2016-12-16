/*
 * Copyright 2016 White Magic Software, Ltd.
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

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jdom2.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Transforms an XML document. The XML document must have a stylesheet specified
 * as part of its processing instructions, such as:
 *
 * <code>xml-stylesheet type="text/xsl" href="markdown.xsl"</code>
 *
 * The XSL must transform the XML document into Markdown, or another format
 * recognized by the next link on the chain.
 *
 * @author White Magic Software, Ltd.
 */
public class XMLProcessor extends AbstractProcessor<String> {

  private ProcessingInstructionHandler handler = new ProcessingInstructionHandler();
  private String href;
  private Path path;

  /**
   *
   * @param processor Next link in the processing chain.
   * @param path
   */
  public XMLProcessor( final Processor<String> processor, final Path path ) {
    super( processor );
    setPath( path );
  }

  @Override
  public String processLink( final String t ) {
    String result = t;

    try( final StringReader sr = new StringReader( t ) ) {
      SAXParserFactory saxFactory = SAXParserFactory.newInstance();
      SAXParser saxParser = saxFactory.newSAXParser();

      final InputSource is = new InputSource( sr );
      saxParser.parse( is, getHandler() );

    } catch( Exception ex ) {
      System.out.println( ex.getMessage() );
    }

    try(
      final StringReader input = new StringReader( t );
      final StringWriter output = new StringWriter(); ) {
      final Source source = new StreamSource( input );

      final TransformerFactory factory = TransformerFactory.newInstance();
      final Path xmlPath = getPath();
      final File xmlDirectory = xmlPath.toFile().getParentFile();

      final Path xslPath = Paths.get( xmlDirectory.getPath(), getHref() );

      final Source xslt = new StreamSource( xslPath.toFile() );
      final Transformer transformer = factory.newTransformer( xslt );
      
      final StreamResult sr = new StreamResult( output );

      transformer.transform( source, sr );
      
      result = output.toString();

      input.close();
      output.close();
    } catch( Exception e ) {
      System.out.println( e.getMessage() );
    }

    return result;
  }

  private ProcessingInstructionHandler getHandler() {
    return this.handler;
  }

  private String getHref() {
    return this.href;
  }

  private void setHref( final String href ) {
    this.href = href;
  }

  private void setPath( final Path path ) {
    this.path = path;
  }

  private Path getPath() {
    return this.path;
  }

  private class ProcessingInstructionHandler extends DefaultHandler {

    @Override
    public void processingInstruction( final String target, final String data ) {
      final ProcessingInstruction xmlstylesheet
        = new ProcessingInstruction( target, data );
      setHref( xmlstylesheet.getPseudoAttributeValue( "href" ) );
    }
  }
}
