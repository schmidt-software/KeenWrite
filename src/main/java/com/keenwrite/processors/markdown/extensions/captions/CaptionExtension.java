/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.captions;

import com.keenwrite.processors.markdown.extensions.common.MarkdownExtension;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser.Builder;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Responsible for parsing and rendering {@link CaptionBlock} instances.
 */
public final class CaptionExtension extends MarkdownExtension {
  /**
   * Use {@link #create()}.
   */
  private CaptionExtension() {}

  /**
   * Returns a new {@link CaptionExtension}.
   *
   * @return An extension capable of parsing caption syntax.
   */
  public static CaptionExtension create() {
    return new CaptionExtension();
  }

  @Override
  public void extend( final Builder builder ) {
    builder.customBlockParserFactory( new CaptionBlockParserFactory() );
  }

  @Override
  protected NodeRendererFactory createNodeRendererFactory() {
    return new CaptionNodeRenderer.Factory();
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
