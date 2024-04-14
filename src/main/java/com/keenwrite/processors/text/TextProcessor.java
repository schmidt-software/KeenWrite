/* Copyright 2024 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.text;

import com.keenwrite.io.MediaType;
import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.r.RInlineEvaluator;
import com.keenwrite.processors.variable.RVariableProcessor;
import com.keenwrite.processors.variable.VariableProcessor;

import java.util.function.Function;

import static com.keenwrite.io.MediaType.TEXT_R_MARKDOWN;
import static com.keenwrite.processors.html.IdentityProcessor.IDENTITY;

/**
 * Responsible for converting documents to plain text files. This will
 * perform interpolated variable substitutions and execute R commands
 * as necessary.
 */
public class TextProcessor extends ExecutorProcessor<String> {
  private final Function<String, String> mEvaluator;

  public TextProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    super( successor );

    final var inputPath = context.getSourcePath();
    final var mediaType = MediaType.fromFilename( inputPath );

    if( mediaType == TEXT_R_MARKDOWN ) {
      final var rVarProcessor = new RVariableProcessor( IDENTITY, context );
      mEvaluator = new RInlineEvaluator( rVarProcessor );
    }
    else {
      mEvaluator = new VariableProcessor( IDENTITY, context );
    }
  }

  @Override
  public String apply( final String document ) {
    return mEvaluator.apply( document );
  }
}
