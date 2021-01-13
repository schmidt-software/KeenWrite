/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.r;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

/**
 * The singleton is injected into the abstract syntax tree to mark an instance
 * of {@link Node} such that it must not be processed normally. Using a wrapper
 * for a given {@link Node} cannot work because the class type is used by
 * the parsing library for processing.
 */
public final class EmptyNode extends Node {
  public static final Node EMPTY_NODE = new EmptyNode();

  private static final BasedSequence[] BASE_SEQ = new BasedSequence[ 0 ];

  private EmptyNode() {
  }

  @Override
  public @NotNull BasedSequence[] getSegments() {
    return BASE_SEQ;
  }
}
