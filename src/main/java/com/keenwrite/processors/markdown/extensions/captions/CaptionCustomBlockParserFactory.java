/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.common.MarkdownCustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.util.data.DataHolder;

class CaptionCustomBlockParserFactory extends MarkdownCustomBlockParserFactory {
  CaptionCustomBlockParserFactory() {}

  @Override
  public BlockParserFactory createBlockParserFactory(
    final DataHolder options ) {
    return new CaptionBlockParserFactory( options );
  }
}
