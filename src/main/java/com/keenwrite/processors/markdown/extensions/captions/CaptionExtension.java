/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.common.MarkdownExtension;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser.Builder;

/**
 * Responsible for parsing and rendering {@link CaptionBlock} instances.
 */
public final class CaptionExtension extends MarkdownExtension {
  /**
   * Use {@link #create()}.
   */
  private CaptionExtension() {}

  /**
   * Returns a new {@link CaptionExtension}.
   *
   * @return An extension capable of parsing caption syntax.
   */
  public static CaptionExtension create() {
    return new CaptionExtension();
  }

  @Override
  public void extend( final Builder builder ) {
    builder.customBlockParserFactory( new CaptionCustomBlockParserFactory() );
  }

  @Override
  protected NodeRendererFactory createNodeRendererFactory() {
    return new CaptionNodeRendererFactory();
  }
}
