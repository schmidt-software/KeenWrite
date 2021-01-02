/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.tex;

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
