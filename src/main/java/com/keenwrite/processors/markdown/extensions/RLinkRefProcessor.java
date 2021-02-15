package com.keenwrite.processors.markdown.extensions;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.parser.LinkRefProcessor;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class RLinkRefProcessor implements LinkRefProcessor {
  private static final boolean WANT_EXCLAMATION_PREFIX = false;
  private static final int BRACKET_NESTING_LEVEL = 0;

  @SuppressWarnings( "unused" )
  public RLinkRefProcessor( final Document document ) {
  }

  @Override
  public boolean getWantExclamationPrefix() {
    System.out.println( "getWantExclamationPrefix()" );
    return WANT_EXCLAMATION_PREFIX;
  }

  @Override
  public int getBracketNestingLevel() {
    System.out.println( "getBracketNestingLevel()" );
    return BRACKET_NESTING_LEVEL;
  }

  @Override
  public boolean isMatch( @NotNull final BasedSequence nodeChars ) {
    System.out.println( "isMatch(BasedSequence)" );
    return nodeChars.length() >= 2;
  }

  @Override
  public @NotNull Node createNode( @NotNull final BasedSequence nodeChars ) {
    System.out.println( "createNode(BasedSequence)" );
    return new Link( nodeChars );
  }

  @Override
  public @NotNull BasedSequence adjustInlineText(
    @NotNull final Document document, @NotNull final Node node ) {
    System.out.println( "adjustInlineText(Document,Node)" );
    return node.getChars();
  }

  @Override
  public boolean allowDelimiters(
    @NotNull final BasedSequence chars,
    @NotNull final Document document,
    @NotNull final Node node ) {
    System.out.println( "allowDelimiters(BasedSequence,Document,Node)" );
    return true;
  }

  @Override
  public void updateNodeElements(
    @NotNull final Document document, @NotNull final Node node ) {
    System.out.println( "updateNodeElements(Document, Node)" );
  }
}
