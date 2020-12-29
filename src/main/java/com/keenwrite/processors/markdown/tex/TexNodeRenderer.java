/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.tex;

import com.keenwrite.ExportFormat;
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

import java.util.Set;

import static com.keenwrite.preview.MathRenderer.MATH_RENDERER;
import static com.keenwrite.processors.markdown.tex.TexNode.*;

public class TexNodeRenderer {

  public static class Factory implements NodeRendererFactory {
    private final ExportFormat mExportFormat;

    public Factory( final ExportFormat exportFormat ) {
      mExportFormat = exportFormat;
    }

    @NotNull
    @Override
    public NodeRenderer apply( @NotNull DataHolder options ) {
      return switch( mExportFormat ) {
        case HTML_TEX_SVG -> new TexSvgNodeRenderer();
        case HTML_TEX_DELIMITED, MARKDOWN_PLAIN -> new TexDelimNodeRenderer();
        case NONE -> new TexElementNodeRenderer();
      };
    }
  }

  private static abstract class AbstractTexNodeRenderer
      implements NodeRenderer {

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      final var h = new NodeRenderingHandler<>( TexNode.class, this::render );
      return Set.of( h );
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
  }

  /**
   * Responsible for rendering a TeX node as an HTML {@code <tex>}
   * element. This is the default behaviour.
   */
  private static class TexElementNodeRenderer extends AbstractTexNodeRenderer {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      html.tag( HTML_TEX );
      html.raw( node.getText() );
      html.closeTag( HTML_TEX );
    }
  }

  /**
   * Responsible for rendering a TeX node as an HTML {@code <svg>}
   * element.
   */
  private static class TexSvgNodeRenderer extends AbstractTexNodeRenderer {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      final var tex = node.getText().toStringOrNull();
      final var doc = MATH_RENDERER.render( tex == null ? "" : tex );
      final var svg = SvgRasterizer.toSvg( doc.getDocumentElement() );
      html.raw( svg );
    }
  }

  /**
   * Responsible for rendering a TeX node as text bracketed by $ tokens.
   */
  private static class TexDelimNodeRenderer extends AbstractTexNodeRenderer {
    void render( final TexNode node,
                 final NodeRendererContext context,
                 final HtmlWriter html ) {
      html.raw( TOKEN_OPEN );
      html.raw( node.getText() );
      html.raw( TOKEN_CLOSE );
    }
  }
}
