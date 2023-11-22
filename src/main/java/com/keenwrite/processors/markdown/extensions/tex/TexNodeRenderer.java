/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

import com.keenwrite.ExportFormat;
import com.keenwrite.preview.MathRenderer;
import com.keenwrite.preview.SvgRasterizer;
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
import java.util.function.Function;

import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.*;

public class TexNodeRenderer {
  private static final RendererFacade RENDERER =
    new TexElementNodeRenderer( false );

  private static final Map<ExportFormat, RendererFacade> EXPORT_RENDERERS =
    Map.of(
      APPLICATION_PDF, new TexElementNodeRenderer( true ),
      HTML_TEX_SVG, new TexSvgNodeRenderer(),
      HTML_TEX_DELIMITED, new TexDelimitedNodeRenderer(),
      XHTML_TEX, new TexElementNodeRenderer( true ),
      NONE, RENDERER
    );

  public static class Factory implements NodeRendererFactory {
    private final RendererFacade mNodeRenderer;

    public Factory(
      final ExportFormat exportFormat,
      final Function<String, String> evaluator ) {
      final var format = exportFormat == null ? NONE : exportFormat;

      mNodeRenderer = EXPORT_RENDERERS.getOrDefault( format, RENDERER );
      mNodeRenderer.setEvaluator( evaluator );
    }

    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return mNodeRenderer;
    }
  }

  private static abstract class RendererFacade
    implements NodeRenderer {
    private Function<String, String> mEvaluator;

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

    private void setEvaluator( final Function<String, String> evaluator ) {
      mEvaluator = evaluator;
    }

    Function<String, String> getEvaluator() {
      return mEvaluator;
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
      final var text = getEvaluator().apply( node.getText().toString() );
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
      final var doc = MathRenderer.toDocument(
        tex == null ? "" : getEvaluator().apply( tex )
      );
      final var svg = SvgRasterizer.toSvg( doc.getDocumentElement() );
      html.raw( svg );
    }
  }

  /**
   * Responsible for rendering a TeX node as text bracketed by $ tokens.
   */
  private static class TexDelimitedNodeRenderer extends RendererFacade {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      html.raw( TOKEN_OPEN );
      html.raw( getEvaluator().apply( node.getText().toString() ) );
      html.raw( TOKEN_CLOSE );
    }
  }
}
