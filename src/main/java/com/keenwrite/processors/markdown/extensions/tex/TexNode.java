/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

import com.vladsch.flexmark.ast.DelimitedNodeImpl;

public class TexNode extends DelimitedNodeImpl {
  /**
   * TeX expression wrapped in a {@code <tex>} element.
   */
  public static final String HTML_TEX = "tex";

  public static final String TOKEN_OPEN = "$";
  public static final String TOKEN_CLOSE = "$";

  public TexNode() {
  }
}
