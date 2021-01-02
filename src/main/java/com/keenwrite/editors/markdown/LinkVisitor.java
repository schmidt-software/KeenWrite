/*
 * Copyright 2020-2021 White Magic Software, Ltd.
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
package com.keenwrite.editors.markdown;

import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;

/**
 * Responsible for extracting a hyperlink from the document so that the user
 * can edit the link within a dialog.
 */
public class LinkVisitor {

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
