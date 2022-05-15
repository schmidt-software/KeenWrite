/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.keenwrite.constants.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.processors.IdentityProcessor.IDENTITY;

/**
 * Evaluates inline R statements.
 */
public final class RInlineEvaluator
  implements Function<String, String>, Predicate<String> {
  public static final String PREFIX = "`r#";
  public static final String SUFFIX = "`";

  private static final int PREFIX_LENGTH = PREFIX.length();
  private static final int SUFFIX_LENGTH = SUFFIX.length();

  private final Processor<String> mProcessor;

  /**
   * Constructs an evaluator capable of executing R statements.
   */
  public RInlineEvaluator( final ProcessorContext context ) {
    mProcessor = new RVariableProcessor( IDENTITY, context );
  }

  /**
   * Evaluates all R statements in the source document and inserts the
   * calculated value into the generated document.
   *
   * @param text The document text that includes variables that should be
   *             replaced with values when rendered as HTML.
   * @return The generated document with output from all R statements
   * substituted with value returned from their execution.
   */
  @Override
  public String apply( final String text ) {
    try {
      final var len = text.length();
      final var r = mProcessor.apply(
        text.substring( PREFIX_LENGTH, len - SUFFIX_LENGTH )
      );

      // Return the evaluated R expression for insertion back into the text.
      return Engine.eval( r );
    } catch( final Exception ex ) {
      clue( STATUS_PARSE_ERROR, ex.getMessage() );

      // If the string couldn't be parsed using R, append the statement
      // that failed to parse, instead of its evaluated value.
      return text;
    }
  }

  /**
   * Answers whether the engine associated with this evaluator may attempt to
   * evaluate the given source code statement.
   *
   * @param code The source code to verify.
   * @return {@code true} if the code may be evaluated.
   */
  @Override
  public boolean test( final String code ) {
    return code.startsWith( PREFIX );
  }
}
