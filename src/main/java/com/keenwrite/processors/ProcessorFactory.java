/* Copyright 2020-2024 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors;

import com.keenwrite.processors.html.PreformattedProcessor;
import com.keenwrite.processors.html.XhtmlProcessor;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.processors.pdf.PdfProcessor;
import com.keenwrite.processors.text.TextProcessor;
import com.keenwrite.processors.variable.VariableProcessor;

import static com.keenwrite.ExportFormat.TEXT_TEX;
import static com.keenwrite.io.FileType.RMARKDOWN;
import static com.keenwrite.io.FileType.SOURCE;
import static com.keenwrite.processors.html.IdentityProcessor.IDENTITY;

/**
 * Responsible for creating processors capable of parsing, transforming,
 * interpolating, and rendering known file types.
 */
public final class ProcessorFactory {

  private ProcessorFactory() {
  }

  public static Processor<String> createProcessors(
    final ProcessorContext context ) {
    return createProcessors( context, null );
  }

  /**
   * Creates a new {@link Processor} chain suitable for parsing and rendering
   * the file opened at the given tab.
   *
   * @param context The tab containing a text editor, path, and caret position.
   * @return A processor that can render the given tab's text.
   */
  public static Processor<String> createProcessors(
    final ProcessorContext context, final Processor<String> preview ) {
    return createProcessor( context, preview );
  }

  /**
   * Constructs processors that chain various processing operations on a
   * document to generate a transformed version of the source document.
   *
   * @param context Parameters needed to construct various processors.
   * @param preview The processor to use when no export format is specified.
   */
  private static Processor<String> createProcessor(
    final ProcessorContext context, final Processor<String> preview ) {
    // If the content is not to be exported, then the successor processor
    // is one that parses Markdown into HTML and passes the string to the
    // HTML preview pane.
    //
    // Otherwise, bolt on a processor that---after the interpolation and
    // substitution phase, which includes text strings or R code---will
    // generate HTML or plain Markdown. HTML has a few output formats:
    // with embedded SVG representing formulas, or without any conversion
    // to SVG. Without conversion would require client-side rendering of
    // math (such as using the JavaScript-based KaTeX engine).
    final var outputType = context.getExportFormat();

    final var successor = switch( outputType ) {
      case NONE -> preview;
      case XHTML_TEX -> createXhtmlProcessor( context );
      case TEXT_TEX -> createTextProcessor( context );
      case APPLICATION_PDF -> createPdfProcessor( context );
      default -> createIdentityProcessor( context );
    };

    final var inputType = context.getSourceFileType();
    final Processor<String> processor;

    if( preview == null ) {
      if( outputType == TEXT_TEX ) {
        processor = successor;
      }
      else {
        processor = createMarkdownProcessor( successor, context );
      }
    }
    else {
      processor = inputType == SOURCE || inputType == RMARKDOWN
        ? createMarkdownProcessor( successor, context )
        : createPreformattedProcessor( successor );
    }

    return new ExecutorProcessor<>( processor );
  }

  /**
   * Instantiates a new {@link Processor} that has no successor and returns
   * the string it was given without modification.
   *
   * @return An instance of {@link Processor} that performs no processing.
   */
  @SuppressWarnings( "unused" )
  private static Processor<String> createIdentityProcessor(
    final ProcessorContext ignored ) {
    return IDENTITY;
  }

  /**
   * Instantiates a {@link Processor} responsible for parsing Markdown and
   * definitions.
   *
   * @return A chain of {@link Processor}s for processing Markdown and
   * definitions.
   */
  private static Processor<String> createMarkdownProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    final var dp = createVariableProcessor( successor, context );
    return MarkdownProcessor.create( dp, context );
  }

  private static Processor<String> createVariableProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    return new VariableProcessor( successor, context );
  }

  /**
   * Instantiates a new {@link Processor} that wraps an HTML document into
   * its final, well-formed state (including head and body tags). This is
   * useful for generating XHTML documents suitable for typesetting (using
   * an engine such as LuaTeX).
   *
   * @return An instance of {@link Processor} that completes an HTML document.
   */
  private static Processor<String> createXhtmlProcessor(
    final ProcessorContext context ) {
    return createXhtmlProcessor( IDENTITY, context );
  }

  private static Processor<String> createTextProcessor(
    final ProcessorContext context ) {
    return new TextProcessor( IDENTITY, context );
  }

  private static Processor<String> createXhtmlProcessor(
    final Processor<String> successor, final ProcessorContext context ) {
    return new XhtmlProcessor( successor, context );
  }

  private static Processor<String> createPdfProcessor(
    final ProcessorContext context ) {
    final var pdfProcessor = new PdfProcessor( context );
    return createXhtmlProcessor( pdfProcessor, context );
  }

  private static Processor<String> createPreformattedProcessor(
    final Processor<String> successor ) {
    return new PreformattedProcessor( successor );
  }
}
