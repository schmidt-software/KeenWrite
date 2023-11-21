/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.ast.DelimitedNodeImpl;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Responsible for writing HTML anchor names in the form
 * {@code <a data-type="..." name="name" />}, where {@code name} can be
 * referred to by a cross-reference.
 *
 * @see AnchorXrefNode
 */
class AnchorNameNode extends DelimitedNodeImpl implements CrossReferenceNode {
  private static final String REGEX_ANCHOR = "#(\\w+):(\\w+)";
  private static final Pattern PATTERN_ANCHOR = compile( REGEX_ANCHOR );

  private BasedSequence mOpeningMarker = BasedSequence.EMPTY;
  private BasedSequence mClosingMarker = BasedSequence.EMPTY;

  private String mTypeName = "";
  private String mIdName = "";

  public AnchorNameNode( final Delimiter opener, final Delimiter closer ) {}

  @Override
  public String getTypeName() {
    return mTypeName;
  }

  @Override
  public String getIdName() {
    return mIdName;
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
    final var matcher = PATTERN_ANCHOR.matcher( text.toString() );

    if( matcher.find() ) {
      mTypeName = matcher.group( 1 );
      mIdName = matcher.group( 2 );
    }
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
