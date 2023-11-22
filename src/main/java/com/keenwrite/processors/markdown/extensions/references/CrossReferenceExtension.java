/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.Parser.ParserExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for processing {@code {@type:id}} anchors and their corresponding
 * {@code [@type:id]} cross-references.
 */
public class CrossReferenceExtension extends HtmlRendererAdapter
  implements ParserExtension {

  /**
   * Returns a new instance of {@link CrossReferenceExtension}. This is here
   * for consistency with the other extensions.
   *
   * @return A new {@link CrossReferenceExtension} instance.
   */
  public static CrossReferenceExtension create() {
    return new CrossReferenceExtension();
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.linkRefProcessorFactory( new AnchorXrefProcessorFactory() );
    builder.customDelimiterProcessor( new AnchorNameDelimiterProcessor() );
  }

  @Override
  public void extend( @NotNull final HtmlRenderer.Builder builder,
                      @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new CrossReferencesNodeRenderer.Factory() );
    }
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {}
}
