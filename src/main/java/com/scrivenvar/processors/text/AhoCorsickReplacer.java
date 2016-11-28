/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.processors.text;

import java.util.Map;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

/**
 * Replaces text using an Aho-Corsick algorithm.
 *
 * @author White Magic Software, Ltd.
 */
public class AhoCorsickReplacer extends AbstractTextReplacer {

  /**
   * Default (empty) constructor.
   */
  protected AhoCorsickReplacer() {
  }

  @Override
  public String replace( final String text, final Map<String, String> map ) {
    // Create a buffer sufficiently large that re-allocations are minimized.
    final StringBuilder sb = new StringBuilder( (int)(text.length() * 1.25) );

    // The TrieBuilder should only match whole words and ignore overlaps (there
    // shouldn't be any).
    final Trie.TrieBuilder builder = Trie.builder();
    builder.onlyWholeWords();
    builder.removeOverlaps();

    for( final String key : keys( map ) ) {
      builder.addKeyword( key );
    }

    int index = 0;

    for( final Emit emit : builder.build().parseText( text ) ) {
      sb.append( text.substring( index, emit.getStart() ) );
      sb.append( map.get( emit.getKeyword() ) );
      index = emit.getEnd() + 1;
    }

    // Add the remainder of the string (contains no more matches).
    sb.append( text.substring( index ) );

    return sb.toString();
  }
}
