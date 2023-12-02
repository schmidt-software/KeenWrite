/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for retaining the text node and all child nodes with respect
 * to a caption. The caption can be associated with most items, such as
 * block quotes, tables, math expressions, and images.
 */
class CaptionBlock extends Block {
  private final BasedSequence mCaption;

  CaptionBlock( final BasedSequence caption ) {
    assert caption != null;

    mCaption = caption;
  }

  /**
   * Opens the caption.
   *
   * @param writer Where to write the opening tags.
   */
  void opening( final HtmlWriter writer ) {
    writer.raw( "<span class=\"caption\">" );
  }

  /**
   * Closes the caption.
   *
   * @param writer Where to write the closing tags.
   */
  void closing( final HtmlWriter writer ) {
    writer.raw( "</span>" );
  }

  void parse( final InlineParser inlineParser ) {
    assert inlineParser != null;

    inlineParser.parse( mCaption, this );
  }

  @NotNull
  @Override
  public BasedSequence[] getSegments() {
    return BasedSequence.EMPTY_SEGMENTS;
  }
}
