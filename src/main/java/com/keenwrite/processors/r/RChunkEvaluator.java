/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.ProcessorContext;

import java.util.function.Function;

import static com.keenwrite.constants.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.events.StatusEvent.clue;

/**
 * Transforms a document containing R statements into Markdown. The statements
 * are part of an R chunk, <code>```{r}</code>.
 */
public final class RChunkEvaluator implements Function<String, String> {

  private final ProcessorContext mContext;

  /**
   * Constructs an evaluator capable of executing R statements.
   *
   * @param context Used to initialize the {@link RBootstrapController}.
   */
  public RChunkEvaluator( final ProcessorContext context ) {
    mContext = context;
  }

  /**
   * Evaluates the given R statements and returns the result as a string.
   * If an image was produced, the calling code is responsible for persisting
   * and making the file embeddable into the document.
   *
   * @param r The R statements to evaluate.
   * @return The output from the final R statement.
   */
  @Override
  public String apply( final String r ) {
    try {
      RBootstrapController.init( mContext );
      return Engine.eval( r );
    } catch( final Exception ex ) {
      clue( STATUS_PARSE_ERROR, ex.getMessage() );

      return r;
    }
  }
}
