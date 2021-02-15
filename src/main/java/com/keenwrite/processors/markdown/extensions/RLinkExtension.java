package com.keenwrite.processors.markdown.extensions;

import com.vladsch.flexmark.parser.LinkRefProcessor;
import com.vladsch.flexmark.parser.LinkRefProcessorFactory;
import com.vladsch.flexmark.parser.Parser.Builder;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

import static com.vladsch.flexmark.parser.Parser.ParserExtension;

public class RLinkExtension implements ParserExtension {
  private static final boolean WANT_EXCLAMATION_PREFIX = false;
  private static final int BRACKET_NESTING_LEVEL = 0;

  private final LinkRefProcessorFactory FACTORY = new Factory();

  public static RLinkExtension create() {
    System.out.println( "create()" );
    return new RLinkExtension();
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {
  }

  @Override
  public void extend( final Builder builder ) {
    System.out.println( "extend(Builder)" );
    builder.linkRefProcessorFactory( FACTORY );
  }

  public static class Factory implements LinkRefProcessorFactory {
    @NotNull
    @Override
    public LinkRefProcessor apply( @NotNull final Document document ) {
      return new RLinkRefProcessor( document );
    }

    @Override
    public boolean getWantExclamationPrefix(
      @NotNull final DataHolder options ) {
      System.out.println( "getWantExclamationPrefix(DataHolder)" );
      return WANT_EXCLAMATION_PREFIX;
    }

    @Override
    public int getBracketNestingLevel( @NotNull final DataHolder options ) {
      System.out.println( "getBracketNestingLevel(DataHolder)" );
      return BRACKET_NESTING_LEVEL;
    }
  }
}
