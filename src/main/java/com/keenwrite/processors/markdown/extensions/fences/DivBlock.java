/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

abstract class DivBlock extends Block {
  static final CharSequence HTML_DIV = "div";

  @Override
  @NotNull
  public BasedSequence[] getSegments() {
    return EMPTY_SEGMENTS;
  }

  /**
   * Append an opening or closing HTML div element to the given writer.
   *
   * @param html Builds the HTML document to be written.
   */
  abstract void write( HtmlWriter html );
}
