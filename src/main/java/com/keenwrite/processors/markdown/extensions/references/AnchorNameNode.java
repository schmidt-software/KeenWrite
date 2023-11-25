/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.ast.DelimitedNodeImpl;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for writing HTML anchor names in the form
 * {@code <a data-type="..." name="name" />}, where {@code name} can be
 * referred to by a cross-reference.
 *
 * @see AnchorXrefNode
 */
public class AnchorNameNode extends DelimitedNodeImpl implements CrossReferenceNode {

  private BasedSequence mOpeningMarker = BasedSequence.EMPTY;
  private BasedSequence mClosingMarker = BasedSequence.EMPTY;

  private BasedSequenceNameParser mParser;

  public AnchorNameNode() {}

  @Override
  public String getTypeName() {
    return mParser.getTypeName();
  }

  @Override
  public String getIdName() {
    return mParser.getIdName();
  }

  @Override
  public String getRefAttrName() {
    return "name";
  }

  @Override
  public BasedSequence getOpeningMarker() {
    return mOpeningMarker;
  }

  @NotNull
  @Override
  public BasedSequence getChars() {
    return BasedSequence.EMPTY;
  }

  @Override
  public void setOpeningMarker( final BasedSequence openingMarker ) {
    mOpeningMarker = openingMarker;
  }

  @Override
  public BasedSequence getText() {
    return BasedSequence.EMPTY;
  }

  @Override
  public void setText( final BasedSequence text ) {
    mParser = BasedSequenceNameParser.parse( text );
  }

  @Override
  public BasedSequence getClosingMarker() {
    return mClosingMarker;
  }

  @Override
  public void setClosingMarker( final BasedSequence closingMarker ) {
    mClosingMarker = closingMarker;
  }
}
