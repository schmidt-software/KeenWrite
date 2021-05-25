/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import java.util.Map;
import java.util.function.Function;

import static com.keenwrite.processors.text.TextReplacementFactory.replace;

/**
 * Processes interpolated string definitions in the document and inserts
 * their values into the post-processed text. The default variable syntax is
 * {@code $variable$}.
 */
public class DefinitionProcessor
  extends ExecutorProcessor<String> implements Function<String, String> {

  private final Map<String, String> mDefinitions;

  /**
   * Constructs a processor capable of interpolating string definitions.
   *
   * @param successor Subsequent link in the processing chain.
   * @param context   Contains resolved definitions map.
   */
  public DefinitionProcessor(
      final Processor<String> successor,
      final ProcessorContext context ) {
    super( successor );
    mDefinitions = context.getResolvedMap();
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
    return replace( text, getDefinitions() );
  }

  /**
   * Returns the map to use for variable substitution.
   *
   * @return A map of variable names to values.
   */
  protected Map<String, String> getDefinitions() {
    return mDefinitions;
  }
}
