/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.r;

import com.keenwrite.processors.InlineRProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.RVariableProcessor;
import com.keenwrite.sigils.RSigilOperator;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.InlineParserFactory;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.internal.InlineParserImpl;
import com.vladsch.flexmark.parser.internal.LinkRefProcessorData;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static com.keenwrite.processors.IdentityProcessor.IDENTITY;
import static com.vladsch.flexmark.parser.Parser.Builder;
import static com.vladsch.flexmark.parser.Parser.ParserExtension;

/**
 * Responsible for processing inline R statements (denoted using the
 * {@link RSigilOperator#PREFIX}) to prevent them from being converted to
 * HTML {@code <code>} elements and stop them from interfering with TeX
 * statements. Note that TeX statements are processed using a Markdown
 * extension, rather than an implementation of {@link Processor}. For this
 * reason, some pre-conversion is necessary.
 */
public final class RExtension implements ParserExtension {
  private final InlineParserFactory FACTORY = CustomParser::new;

  private final Processor<String> mProcessor;

  private RExtension( final Processor<String> processor ) {
    mProcessor = processor;
  }

  /**
   * Creates an extension capable of intercepting R code blocks and preventing
   * them from being converted into HTML {@code <code>} elements.
   */
  public static RExtension create( final ProcessorContext context ) {
    final var irp = new InlineRProcessor( IDENTITY, context );
    final var rvp = new RVariableProcessor( irp, context );
    return new RExtension( rvp );
  }

  @Override
  public void extend( final Builder builder ) {
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
  private class CustomParser extends InlineParserImpl {
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
          final var code = codeNode.getChars().toString();

          if( code.startsWith( RSigilOperator.PREFIX ) ) {
            codeNode.unlink();
            blockNode.appendChild( new Text( mProcessor.apply( code ) ) );
          }
        }
      }

      return foundTicks;
    }
  }
}
