/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors;

import com.keenwrite.processors.r.RBootstrapController;

public class RBootstrapProcessor extends ExecutorProcessor<String> {
  private final Processor<String> mSuccessor;
  private final ProcessorContext mContext;

  public RBootstrapProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    assert successor != null;
    assert context != null;

    mSuccessor = successor;
    mContext = context;
  }

  /**
   * Processes the given text document by replacing variables with their values.
   *
   * @param text The document text that includes variables that should be
   *             replaced with values when rendered as HTML.
   * @return The text with all variables replaced.
   */
  @Override
  public String apply( final String text ) {
    assert text != null;

    final var bootstrap = mContext.getRScript();
    final var workingDir = mContext.getRWorkingDir().toString();
    final var definitions = mContext.getDefinitions();

    RBootstrapController.update( bootstrap, workingDir, definitions );

    return mSuccessor.apply( text );
  }
}
