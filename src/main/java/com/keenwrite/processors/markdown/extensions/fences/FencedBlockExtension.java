/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.DiagramUrlGenerator;
import com.keenwrite.processors.DefinitionProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.renderer.DelegatingNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.keenwrite.preferences.WorkspaceKeys.KEY_IMAGES_SERVER;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.renderer.LinkType.LINK;

/**
 * Responsible for converting textual diagram descriptions into HTML image
 * elements.
 */
public class FencedBlockExtension extends HtmlRendererAdapter {
  private final static String DIAGRAM_STYLE = "diagram-";
  private final static int DIAGRAM_STYLE_LEN = DIAGRAM_STYLE.length();

  private final Processor<String> mProcessor;
  private final Workspace mWorkspace;

  public FencedBlockExtension(
    final Processor<String> processor, final Workspace workspace ) {
    assert processor != null;
    assert workspace != null;
    mProcessor = processor;
    mWorkspace = workspace;
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
   * @param processor Used to pre-process the text.
   * @return A new {@link FencedBlockExtension} capable of shunting ASCII
   * diagrams to a service for conversion to SVG.
   */
  public static FencedBlockExtension create(
    final Processor<String> processor, final ProcessorContext context ) {
    assert processor != null;
    assert context != null;
    return new FencedBlockExtension( processor, context.getWorkspace() );
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
          final var server = mWorkspace.toString( KEY_IMAGES_SERVER );
          final var source = DiagramUrlGenerator.toUrl( server, type, text );
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
