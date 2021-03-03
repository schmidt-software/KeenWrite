/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.fences;

import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.block.*;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.html.AttributeImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static com.vladsch.flexmark.parser.Parser.ParserExtension;

/**
 * Responsible for parsing div block syntax into HTML div tags. Fenced div
 * blocks start with three or more consecutive colons, followed by a space,
 * followed by attributes. The attributes can be either a single word, or
 * multiple words nested in braces. For example:
 *
 * <p>
 * ::: poem
 * Tyger Tyger, burning bright,
 * In the forests of the night;
 * What immortal hand or eye,
 * Could frame thy fearful symmetry?
 * :::
 * </p>
 * <p>
 * As well as:
 * </p>
 * <p>
 * ::: {#verse .poem .dark k=v author="Emily Dickinson" type='slant rhyme'}
 * Because I could not stop for Death --
 * He kindly stopped for me --
 * The Carriage held but just Ourselves --
 * And Immortality.
 * :::
 * </p>
 *
 * <p>
 * The second example produces the following starting {@code div} element:
 * </p>
 * <p>
 * &lt;div id="verse" class="poem dark" data-k="v" data-author="Emily Dickson"
 * data-type="slant rhyme"&gt;
 * </p>
 */
public class FencedDivExtension extends HtmlRendererAdapter
  implements ParserExtension {
  /**
   * Matches any number of colons at start of line. This will match both the
   * opening and closing fences, with any number of colons.
   */
  private static final Pattern FENCE = Pattern.compile( "^:::.*" );

  /**
   * After a fenced div is detected, this will match the opening fence.
   */
  private static final Pattern FENCE_OPENING = Pattern.compile(
    "^:::+\\s+(\\w[\\w-]*|\\{.+})\\s*$" );

  private static final Pattern ATTR_MULTIPLE = Pattern.compile( "\\{(.+)}" );

  // TODO: HtmlRendererAdapter?
  public static FencedDivExtension create() {
    return new FencedDivExtension();
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.customBlockParserFactory( new Factory() );
  }

  /**
   * Creates a renderer that can generate HTML div elements.
   *
   * @param builder      The document builder.
   * @param rendererType Indicates the document type to be built.
   */
  @Override
  public void extend( @NotNull final HtmlRenderer.Builder builder,
                      @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new FencedDivRenderer.Factory() );
    }
  }

  /**
   * Responsible for creating an instance of {@link ParserFactory}.
   */
  private static class Factory implements CustomBlockParserFactory {
    @Override
    public @NotNull BlockParserFactory apply(
      @NotNull final DataHolder options ) {
      return new ParserFactory( options );
    }

    @Override
    public @Nullable Set<Class<?>> getAfterDependents() { return null; }

    @Override
    public @Nullable Set<Class<?>> getBeforeDependents() { return null; }

    @Override
    public boolean affectsGlobalScope() { return false; }
  }

  /**
   * Responsible for creating a fenced div parser that is appropriate for the
   * type of fenced div encountered: opening or closing.
   */
  private static class ParserFactory extends AbstractBlockParserFactory {
    public ParserFactory( final DataHolder options ) {
      super( options );
    }

    /**
     * Try to match an opening or closing fenced div.
     *
     * @param state              Block parser state.
     * @param matchedBlockParser Last matched open block parser.
     * @return Wrapper for the opening or closing parser, upon finding :::.
     */
    @Override
    public BlockStart tryStart(
      final ParserState state, final MatchedBlockParser matchedBlockParser ) {
      return
        state.getIndent() == 0 && FENCE.matcher( state.getLine() ).matches()
          ? parseFence( state )
          : BlockStart.none();
    }

    /**
     * After finding a fenced div, this will further disambiguate an opening
     * from a closing fence.
     *
     * @param state Block parser state, contains line to parse.
     * @return Wrapper for the opening or closing parser, upon finding :::.
     */
    private BlockStart parseFence( final ParserState state ) {
      final var fence = FENCE_OPENING.matcher( state.getLine() );

      return BlockStart.of(
        fence.matches()
          ? new OpeningParser( fence.group( 1 ) )
          : new ClosingParser()
      ).atIndex( state.getIndex() );
    }
  }

  /**
   * Abstracts common {@link OpeningParser} and {@link ClosingParser} methods.
   */
  private static abstract class DivBlockParser extends AbstractBlockParser {
    @Override
    public BlockContinue tryContinue( final ParserState state ) {
      return BlockContinue.none();
    }

    @Override
    public void closeBlock( final ParserState state ) {}
  }

  /**
   * Responsible for creating an instance of {@link OpeningDivBlock}.
   */
  private static class OpeningParser extends DivBlockParser {
    private final OpeningDivBlock mBlock;

    /**
     * Parses the arguments upon construction.
     *
     * @param args Text after :::, excluding leading/trailing whitespace.
     */
    public OpeningParser( final String args ) {
      final var attrs = new ArrayList<Attribute>();
      final var matcher = ATTR_MULTIPLE.matcher( args );

      if( matcher.matches() ) {
        // Split on whitespace (tabs, spaces, etc.)
        final var st = new StringTokenizer( matcher.group( 1 ) );
        String name;

        while( st.hasMoreTokens() ) {
          final var token = st.nextToken();
          int index = 1;

          if( token.startsWith( "#" ) ) {
            name = "id";
          }
          else if( token.startsWith( "." ) ) {
            name = "class";
          }
          else if( !token.contains( "=" ) ) {
            name = token;
            index = 0;
          }
          else {
            name = "data";
          }

          attrs.add( AttributeImpl.of( name, token.substring( index ) ) );
        }
      }
      else {
        attrs.add( AttributeImpl.of( "class", args ) );
      }

      mBlock = new OpeningDivBlock( attrs );
    }

    @Override
    public Block getBlock() {
      return mBlock;
    }
  }

  /**
   * Responsible for creating an instance of {@link ClosingDivBlock}.
   */
  private static class ClosingParser extends DivBlockParser {
    private final ClosingDivBlock mBlock = new ClosingDivBlock();

    @Override
    public Block getBlock() {
      return mBlock;
    }
  }
}
