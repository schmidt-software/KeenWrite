/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.util.data.DataHolder;

class CaptionBlockParserFactory extends AbstractBlockParserFactory {
  CaptionBlockParserFactory( final DataHolder options ) {
    super( options );
  }

  @Override
  public BlockStart tryStart(
    final ParserState state,
    final MatchedBlockParser matchedBlockParser ) {

    final var flush = state.getIndent() == 0;
    final var index = state.getNextNonSpaceIndex();
    final var line = state.getLine();
    final var length = line.length();
    final var text = line.subSequence( index, length );

    return flush && CaptionParser.canParse( text )
      ? BlockStart.of( new CaptionParser( text ) ).atIndex( length )
      : BlockStart.none();
  }
}
