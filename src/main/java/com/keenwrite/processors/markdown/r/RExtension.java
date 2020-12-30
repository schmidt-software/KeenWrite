/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.r;

import com.keenwrite.processors.InlineRProcessor;
import com.keenwrite.sigils.RSigilOperator;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.InlineParserFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.internal.InlineParserImpl;
import com.vladsch.flexmark.parser.internal.LinkRefProcessorData;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Responsible for preventing the Markdown engine from interpreting inline
 * backticks as inline code elements. This is required so that inline R code
 * can be executed after conversion of Markdown to HTML but before the HTML
 * is previewed (or exported).
 */
public final class RExtension implements Parser.ParserExtension {
  private static final InlineParserFactory FACTORY = CustomParser::new;

  private RExtension() {
  }

  /**
   * Creates an extension capable of intercepting R code blocks and preventing
   * them from being converted into HTML {@code <code>} elements.
   */
  public static RExtension create() {
    return new RExtension();
  }

  @Override
  public void extend( final Parser.Builder builder ) {
    builder.customInlineParserFactory( FACTORY );
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {
  }

  /**
   * Prevents rendering {@code `r} statements as inline HTML {@code <code>}
   * blocks, which allows the {@link InlineRProcessor} to post-process the
   * text prior to display in the preview pane. This intervention assists
   * with decoupling the caret from the Markdown content so that the two
   * can vary independently in the architecture while permitting synchronization
   * of the editor and preview pane.
   * <p>
   * The text is therefore processed twice: once by flexmark-java and once by
   * {@link InlineRProcessor}.
   * </p>
   */
  private static class CustomParser extends InlineParserImpl {
    private CustomParser(
      final DataHolder options,
      final BitSet specialCharacters,
      final BitSet delimiterCharacters,
      final Map<Character, DelimiterProcessor> delimiterProcessors,
      final LinkRefProcessorData referenceLinkProcessors,
      final List<InlineParserExtensionFactory> inlineParserExtensions ) {
      super( options,
             specialCharacters,
             delimiterCharacters,
             delimiterProcessors,
             referenceLinkProcessors,
             inlineParserExtensions );
    }

    /**
     * The superclass handles a number backtick parsing edge cases; this method
     * changes the behaviour to retain R code snippets, identified by
     * {@link RSigilOperator#PREFIX}, so that subsequent processing can
     * invoke R. If other languages are added, the {@link CustomParser} will
     * have to be rewritten to identify more than merely R.
     *
     * @return The return value from {@link super#parseBackticks()}.
     * @inheritDoc
     */
    @Override
    protected final boolean parseBackticks() {
      final var foundTicks = super.parseBackticks();

      if( foundTicks ) {
        final var blockNode = getBlock();
        final var codeNode = blockNode.getLastChild();

        if( codeNode != null ) {
          final var code = codeNode.getChars();

          if( code.startsWith( RSigilOperator.PREFIX ) ) {
            codeNode.unlink();
            blockNode.appendChild( new Text( code ) );
          }
        }
      }

      return foundTicks;
    }
  }
}
