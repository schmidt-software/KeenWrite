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
 * Responsible for rendering opening and closing fenced div blocks as HTML
 * {@code div} elements.
 */
class FencedDivRenderer implements NodeRenderer {
  @Nullable
  @Override
  public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    return Set.of(
      new NodeRenderingHandler<>( OpeningDivBlock.class, this::render ),
      new NodeRenderingHandler<>( ClosingDivBlock.class, this::render )
    );
  }

  /**
   * Renders the fenced div block as an HTML {@code <div></div>} element.
   */
  void render(
    final DivBlock node,
    final NodeRendererContext context,
    final HtmlWriter html ) {
    node.write( html );
  }

  static class Factory implements NodeRendererFactory {
    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return new FencedDivRenderer();
    }
  }
}
