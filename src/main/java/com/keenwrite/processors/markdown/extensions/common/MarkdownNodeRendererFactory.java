/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.common;

import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

public abstract class MarkdownNodeRendererFactory
  implements NodeRendererFactory {
  @NotNull
  @Override
  public NodeRenderer apply( @NotNull final DataHolder options ) {
    return createNodeRenderer( options );
  }

  protected abstract NodeRenderer createNodeRenderer( DataHolder options );
}
