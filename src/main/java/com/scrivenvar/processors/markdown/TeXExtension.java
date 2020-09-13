package com.scrivenvar.processors.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
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
   * Creates an extension capable of handling delimited TeX code in Markdown.
   *
   * @return The new {@link TeXExtension}, never {@code null}.
   */
  public static TeXExtension create() {
    return new TeXExtension();
  }

  /**
   * Force using the {@link #create()} method for consistency.
   */
  private TeXExtension() {
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
      builder.nodeRendererFactory( new TeXNodeRenderer.Factory() );
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
