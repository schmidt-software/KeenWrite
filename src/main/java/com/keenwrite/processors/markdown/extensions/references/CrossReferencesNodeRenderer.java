/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Responsible for rendering HTML elements that correspond to cross-references.
 */
class CrossReferencesNodeRenderer implements NodeRenderer {
  @Override
  public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    return new HashSet<>( Arrays.asList(
      new NodeRenderingHandler<>( AnchorNameNode.class, this::render ),
      new NodeRenderingHandler<>( AnchorXrefNode.class, this::render )
    ) );
  }

  private void render(
    final CrossReferenceNode node,
    final NodeRendererContext context,
    final HtmlWriter html ) {
    node.write( html );
  }
}
