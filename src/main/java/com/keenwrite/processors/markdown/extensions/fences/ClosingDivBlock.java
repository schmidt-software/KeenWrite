/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.vladsch.flexmark.html.HtmlWriter;

/**
 * Responsible for helping to generate a closing div element.
 */
class ClosingDivBlock extends DivBlock {
  @Override
  void write( final HtmlWriter html ) {
    html.closeTag( HTML_DIV );
  }
}
