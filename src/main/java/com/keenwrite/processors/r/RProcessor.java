/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;

import static com.keenwrite.processors.IdentityProcessor.IDENTITY;

/**
 * Responsible for processing R statements within a text block.
 */
public class RProcessor extends ExecutorProcessor<String> {
  private final Processor<String> mProcessor;
  private final InlineRProcessor mInlineRProcessor;
  private volatile boolean mReady;

  public RProcessor( final ProcessorContext context ) {
    final var irp = new InlineRProcessor( IDENTITY, context );
    final var rvp = new RVariableProcessor( irp, context );
    mProcessor = new ExecutorProcessor<>( rvp );
    mInlineRProcessor = irp;
  }

  public void init() {
    mReady = mInlineRProcessor.init();
  }

  public boolean isReady() {
    return mReady;
  }

  public String apply( final String text ) {
    return mProcessor.apply( text );
  }
}
