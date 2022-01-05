/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.sigils.RKeyOperator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.keenwrite.constants.Constants.STATUS_PARSE_ERROR;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.processors.r.RVariableProcessor.escape;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static java.lang.Math.min;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class InlineRProcessor extends VariableProcessor {
  public static final String PREFIX = "`r#";
  public static final char SUFFIX = '`';

  private static final int PREFIX_LENGTH = PREFIX.length();

  /**
   * Set to {@code true} when the R bootstrap script is loaded successfully.
   */
  private final AtomicBoolean mReady = new AtomicBoolean();

  private final RKeyOperator mOperator = new RKeyOperator();

  /**
   * Constructs a processor capable of evaluating R statements.
   *
   * @param successor Subsequent link in the processing chain.
   * @param context   Contains resolved definitions map.
   */
  public InlineRProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    super( successor, context );
  }

  /**
   * Initializes the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   * <p>
   * If the R code to bootstrap contained variables, and they were all updated
   * successfully, this will update the internal ready flag to {@code true}.
   */
  public void init() {
    final var context = getContext();
    final var bootstrap = context.getRScript();

    if( !bootstrap.isBlank() ) {
      final var wd = context.getRWorkingDir();
      final var dir = wd.toString().replace( '\\', '/' );
      final var definitions = getContext().getDefinitions();
      final var map = new HashMap<String, String>( definitions.size() + 1 );

      definitions.forEach(
        ( k, v ) -> map.put( mOperator.apply( k ), escape( v ) )
      );
      map.put(
        mOperator.apply( "application.r.working.directory" ),
        escape( dir )
      );

      try {
        Engine.eval( replace( bootstrap, map ) );
        mReady.set( true );
      } catch( final Exception ex ) {
        clue( ex );
        // A problem with the bootstrap script is likely caused by variables
        // not being loaded. This implies that the R processor is being invoked
        // too soon.
      }
    }
  }

  /**
   * Answers whether R has been initialized without failures.
   *
   * @return {@code true} the R engine is ready to process inline R statements.
   */
  public boolean isReady() {
    return mReady.get();
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
  public @NotNull String apply( final String text ) {
    final int length = text.length();

    // The * 2 is a wild guess at the ratio of R statements to the length
    // of text produced by those statements.
    final StringBuilder sb = new StringBuilder( length * 2 );

    int prevIndex = 0;
    int currIndex = text.indexOf( PREFIX );

    while( currIndex >= 0 ) {
      // Copy everything up to, but not including, the opening token.
      sb.append( text, prevIndex, currIndex );

      // Jump to the start of the R statement.
      prevIndex = currIndex + PREFIX_LENGTH;

      // Find the closing token, without indexing past the text boundary.
      currIndex = text.indexOf( SUFFIX, min( currIndex + 1, length ) );

      // Only evaluate inline R statements that have end delimiters.
      if( currIndex > 1 ) {
        // Extract the inline R statement to be evaluated.
        final var r = text.substring( prevIndex, currIndex );

        // Pass the R statement into the R engine for evaluation.
        try {
          // Append the string representation of the result into the text.
          sb.append( Engine.eval( r ) );
        } catch( final Exception ex ) {
          // Inform the user that there was a problem.
          clue( STATUS_PARSE_ERROR, ex.getMessage(), currIndex );

          // If the string couldn't be parsed using R, append the statement
          // that failed to parse, instead of its evaluated value.
          sb.append( PREFIX ).append( r ).append( SUFFIX );
        }

        // Retain the R statement's ending position in the text.
        prevIndex = currIndex + 1;
      }

      // Find the start of the next inline R statement.
      currIndex = text.indexOf( PREFIX, min( currIndex + 1, length ) );
    }

    // Copy from the previous index to the end of the string.
    return sb.append( text.substring( min( prevIndex, length ) ) ).toString();
  }
}
