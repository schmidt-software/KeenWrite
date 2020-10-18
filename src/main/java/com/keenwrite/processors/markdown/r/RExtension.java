/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.processors.markdown.r;

import com.keenwrite.processors.InlineRProcessor;
import com.keenwrite.sigils.RSigilOperator;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.InlineParserFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.internal.CommonmarkInlineParser;
import com.vladsch.flexmark.parser.internal.LinkRefProcessorData;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

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
  private final static InlineParserFactory R_INLINE_PARSER_FACTORY =
      RInlineParser::new;

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
  private static class RInlineParser extends CommonmarkInlineParser {
    private RInlineParser(
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
     * invoke R. If other languages are added, this {@link RInlineParser} will
     * have to be rewritten to identify more than merely R.
     *
     * @return The return value from {@link super#parseBackticks()}.
     * @inheritDoc
     */
    @Override
    protected final boolean parseBackticks() {
      final var foundCode = super.parseBackticks();

      if( foundCode ) {
        final var block = getBlock();
        final var codeNode = block.getLastChild();
        final var code = codeNode == null
            ? BasedSequence.of( "" )
            : codeNode.getChars();

        if( code.startsWith( RSigilOperator.PREFIX ) ) {
          assert codeNode != null;
          codeNode.unlink();
          block.appendChild( new Text( code ) );
        }
      }

      return foundCode;
    }
  }

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
    builder.customInlineParserFactory( R_INLINE_PARSER_FACTORY );
  }

  @Override
  public void parserOptions( final MutableDataHolder options ) {
  }
}
