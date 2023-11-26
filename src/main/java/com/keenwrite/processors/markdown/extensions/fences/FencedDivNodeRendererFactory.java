/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.keenwrite.processors.markdown.extensions.common.MarkdownNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.util.data.DataHolder;

class FencedDivNodeRendererFactory extends MarkdownNodeRendererFactory {
  @Override
  protected NodeRenderer createNodeRenderer( final DataHolder options ) {
    return new FencedDivRenderer();
  }
}
