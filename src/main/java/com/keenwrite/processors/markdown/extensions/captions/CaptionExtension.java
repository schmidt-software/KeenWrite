/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.Parser.ParserExtension;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Responsible for parsing and rendering {@link CaptionBlock} instances.
 */
public class CaptionExtension extends HtmlRendererAdapter
  implements ParserExtension {

  /**
   * @see #create()
   */
  private CaptionExtension() {}

  /**
   * Returns an instance of extension that can be added to the
   * {@link Parser.Builder} and {@link HtmlRenderer.Builder} extensions.
   *
   * @return An extension capable of parsing caption syntax.
   */
  public static CaptionExtension create() {
    return new CaptionExtension();
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.customBlockParserFactory( new CaptionBlockParserFactory() );
  }

  @Override
  public void extend(
    @NotNull final HtmlRenderer.Builder builder,
    @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new CaptionNodeRenderer.Factory() );
    }
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {}

  private static class CaptionParser extends AbstractBlockParser {
    private final CaptionBlock mBlock;

    private CaptionParser( final BasedSequence text ) {
      assert text != null;
      assert text.isNotEmpty();
      assert text.length() > 2;

      final var caption = text.subSequence( 2 );

      mBlock = new CaptionBlock( caption.trim() );
    }

    private static boolean canParse( final BasedSequence text ) {
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

  private static class CaptionBlockParserFactory
    implements CustomBlockParserFactory {

    private CaptionBlockParserFactory() {}

    @NotNull
    @Override
    public BlockParserFactory apply( @NotNull final DataHolder options ) {
      return new CaptionParserFactory( options );
    }

    @Nullable
    @Override
    public Set<Class<?>> getAfterDependents() {
      return null;
    }

    @Nullable
    @Override
    public Set<Class<?>> getBeforeDependents() {
      return null;
    }

    @Override
    public boolean affectsGlobalScope() {
      return false;
    }
  }

  private static class CaptionParserFactory extends AbstractBlockParserFactory {
    private CaptionParserFactory( final DataHolder options ) {
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
}
