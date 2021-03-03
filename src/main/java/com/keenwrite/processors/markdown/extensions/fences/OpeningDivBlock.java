/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.util.html.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for helping to generate an opening div element.
 */
class OpeningDivBlock extends DivBlock {
  private final List<Attribute> mAttributes = new ArrayList<>();

  OpeningDivBlock( final List<Attribute> attributes ) {
    assert attributes != null;
    mAttributes.addAll( attributes );
  }

  void export( final HtmlWriter html ) {
    mAttributes.forEach( html::attr );
    html.withAttr().tag( HTML_DIV );
  }
}
