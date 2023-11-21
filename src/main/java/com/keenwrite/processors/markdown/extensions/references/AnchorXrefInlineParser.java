/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.parser.InlineParserExtensionFactory;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.internal.InlineParserImpl;
import com.vladsch.flexmark.parser.internal.LinkRefProcessorData;
import com.vladsch.flexmark.util.data.DataHolder;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

final class AnchorXrefInlineParser extends InlineParserImpl {

  private static final String REGEX_REFERENCE = "@(\\w+):(\\w+)";
  private static final Pattern PATTERN_REFERENCE = compile( REGEX_REFERENCE );

  private boolean mFoundReference;

  AnchorXrefInlineParser(
    final DataHolder options,
    final BitSet specialCharacters,
    final BitSet delimiterCharacters,
    final Map<Character, DelimiterProcessor> delimiterProcessors,
    final LinkRefProcessorData referenceLinkProcessors,
    final List<InlineParserExtensionFactory> inlineParserExtensions ) {
    super(
      options,
      specialCharacters,
      delimiterCharacters,
      delimiterProcessors,
      referenceLinkProcessors,
      inlineParserExtensions
    );
  }

  @Override
  public boolean parseOpenBracket() {
    mFoundReference = peek( 1 ) == '@';

    return super.parseOpenBracket();
  }

  @Override
  public boolean parseCloseBracket() {
    final boolean foundBracket = super.parseCloseBracket();

    if( mFoundReference ) {
      final var blockNode = getBlock();
      final var linkRef = blockNode.getLastChild();

      if( linkRef != null ) {
        final var text = linkRef.getChildChars();
        final var matcher = PATTERN_REFERENCE.matcher( text );

        if( matcher.find() ) {
          final var typeName = matcher.group( 1 );
          final var idName = matcher.group( 2 );
          final var xref = new AnchorXrefNode( typeName, idName );

          linkRef.unlink();
          blockNode.appendChild( xref );
        }
      }
    }

    return foundBracket;
  }
}
