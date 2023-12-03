/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;

import static com.keenwrite.constants.Constants.DEF_DELIM_BEGAN_DEFAULT;

/**
 * Responsible for processing {@code {@type:id}} anchors.
 */
class AnchorNameDelimiterProcessor implements DelimiterProcessor {

  @Override
  public void process(
    final Delimiter opener,
    final Delimiter closer,
    final int delimitersUsed ) {
    final var node = new AnchorNameNode();
    opener.moveNodesBetweenDelimitersTo( node, closer );
  }

  @Override
  public char getOpeningCharacter() {
    return '{';
  }

  @Override
  public char getClosingCharacter() {
    return '}';
  }

  @Override
  public int getMinLength() {
    return 1;
  }

  @Override
  public int getDelimiterUse(
    final DelimiterRun opener,
    final DelimiterRun closer ) {
    final var text = opener.getNode();

    // Ensure that the default delimiters are respected (not clobbered by
    // transforming them into anchor links).
    return text.getChars().toString().equals( DEF_DELIM_BEGAN_DEFAULT ) ? 0 : 1;
  }

  @Override
  public Node unmatchedDelimiterNode(
    final InlineParser inlineParser,
    final DelimiterRun delimiter ) {
    return null;
  }

  @Override
  public boolean canBeOpener(
    final String before,
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
  public boolean canBeCloser(
    final String before,
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
  public boolean skipNonOpenerCloser() {
    return false;
  }
}
