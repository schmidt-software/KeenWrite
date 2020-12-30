/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.processors.DefinitionProcessor;
import com.keenwrite.processors.IdentityProcessor;
import com.keenwrite.processors.ProcessorContext;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;

import static com.keenwrite.StatusBarNotifier.clue;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import static com.vladsch.flexmark.html.renderer.LinkType.LINK;
import static java.lang.String.format;
import static java.util.Base64.getUrlEncoder;
import static java.util.zip.Deflater.BEST_COMPRESSION;
import static java.util.zip.Deflater.FULL_FLUSH;

/**
 * Responsible for converting textual diagram descriptions into HTML image
 * elements.
 */
public class FencedBlockExtension implements HtmlRendererExtension {
  private final static String DIAGRAM_STYLE = "diagram-";
  private final static int DIAGRAM_STYLE_LEN = DIAGRAM_STYLE.length();

  private final DefinitionProcessor mProcessor;

  public FencedBlockExtension( final ProcessorContext context ) {
    assert context != null;
    mProcessor = new DefinitionProcessor( IdentityProcessor.INSTANCE, context );
  }

  /**
   * Creates a new parser for fenced blocks. This calls out to a web service
   * to generate SVG files of text diagrams.
   * <p>
   * Internally, this creates a {@link DefinitionProcessor} to substitute
   * variable definitions. This is necessary because the order of processors
   * matters. If the {@link DefinitionProcessor} comes before an instance of
   * {@link MarkdownProcessor}, for example, then the caret position in the
   * preview pane will not align with the caret position in the editor
   * pane. The {@link MarkdownProcessor} must come before all else. However,
   * when parsing fenced blocks, the variables within the block must be
   * interpolated before being sent to the diagram web service.
   * </p>
   *
   * @param context Used to create a new {@link DefinitionProcessor}.
   * @return A new {@link FencedBlockExtension} capable of shunting ASCII
   * diagrams to a service for conversion to SVG.
   */
  public static FencedBlockExtension create( final ProcessorContext context ) {
    return new FencedBlockExtension( context );
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
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
          final var text = FencedBlockExtension.this.mProcessor.apply( content );
          final var encoded = encode( text );
          final var source = format(
            "https://kroki.io/%s/svg/%s", type, encoded );

          final var link = context.resolveLink( LINK, source, false );

          html.attr( "src", source );
          html.withAttr( link );
          html.tagVoid( "img" );
        }
        else {
          context.delegateRender();
        }
      } ) );

      return set;
    }

    private byte[] compress( byte[] source ) {
      final var inLen = source.length;
      final var result = new byte[ inLen ];
      final var deflater = new Deflater( BEST_COMPRESSION );

      deflater.setInput( source, 0, inLen );
      deflater.finish();
      final var outLen = deflater.deflate( result, 0, inLen, FULL_FLUSH );
      deflater.end();

      try( final var out = new ByteArrayOutputStream() ) {
        out.write( result, 0, outLen );
        return out.toByteArray();
      } catch( final Exception ex ) {
        clue( ex );
        throw new RuntimeException( ex );
      }
    }

    private String encode( final String decoded ) {
      return getUrlEncoder().encodeToString( compress( decoded.getBytes() ) );
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
