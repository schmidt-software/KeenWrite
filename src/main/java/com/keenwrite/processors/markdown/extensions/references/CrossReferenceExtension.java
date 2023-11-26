/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.keenwrite.processors.markdown.extensions.common.MarkdownExtension;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser.Builder;

/**
 * Responsible for processing {@code {@type:id}} anchors and their corresponding
 * {@code [@type:id]} cross-references.
 */
public final class CrossReferenceExtension extends MarkdownExtension {
  /**
   * Use {@link #create()}.
   */
  private CrossReferenceExtension() {}

  /**
   * Returns a new {@link CrossReferenceExtension}.
   *
   * @return An extension capable of parsing cross-reference syntax.
   */
  public static CrossReferenceExtension create() {
    return new CrossReferenceExtension();
  }

  @Override
  public void extend( final Builder builder ) {
    builder.linkRefProcessorFactory( new AnchorXrefProcessorFactory() );
    builder.customDelimiterProcessor( new AnchorNameDelimiterProcessor() );
  }

  @Override
  protected NodeRendererFactory createNodeRendererFactory() {
    return new CrossReferencesNodeRenderer.Factory();
  }
}
