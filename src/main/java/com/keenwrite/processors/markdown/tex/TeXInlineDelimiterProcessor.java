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
package com.keenwrite.processors.markdown.tex;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;

public class TeXInlineDelimiterProcessor implements DelimiterProcessor {

  @Override
  public void process( final Delimiter opener,
                       final Delimiter closer,
                       final int delimitersUsed ) {
    final var node = new TexNode();
    opener.moveNodesBetweenDelimitersTo( node, closer );
  }

  @Override
  public char getOpeningCharacter() {
    return '$';
  }

  @Override
  public char getClosingCharacter() {
    return '$';
  }

  @Override
  public int getMinLength() {
    return 1;
  }

  /**
   * Allow for $ or $$.
   *
   * @param opener One or more opening delimiter characters.
   * @param closer One or more closing delimiter characters.
   * @return The number of delimiters to use to determine whether a valid
   * opening delimiter expression is found.
   */
  @Override
  public int getDelimiterUse(
      final DelimiterRun opener, final DelimiterRun closer ) {
    return 1;
  }

  @Override
  public boolean canBeOpener( final String before,
                              final String after,
                              final boolean leftFlanking,
                              final boolean rightFlanking,
                              final boolean beforeIsPunctuation,
                              final boolean afterIsPunctuation,
                              final boolean beforeIsWhitespace,
                              final boolean afterIsWhiteSpace ) {
    return leftFlanking;
  }

  @Override
  public boolean canBeCloser( final String before,
                              final String after,
                              final boolean leftFlanking,
                              final boolean rightFlanking,
                              final boolean beforeIsPunctuation,
                              final boolean afterIsPunctuation,
                              final boolean beforeIsWhitespace,
                              final boolean afterIsWhiteSpace ) {
    return rightFlanking;
  }

  @Override
  public Node unmatchedDelimiterNode(
      final InlineParser inlineParser, final DelimiterRun delimiter ) {
    return null;
  }

  @Override
  public boolean skipNonOpenerCloser() {
    return false;
  }
}
