/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

import com.vladsch.flexmark.ast.DelimitedNodeImpl;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;

public class TexNode extends DelimitedNodeImpl {
  /**
   * TeX expression wrapped in a {@code <tex>} element.
   */
  public static final String HTML_TEX = "tex";

  public static final String TOKEN_OPEN = "$";
  public static final String TOKEN_CLOSE = "$";

  private final String mOpener;
  private final String mCloser;

  /**
   * Creates a new TeX node representation that can distinguish between '$'
   * and '$$' as opening/closing delimiters. The '$' is used for inline
   * TeX statements and '$$' is used for multi-line statements.
   *
   * @param opener The opening delimiter.
   * @param closer The closing delimiter.
   */
  public TexNode( final Delimiter opener, final Delimiter closer ) {
    mOpener = getDelimiter( opener );
    mCloser = getDelimiter( closer );
  }

  /**
   * @return Either '$' or '$$'.
   */
  public String getOpeningDelimiter() { return mOpener; }

  /**
   * @return Either '$' or '$$'.
   */
  public String getClosingDelimiter() { return mCloser; }

  private String getDelimiter( final Delimiter delimiter ) {
    return delimiter.getInput().subSequence(
      delimiter.getStartIndex(), delimiter.getEndIndex()
    ).toString();
  }
}
