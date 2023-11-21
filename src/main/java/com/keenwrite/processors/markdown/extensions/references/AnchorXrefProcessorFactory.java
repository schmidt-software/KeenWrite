/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.parser.LinkRefProcessor;
import com.vladsch.flexmark.parser.LinkRefProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for processing {@code [@type:id]} anchors.
 */
class AnchorXrefProcessorFactory implements LinkRefProcessorFactory {
  private final LinkRefProcessor mProcessor = new AnchorLinkRefProcessor();

  @Override
  public boolean getWantExclamationPrefix( @NotNull final DataHolder options ) {
    return false;
  }

  @Override
  public int getBracketNestingLevel( @NotNull final DataHolder options ) {
    return 0;
  }

  @NotNull
  @Override
  public LinkRefProcessor apply( @NotNull final Document document ) {
    return mProcessor;
  }

  private static class AnchorLinkRefProcessor implements LinkRefProcessor {

    @Override
    public boolean getWantExclamationPrefix() {
      return false;
    }

    @Override
    public int getBracketNestingLevel() {
      return 0;
    }

    @Override
    public boolean isMatch( @NotNull final BasedSequence nodeChars ) {
      return nodeChars.indexOf( '@' ) == 1;
    }

    @NotNull
    @Override
    public Node createNode( @NotNull final BasedSequence nodeChars ) {
      return BasedSequenceXrefParser.parse( nodeChars ).toNode();
    }

    @NotNull
    @Override
    public BasedSequence adjustInlineText(
      @NotNull final Document document,
      @NotNull final Node node ) {
      return BasedSequence.EMPTY;
    }

    @Override
    public boolean allowDelimiters(
      @NotNull final BasedSequence chars,
      @NotNull final Document document,
      @NotNull final Node node ) {
      return false;
    }

    @Override
    public void updateNodeElements(
      @NotNull final Document document,
      @NotNull final Node node ) {}
  }
}
