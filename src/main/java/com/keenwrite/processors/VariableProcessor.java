/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.keenwrite.processors.text.TextReplacementFactory.replace;

/**
 * Processes interpolated string definitions in the document and inserts
 * their values into the post-processed text. The default variable syntax is
 * <pre>{{variable}}</pre> (a.k.a., moustache syntax).
 */
public class VariableProcessor
  extends ExecutorProcessor<String> implements Function<String, String> {

  private final ProcessorContext mContext;
  private final UnaryOperator<String> mSigilOperator;

  /**
   * Constructs a processor capable of interpolating string definitions.
   *
   * @param successor Subsequent link in the processing chain.
   * @param context   Contains resolved definitions map.
   */
  public VariableProcessor(
    final Processor<String> successor,
    final ProcessorContext context ) {
    super( successor );

    mSigilOperator = createKeyOperator( context );
    mContext = context;
  }

  /**
   * Subclasses may change the type of operation performed on keys, such as
   * wrapping key names in sigils.
   *
   * @param context Provides the name of the file being edited.
   * @return An operator for transforming key names.
   */
  protected UnaryOperator<String> createKeyOperator(
    final ProcessorContext context ) {
    return context.createSigilOperator();
  }

  /**
   * Returns the map to use for variable substitution.
   *
   * @return A map of variable names to values, with keys wrapped in sigils.
   */
  protected Map<String, String> getDefinitions() {
    return entoken( mContext.getInterpolatedDefinitions() );
  }

  /**
   * Subclasses may override this method to change how keys are wrapped
   * in sigils.
   *
   * @param key The key to enwrap.
   * @return The wrapped key.
   */
  protected String processKey( final String key ) {
    return mSigilOperator.apply( key );
  }

  /**
   * Subclasses may override this method to modify values prior to use. This
   * can be used, for example, to escape values prior to evaluating by a
   * scripting engine.
   *
   * @param value The value to process.
   * @return The processed value.
   */
  protected String processValue( final String value ) {
    return value;
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
   * Converts the given map from regular variables to processor-specific
   * variables.
   *
   * @param map Map of variable names to values.
   * @return Map of variables with the keys and values subjected to
   * post-processing.
   */
  protected Map<String, String> entoken( final Map<String, String> map ) {
    final var result = new HashMap<String, String>( map.size() );

    map.forEach( ( k, v ) -> result.put( processKey( k ), processValue( v ) ) );

    return result;
  }

  protected ProcessorContext getContext() {
    return mContext;
  }
}
