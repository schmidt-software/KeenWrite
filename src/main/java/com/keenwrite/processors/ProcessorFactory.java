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
package com.keenwrite.processors;

import com.keenwrite.AbstractFileFactory;
import com.keenwrite.preview.HTMLPreviewPane;
import com.keenwrite.processors.markdown.MarkdownProcessor;

import java.nio.file.Path;
import java.util.Map;

import static com.keenwrite.ExportFormat.NONE;

/**
 * Responsible for creating processors capable of parsing, transforming,
 * interpolating, and rendering known file types.
 */
public class ProcessorFactory extends AbstractFileFactory {

  private final ProcessorContext mProcessorContext;
  private final Processor<String> mMarkdownProcessor;

  /**
   * Constructs a factory with the ability to create processors that can perform
   * text and caret processing to generate a final preview.
   *
   * @param processorContext Parameters needed to construct various processors.
   */
  private ProcessorFactory( final ProcessorContext processorContext ) {
    mProcessorContext = processorContext;
    mMarkdownProcessor = createMarkdownProcessor();
  }

  private Processor<String> createProcessor() {
    final ProcessorContext context = getProcessorContext();

    final Processor<String> successor;

    if( context.isExportFormat( NONE ) ) {
      // If the content is not to be exported, then the successor processor
      // is one that parses Markdown into HTML and passes the string to the
      // HTML preview pane.
      successor = getCommonProcessor();
    }
    else {
      // Otherwise, bolt on a processor that--after the interpolation and
      // substitution phase, which includes text strings or R code---will
      // generate HTML or plain Markdown. HTML has a few output formats:
      // with embedded SVG representing formulas, or without any conversion
      // to SVG. Without conversion would require client-side rendering of
      // math (such as using the KaTeX engine, written in JavaScript).
      successor = switch( context.getExportFormat() ) {
        case HTML_SVG -> createHtmlSvgProcessor();
        case HTML_TEX -> createHtmlTexProcessor();
        case MARKDOWN_PLAIN -> createMarkdownPlainProcessor();
        case NONE -> null;
      };
    }

    return switch( context.getFileType() ) {
      case RMARKDOWN -> createRProcessor( successor );
      case SOURCE -> createMarkdownDefinitionProcessor( successor );
      case RXML -> createRXMLProcessor( successor );
      case XML -> createXMLProcessor( successor );
      default -> createHtmlPreProcessor();
    };
  }

  /**
   * Creates a processor chain suitable for parsing and rendering the file
   * opened at the given tab.
   *
   * @param context The tab containing a text editor, path, and caret position.
   * @return A processor that can render the given tab's text.
   */
  public static Processor<String> createProcessors(
      final ProcessorContext context ) {
    return new ProcessorFactory( context ).createProcessor();
  }

  /**
   * Executes the processing chain, operating on the given string.
   *
   * @param handler The first processor in the chain to call.
   * @param text    The initial value of the text to process.
   * @return The final value of the text that was processed by the chain.
   */
  public static String processChain( Processor<String> handler, String text ) {
    while( handler != null && text != null ) {
      text = handler.apply( text );
      handler = handler.next();
    }

    return text;
  }

  private Processor<String> createHTMLPreviewProcessor() {
    return new HtmlPreviewProcessor( getPreviewPane() );
  }

  /**
   * Creates and links the processors at the end of the processing chain.
   *
   * @return A markdown, caret replacement, and preview pane processor chain.
   */
  private Processor<String> createMarkdownProcessor() {
    final var hpp = createHTMLPreviewProcessor();
    return new MarkdownProcessor( hpp, getPreviewPane().getPath() );
  }

  private Processor<String> createHtmlPreProcessor(
      final Processor<String> successor ) {
    return new PreformattedProcessor( successor );
  }

  private Processor<String> createIdentityProcessor() {
    return new IdentityProcessor( null );
  }

  private Processor<String> createHtmlPreProcessor() {
    return createHtmlPreProcessor( createHTMLPreviewProcessor() );
  }

  private Processor<String> createDefinitionProcessor(
      final Processor<String> successor ) {
    return new DefinitionProcessor( successor, getResolvedMap() );
  }

  private Processor<String> createRProcessor(
      final Processor<String> successor ) {
    final var rp = new InlineRProcessor( successor, getResolvedMap() );
    return new RVariableProcessor( rp, getResolvedMap() );
  }

  private Processor<String> createMarkdownDefinitionProcessor(
      final Processor<String> successor ) {
    return createDefinitionProcessor( successor );
  }

  protected Processor<String> createRXMLProcessor(
      final Processor<String> successor ) {
    final var xmlp = new XmlProcessor( successor, getPath() );
    final var rp = new InlineRProcessor( xmlp, getResolvedMap() );
    return new RVariableProcessor( rp, getResolvedMap() );
  }

  private Processor<String> createXMLProcessor(
      final Processor<String> successor ) {
    final var xmlp = new XmlProcessor( successor, getPath() );
    return createDefinitionProcessor( xmlp );
  }

  private Processor<String> createHtmlSvgProcessor() {
    return new MarkdownProcessor( null, getPreviewPane().getPath() );
  }

  private Processor<String> createHtmlTexProcessor() {
    return new MarkdownProcessor( null, getPreviewPane().getPath() );
  }

  private Processor<String> createMarkdownPlainProcessor() {
    return createIdentityProcessor();
  }

  /**
   * Returns a processor common to all processors: markdown, caret position
   * token replacer, and an HTML preview renderer.
   *
   * @return Processors at the end of the processing chain.
   */
  private Processor<String> getCommonProcessor() {
    return mMarkdownProcessor;
  }

  private ProcessorContext getProcessorContext() {
    return mProcessorContext;
  }

  private HTMLPreviewPane getPreviewPane() {
    return getProcessorContext().getPreviewPane();
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return getProcessorContext().getResolvedMap();
  }

  private Path getPath() {
    return getProcessorContext().getPath();
  }
}
