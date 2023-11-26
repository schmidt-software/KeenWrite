/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.keenwrite.processors.markdown.extensions.common.MarkdownNodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.util.data.DataHolder;

class CrossReferencesNodeRendererFactory extends MarkdownNodeRendererFactory {
  @Override
  protected NodeRenderer createNodeRenderer( final DataHolder options ) {
    return new CrossReferencesNodeRenderer();
  }
}
