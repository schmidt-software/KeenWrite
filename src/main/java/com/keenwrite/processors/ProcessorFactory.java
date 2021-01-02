/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import com.keenwrite.AbstractFileFactory;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.markdown.MarkdownProcessor;

import static com.keenwrite.ExportFormat.NONE;

/**
 * Responsible for creating processors capable of parsing, transforming,
 * interpolating, and rendering known file types.
 */
public class ProcessorFactory extends AbstractFileFactory {

  private final ProcessorContext mContext;

  /**
   * Constructs a factory with the ability to create processors that can perform
   * text and caret processing to generate a final preview.
   *
   * @param context Parameters needed to construct various processors.
   */
  private ProcessorFactory( final ProcessorContext context ) {
    mContext = context;
  }

  private Processor<String> createProcessor() {
    final var context = getProcessorContext();

    // If the content is not to be exported, then the successor processor
    // is one that parses Markdown into HTML and passes the string to the
    // HTML preview pane.
    //
    // Otherwise, bolt on a processor that--after the interpolation and
    // substitution phase, which includes text strings or R code---will
    // generate HTML or plain Markdown. HTML has a few output formats:
    // with embedded SVG representing formulas, or without any conversion
    // to SVG. Without conversion would require client-side rendering of
    // math (such as using the JavaScript-based KaTeX engine).
    final var successor = context.isExportFormat( NONE )
      ? createHtmlPreviewProcessor()
      : createIdentityProcessor();

    final var processor = switch( context.getFileType() ) {
      case RMARKDOWN -> createRProcessor( successor );
      case SOURCE -> createMarkdownProcessor( successor );
      case RXML -> createRXMLProcessor( successor );
      case XML -> createXMLProcessor( successor );
      default -> createPreformattedProcessor( successor );
    };

    return new ExecutorProcessor<>( processor );
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
   * Instantiates a new {@link Processor} that has no successor and returns
   * the string it was given without modification.
   *
   * @return An instance of {@link Processor} that performs no processing.
   */
  private Processor<String> createIdentityProcessor() {
    return IdentityProcessor.IDENTITY;
  }

  /**
   * Instantiates a new {@link Processor} that passes an incoming HTML
   * string to a user interface widget that can render HTML as a web page.
   *
   * @return An instance of {@link Processor} that forwards HTML for display.
   */
  private Processor<String> createHtmlPreviewProcessor() {
    return new HtmlPreviewProcessor( getPreviewPane() );
  }

  /**
   * Instantiates a {@link Processor} responsible for parsing Markdown and
   * definitions.
   *
   * @return A chain of {@link Processor}s for processing Markdown and
   * definitions.
   */
  private Processor<String> createMarkdownProcessor(
    final Processor<String> successor ) {
    final var dp = createDefinitionProcessor( successor );
    return MarkdownProcessor.create( dp, getProcessorContext() );
  }

  private Processor<String> createDefinitionProcessor(
    final Processor<String> successor ) {
    return new DefinitionProcessor( successor, getProcessorContext() );
  }

  private Processor<String> createRProcessor(
    final Processor<String> successor ) {
    final var irp = new InlineRProcessor( successor, getProcessorContext() );
    final var rvp = new RVariableProcessor( irp, getProcessorContext() );
    return MarkdownProcessor.create( rvp, getProcessorContext() );
  }

  protected Processor<String> createRXMLProcessor(
    final Processor<String> successor ) {
    final var xmlp = new XmlProcessor( successor, getProcessorContext() );
    return createRProcessor( xmlp );
  }

  private Processor<String> createXMLProcessor(
    final Processor<String> successor ) {
    final var xmlp = new XmlProcessor( successor, getProcessorContext() );
    return createDefinitionProcessor( xmlp );
  }

  private Processor<String> createPreformattedProcessor(
    final Processor<String> successor ) {
    return new PreformattedProcessor( successor );
  }

  private ProcessorContext getProcessorContext() {
    return mContext;
  }

  private HtmlPreview getPreviewPane() {
    return getProcessorContext().getPreview();
  }
}
