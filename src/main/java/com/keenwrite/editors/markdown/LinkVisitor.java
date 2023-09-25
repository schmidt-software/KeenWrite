/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.editors.markdown;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;

/**
 * Responsible for extracting a hyperlink from the document so that the user
 * can edit the link within a dialog.
 */
public final class LinkVisitor {

  private NodeVisitor mVisitor;
  private Link mLink;
  private final int mOffset;

  /**
   * Creates a hyperlink given an offset into a paragraph and the Markdown AST
   * link node.
   *
   * @param index Index into the paragraph that indicates the hyperlink to
   *              change.
   */
  public LinkVisitor( final int index ) {
    mOffset = index;
  }

  public Link process( final Node root ) {
    getVisitor().visit( root );
    return getLink();
  }

  /**
   * @param link Not null.
   */
  private void visit( final Link link ) {
    final int began = link.getStartOffset();
    final int ended = link.getEndOffset();
    final int index = getOffset();

    if( index >= began && index <= ended ) {
      setLink( link );
    }
  }

  private synchronized NodeVisitor getVisitor() {
    if( mVisitor == null ) {
      mVisitor = createVisitor();
    }

    return mVisitor;
  }

  protected NodeVisitor createVisitor() {
    return new NodeVisitor(
      new VisitHandler<>( Link.class, LinkVisitor.this::visit ) );
  }

  private Link getLink() {
    return mLink;
  }

  private void setLink( final Link link ) {
    mLink = link;
  }

  public int getOffset() {
    return mOffset;
  }
}
