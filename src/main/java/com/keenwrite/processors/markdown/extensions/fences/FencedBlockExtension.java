/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.keenwrite.preview.DiagramUrlGenerator;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.renderer.CoreNodeRenderer.CODE_CONTENT;
import static com.vladsch.flexmark.html.renderer.LinkType.LINK;

/**
 * Responsible for converting textual diagram descriptions into HTML image
 * elements.
 */
public class FencedBlockExtension extends HtmlRendererAdapter {
  private final static String DIAGRAM_STYLE = "diagram-";
  private final static int DIAGRAM_STYLE_LEN = DIAGRAM_STYLE.length();

  private final Processor<String> mProcessor;
  private final ProcessorContext mContext;

  public FencedBlockExtension(
    final Processor<String> processor, final ProcessorContext context ) {
    assert processor != null;
    assert context != null;
    mProcessor = processor;
    mContext = context;
  }

  /**
   * Creates a new parser for fenced blocks. This calls out to a web service
   * to generate SVG files of text diagrams.
   * <p>
   * Internally, this creates a {@link VariableProcessor} to substitute
   * variable definitions. This is necessary because the order of processors
   * matters. If the {@link VariableProcessor} comes before an instance of
   * {@link MarkdownProcessor}, for example, then the caret position in the
   * preview pane will not align with the caret position in the editor
   * pane. The {@link MarkdownProcessor} must come before all else. However,
   * when parsing fenced blocks, the variables within the block must be
   * interpolated before being sent to the diagram web service.
   * </p>
   *
   * @param processor Used to pre-process the text.
   * @return A new {@link FencedBlockExtension} capable of shunting ASCII
   * diagrams to a service for conversion to SVG.
   */
  public static FencedBlockExtension create(
    final Processor<String> processor, final ProcessorContext context ) {
    assert processor != null;
    assert context != null;
    return new FencedBlockExtension( processor, context );
  }

  @Override
  public void extend(
    @NotNull final Builder builder, @NotNull final String rendererType ) {
    builder.nodeRendererFactory( new Factory() );
  }

  /**
   * Converts the given {@link BasedSequence} to a lowercase value.
   *
   * @param text The character string to convert to lowercase.
   * @return The lowercase text value, or the empty string for no text.
   */
  private static String sanitize( final BasedSequence text ) {
    assert text != null;
    return text.toString().toLowerCase();
  }

  /**
   * Responsible for generating images from a fenced block that contains a
   * diagram reference.
   */
  private class CustomRenderer implements NodeRenderer {

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      final var set = new HashSet<NodeRenderingHandler<?>>();

      set.add( new NodeRenderingHandler<>(
        FencedCodeBlock.class, ( node, context, html ) -> {
        final var style = sanitize( node.getInfo() );

        if( style.startsWith( DIAGRAM_STYLE ) ) {
          final var type = style.substring( DIAGRAM_STYLE_LEN );
          final var content = node.getContentChars().normalizeEOL();
          final var text = mProcessor.apply( content );
          final var server = mContext.getImagesServer();
          final var source = DiagramUrlGenerator.toUrl( server, type, text );
          final var link = context.resolveLink( LINK, source, false );

          html.attr( "src", source );
          html.withAttr( link );
          html.tagVoid( "img" );
        }
        else {
          // TODO: Revert to using context.delegateRender() after flexmark
          //   is updated to no longer trim blank lines up to the EOL.
          render( node, context, html );
        }
      } ) );

      return set;
    }

    /**
     * This method is a stop-gap because blank lines that contain only
     * whitespace are collapsed into lines without any spaces. Consequently,
     * the typesetting software does not honour the blank lines, which
     * then would otherwise discard blank lines entirely.
     * <p>
     * Given the following:
     *
     * <pre>
     *   if( bool ) {
     *
     *
     *   }
     * </pre>
     * <p>
     * The typesetter would otherwise render this incorrectly as:
     *
     * <pre>
     *   if( bool ) {
     *   }
     * </pre>
     * <p>
     */
    private void render(
      final FencedCodeBlock node,
      final NodeRendererContext context,
      final HtmlWriter html ) {
      assert node != null;
      assert context != null;
      assert html != null;

      html.line();
      html.srcPosWithTrailingEOL( node.getChars() )
          .withAttr()
          .tag( "pre" )
          .openPre();

      final var options = context.getHtmlOptions();
      final var languageClass = lookupLanguageClass( node, options );

      if( !languageClass.isBlank() ) {
        html.attr( "class", languageClass );
      }

      html.srcPosWithEOL( node.getContentChars() )
          .withAttr( CODE_CONTENT )
          .tag( "code" );

      final var lines = node.getContentLines();

      for( final var line : lines ) {
        if( line.isBlank() ) {
          html.text( "    " );
        }

        html.text( line );
      }

      html.tag( "/code" );
      html.tag( "/pre" )
          .closePre();
      html.lineIf( options.htmlBlockCloseTagEol );
    }

    private String lookupLanguageClass(
      final FencedCodeBlock node,
      final HtmlRendererOptions options ) {
      assert node != null;
      assert options != null;

      final var info = node.getInfo();

      if( info.isNotNull() && !info.isBlank() ) {
        final var lang = node
          .getInfoDelimitedByAny( options.languageDelimiterSet )
          .unescape();
        return options
          .languageClassMap
          .getOrDefault( lang, options.languageClassPrefix + lang );
      }

      return options.noLanguageClass;
    }
  }

  private class Factory implements DelegatingNodeRendererFactory {
    public Factory() {}

    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return new CustomRenderer();
    }

    /**
     * Return {@code null} to indicate this may delegate to the core renderer.
     */
    @Override
    public Set<Class<?>> getDelegates() {
      return null;
    }
  }
}
