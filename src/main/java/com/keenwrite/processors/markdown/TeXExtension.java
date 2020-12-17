/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.ExportFormat;
import com.keenwrite.processors.markdown.tex.TeXInlineDelimiterProcessor;
import com.keenwrite.processors.markdown.tex.TexNodeRenderer.Factory;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.misc.Extension;
import org.jetbrains.annotations.NotNull;

import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
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
public class TeXExtension implements ParserExtension, HtmlRendererExtension {
  /**
   * Controls how the node renderer produces TeX code within HTML output.
   */
  private final ExportFormat mExportFormat;

  /**
   * Creates an extension capable of handling delimited TeX code in Markdown.
   *
   * @return The new {@link TeXExtension}, never {@code null}.
   */
  public static TeXExtension create( final ExportFormat format ) {
    return new TeXExtension( format );
  }

  /**
   * Force using the {@link #create(ExportFormat)} method for consistency with
   * the other {@link Extension} creation invocations.
   */
  private TeXExtension( final ExportFormat exportFormat ) {
    mExportFormat = exportFormat;
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
      builder.nodeRendererFactory( new Factory( mExportFormat ) );
    }
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.customDelimiterProcessor( new TeXInlineDelimiterProcessor() );
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {
  }
}
