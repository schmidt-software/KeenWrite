package com.keenwrite.processors.markdown.extensions.captions;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.sequence.BasedSequence;

class CaptionParser extends AbstractBlockParser {
  private final CaptionBlock mBlock;

  CaptionParser( final BasedSequence text ) {
    assert text != null;
    assert text.isNotEmpty();
    assert text.length() > 2;

    final var caption = text.subSequence( 2 );

    mBlock = new CaptionBlock( caption.trim() );
  }

  static boolean canParse( final BasedSequence text ) {
    return text.length() > 3 &&
           text.charAt( 0 ) == ':' &&
           text.charAt( 1 ) == ':' &&
           text.charAt( 2 ) != ':';
  }

  @Override
  public Block getBlock() {
    return mBlock;
  }

  @Override
  public BlockContinue tryContinue( final ParserState state ) {
    return BlockContinue.none();
  }

  @Override
  public void parseInlines( final InlineParser inlineParser ) {
    assert inlineParser != null;

    mBlock.parse( inlineParser );
  }

  @Override
  public void closeBlock( final ParserState state ) {}
}
