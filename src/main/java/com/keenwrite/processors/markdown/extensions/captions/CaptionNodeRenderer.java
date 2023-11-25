/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsible for rendering {@link CaptionBlock} instances as HTML (via
 * delegation).
 */
public class CaptionNodeRenderer extends CoreNodeRenderer {
  public CaptionNodeRenderer( final DataHolder options ) {
    super( options );
  }

  @Override
  public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    return new HashSet<>( List.of(
      new NodeRenderingHandler<>( CaptionBlock.class, this::render )
    ) );
  }

  private void render(
    final CaptionBlock node,
    final NodeRendererContext context,
    final HtmlWriter html ) {
    node.opening( html );

    if( node.hasChildren() ) {
      context.renderChildren( node );
    }

    node.closing( html );
  }

  static class Factory implements NodeRendererFactory {
    @NotNull
    @Override
    public NodeRenderer apply( @NotNull final DataHolder options ) {
      return new CaptionNodeRenderer( options );
    }
  }
}
