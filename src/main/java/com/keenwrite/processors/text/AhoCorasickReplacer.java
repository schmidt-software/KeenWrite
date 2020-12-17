/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.text;

import java.util.Map;

import static org.ahocorasick.trie.Trie.builder;

/**
 * Replaces text using an Aho-Corasick algorithm.
 */
public class AhoCorasickReplacer extends AbstractTextReplacer {

  /**
   * Default (empty) constructor.
   */
  protected AhoCorasickReplacer() {
  }

  @Override
  public String replace( final String text, final Map<String, String> map ) {
    // Create a buffer sufficiently large that re-allocations are minimized.
    final var sb = new StringBuilder( (int)(text.length() * 1.25) );

    // Definition names cannot overlap.
    final var builder = builder().ignoreOverlaps();
    builder.addKeywords( keys( map ) );

    int index = 0;

    // Replace all instances with dereferenced variables.
    for( final var emit : builder.build().parseText( text ) ) {
      sb.append( text, index, emit.getStart() );
      sb.append( map.get( emit.getKeyword() ) );
      index = emit.getEnd() + 1;
    }

    // Add the remainder of the string (contains no more matches).
    sb.append( text.substring( index ) );

    return sb.toString();
  }
}
