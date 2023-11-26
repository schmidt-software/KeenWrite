/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.common;

import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import org.jetbrains.annotations.NotNull;

public abstract class MarkdownPostProcessorFactory
  extends NodePostProcessorFactory {
  public MarkdownPostProcessorFactory( final boolean ignored ) {
    super( ignored );
  }

  @NotNull
  @Override
  public NodePostProcessor apply( @NotNull Document document ) {
    return createPostProcessor( document );
  }

  protected abstract NodePostProcessor createPostProcessor( Document document );
}
