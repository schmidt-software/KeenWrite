/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.keenwrite.processors.markdown.extensions.EmptyNode.EMPTY_NODE;

class BasedSequenceXrefParser extends BasedSequenceParser {
  private static final String REGEX = STR. "\\[@\{ REGEX_INNER }]" ;
  private static final Pattern PATTERN = asPattern( REGEX );

  private BasedSequenceXrefParser( final String text ) {
    super( text );
  }

  @Override
  Matcher createMatcher( final String text ) {
    return PATTERN.matcher( text );
  }

  static BasedSequenceXrefParser parse( final BasedSequence chars ) {
    return new BasedSequenceXrefParser( chars.toString() );
  }

  Node toNode() {
    final var typeName = getTypeName();
    final var idName = getIdName();

    return typeName == null || idName == null
      ? EMPTY_NODE
      : new AnchorXrefNode( typeName, idName );
  }
}
