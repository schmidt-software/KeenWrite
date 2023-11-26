/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.common;

import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;

/**
 * Hides the {@link #rendererOptions(MutableDataHolder)} from subclasses
 * that would otherwise implement the {@link HtmlRendererExtension} interface.
 */
public abstract class HtmlRendererAdapter implements HtmlRendererExtension {
  /**
   * Empty, unused.
   *
   * @param options Ignored.
   */
  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) { }
}
