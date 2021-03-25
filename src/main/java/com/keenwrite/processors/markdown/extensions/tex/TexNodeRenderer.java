/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

import com.keenwrite.ExportFormat;
import com.keenwrite.preview.SvgRasterizer;
import com.keenwrite.processors.Processor;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.preview.MathRenderer.MATH_RENDERER;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.*;

public class TexNodeRenderer {
  private static final RendererFacade RENDERER =
    new TexElementNodeRenderer( false );

  private static final Map<ExportFormat, RendererFacade> EXPORT_RENDERERS =
    Map.of(
      APPLICATION_PDF, new TexElementNodeRenderer( true ),
      HTML_TEX_SVG, new TexSvgNodeRenderer(),
      HTML_TEX_DELIMITED, new TexDelimNodeRenderer(),
      XHTML_TEX, new TexElementNodeRenderer( true ),
      MARKDOWN_PLAIN, new TexDelimNodeRenderer(),
      NONE, RENDERER
    );

  public static class Factory implements NodeRendererFactory {
    private final RendererFacade mNodeRenderer;

    public Factory(
      final ExportFormat exportFormat, final Processor<String> processor ) {
      mNodeRenderer = EXPORT_RENDERERS.getOrDefault( exportFormat, RENDERER );
      mNodeRenderer.setProcessor( processor );
    }

    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return mNodeRenderer;
    }
  }

  private static abstract class RendererFacade
    implements NodeRenderer {
    private Processor<String> mProcessor;

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      return Set.of(
        new NodeRenderingHandler<>( TexNode.class, this::render )
      );
    }

    /**
     * Subclasses implement this method to render the content of {@link TexNode}
     * instances as per their associated {@link ExportFormat}.
     *
     * @param node    {@link Node} containing text content of a math formula.
     * @param context Configuration information (unused).
     * @param html    Where to write the rendered output.
     */
    abstract void render( final TexNode node,
                          final NodeRendererContext context,
                          final HtmlWriter html );

    private void setProcessor( final Processor<String> processor ) {
      mProcessor = processor;
    }

    Processor<String> getProcessor() {
      return mProcessor;
    }
  }

  /**
   * Responsible for rendering a TeX node as an HTML {@code <tex>}
   * element. This is the default behaviour.
   */
  private static class TexElementNodeRenderer extends RendererFacade {
    private final boolean mIncludeDelimiter;

    private TexElementNodeRenderer( final boolean includeDelimiter ) {
      mIncludeDelimiter = includeDelimiter;
    }

    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      final var text = getProcessor().apply( node.getText().toString() );
      final var content =
        mIncludeDelimiter
          ? node.getOpeningDelimiter() + text + node.getClosingDelimiter()
          : text;
      html.tag( HTML_TEX );
      html.raw( content );
      html.closeTag( HTML_TEX );
    }
  }

  /**
   * Responsible for rendering a TeX node as an HTML {@code <svg>}
   * element.
   */
  private static class TexSvgNodeRenderer extends RendererFacade {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      final var tex = node.getText().toStringOrNull();
      final var doc = MATH_RENDERER.render(
        tex == null ? "" : getProcessor().apply( tex ) );
      final var svg = SvgRasterizer.toSvg( doc.getDocumentElement() );
      html.raw( svg );
    }
  }

  /**
   * Responsible for rendering a TeX node as text bracketed by $ tokens.
   */
  private static class TexDelimNodeRenderer extends RendererFacade {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      html.raw( TOKEN_OPEN );
      html.raw( getProcessor().apply( node.getText().toString() ) );
      html.raw( TOKEN_CLOSE );
    }
  }
}
