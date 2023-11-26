/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.common;

import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser.ParserExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

public abstract class MarkdownExtension extends HtmlRendererAdapter
  implements ParserExtension {

  /**
   * Implemented by subclasses to create the {@link NodeRendererFactory} capable
   * of converting nodes created by an extension into HTML elements.
   *
   * @return The {@link NodeRendererFactory} for producing {@link NodeRenderer}
   * instances.
   */
  protected abstract NodeRendererFactory createNodeRendererFactory();

  /**
   * Adds an extension for HTML document export types.
   *
   * @param builder      The document builder.
   * @param rendererType Indicates the document type to be built.
   */
  @Override
  public void extend(
    @NotNull final Builder builder,
    @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( createNodeRendererFactory() );
    }
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {}
}
