/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;

import java.util.function.Function;

import static com.keenwrite.processors.IdentityProcessor.IDENTITY;

/**
 * Responsible for processing R statements within a text block.
 */
public final class RProcessor
  extends ExecutorProcessor<String> implements Function<String, String> {
  private final Processor<String> mProcessor;
  private final InlineRProcessor mInlineRProcessor;

  public RProcessor( final ProcessorContext context ) {
    final var irp = new InlineRProcessor( IDENTITY, context );
    final var rvp = new RVariableProcessor( irp, context );
    mProcessor = new ExecutorProcessor<>( rvp );
    mInlineRProcessor = irp;
  }

  public String apply( final String text ) {
    if( !mInlineRProcessor.isReady() ) {
      mInlineRProcessor.init();
    }

    return mProcessor.apply( text );
  }

  public boolean isReady() {
    return mInlineRProcessor.isReady();
  }
}
