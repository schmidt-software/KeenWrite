/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for writing HTML anchor cross-references in the form
 * {@code <a data-type="..." href="#name" />} where {@code name} refers
 * to an anchor name.
 *
 * @see AnchorNameNode
 */
public class AnchorXrefNode extends Node implements CrossReferenceNode {
  private final String mTypeName;
  private final String mIdName;

  AnchorXrefNode( final String type, final String id ) {
    mTypeName = type;
    mIdName = STR. "#\{ id }" ;
  }

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
    return "href";
  }

  @NotNull
  @Override
  public BasedSequence[] getSegments() {
    return BasedSequence.EMPTY_SEGMENTS;
  }
}
