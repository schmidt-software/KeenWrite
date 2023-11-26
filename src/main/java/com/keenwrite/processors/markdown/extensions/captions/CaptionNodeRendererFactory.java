/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;

class CaptionNodeRendererFactory implements NodeRendererFactory {
  @NotNull
  @Override
  public NodeRenderer apply( @NotNull final DataHolder options ) {
    return new CaptionNodeRenderer( options );
  }
}
