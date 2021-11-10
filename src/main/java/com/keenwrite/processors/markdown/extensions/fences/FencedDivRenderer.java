/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Responsible for rendering opening and closing fenced div blocks as HTMl
 * div elements.
 */
class FencedDivRenderer implements NodeRenderer {
  @Override
  public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    return Set.of(
      new NodeRenderingHandler<>( OpeningDivBlock.class, this::render ),
      new NodeRenderingHandler<>( ClosingDivBlock.class, this::render )
    );
  }

  /**
   * Renders the opening fenced div block as an HTML {@code <div>} element.
   */
  void render( final OpeningDivBlock node,
               final NodeRendererContext context,
               final HtmlWriter html ) {
    node.export( html );
  }

  /**
   * Renders the closing fenced div block as an HTML {@code </div>} element.
   */
  void render( final ClosingDivBlock node,
               final NodeRendererContext context,
               final HtmlWriter html ) {
    node.export( html );
  }

  static class Factory implements NodeRendererFactory {
    @Override
    public @NotNull NodeRenderer apply( @NotNull final DataHolder options ) {
      return new FencedDivRenderer();
    }
  }
}
