/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.references.CrossReferenceNode;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.CoreNodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Responsible for rendering {@link CaptionBlock} instances as HTML (via
 * delegation).
 */
class CaptionNodeRenderer extends CoreNodeRenderer {
  CaptionNodeRenderer( final DataHolder options ) {
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
    final var anchors = new LinkedList<Node>();

    html.raw( "<p>" );
    node.opening( html );

    if( node.hasChildren() ) {
      for( final var child : node.getChildren() ) {
        if( !child.isOrDescendantOfType( CrossReferenceNode.class ) ) {
          context.render( child );
        }
        else {
          anchors.add( child );
        }
      }
    }

    node.closing( html );

    for( final var anchor : anchors ) {
      context.render( anchor );
    }

    html.raw( "</p>" );
  }
}
