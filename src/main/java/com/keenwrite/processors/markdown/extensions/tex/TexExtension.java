/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

import com.keenwrite.ExportFormat;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.keenwrite.processors.markdown.extensions.tex.TexNodeRenderer.Factory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.vladsch.flexmark.parser.Parser.ParserExtension;

/**
 * Responsible for wrapping delimited TeX code in Markdown into an XML element
 * that the HTML renderer can handle. For example, {@code $E=mc^2$} becomes
 * {@code <tex>E=mc^2</tex>} when passed to HTML renderer. The HTML renderer
 * is responsible for converting the TeX code for display. This avoids inserting
 * SVG code into the Markdown document, which the parser would then have to
 * iterate---a <em>very</em> wasteful operation that impacts front-end
 * performance.
 */
public class TexExtension extends HtmlRendererAdapter
  implements ParserExtension {

  /**
   * Responsible for pre-parsing the input.
   */
  private final Function<String, String> mEvaluator;

  /**
   * Controls how the node renderer produces TeX code within HTML output.
   */
  private final ExportFormat mExportFormat;

  private TexExtension(
    final Function<String, String> evaluator, final ProcessorContext context ) {
    mEvaluator = evaluator;
    mExportFormat = context.getExportFormat();
  }

  /**
   * Creates an extension capable of handling delimited TeX code in Markdown.
   *
   * @return The new {@link TexExtension}, never {@code null}.
   */
  public static TexExtension create(
    final Function<String, String> evaluator, final ProcessorContext context ) {
    return new TexExtension( evaluator, context );
  }

  /**
   * Adds the TeX extension for HTML document export types.
   *
   * @param builder      The document builder.
   * @param rendererType Indicates the document type to be built.
   */
  @Override
  public void extend( @NotNull final HtmlRenderer.Builder builder,
                      @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new Factory( mExportFormat, mEvaluator ) );
    }
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.customDelimiterProcessor( new TexInlineDelimiterProcessor() );
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {}
}
