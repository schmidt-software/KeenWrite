/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.collections.BoundedCache;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static java.lang.Math.min;

/**
 * Responsible for executing R statements, which can also update the engine's
 * state.
 */
public final class Engine {
  /**
   * Inline R expressions that have already been evaluated.
   */
  private static final Map<String, String> sCache =
    new BoundedCache<>( 512 );

  /**
   * Engine for evaluating R expressions.
   */
  private static final ScriptEngine sEngine =
    new ScriptEngineManager().getEngineByName( "Renjin" );

  /**
   * Empties the cache.
   */
  public static void clear() {
    sCache.clear();
  }

  /**
   * Look up an R expression from the cache then return the resulting object.
   * If the R expression hasn't been cached, it'll first be evaluated.
   *
   * @param r R expression to evaluate.
   * @return The object resulting from the evaluation.
   */
  public static String eval( final String r ) {
    return sCache.computeIfAbsent( r, __ -> evaluate( r ) );
  }

  /**
   * Returns the result of an R expression as an object converted to string.
   *
   * @param r R expression to evaluate.
   * @return The object resulting from the evaluation.
   */
  private static String evaluate( final String r ) {
    try {
      return sEngine.eval( r ).toString();
    } catch( final Exception ex ) {
      final var expr = r.substring( 0, min( r.length(), 50 ) );
      clue( get( "Main.status.error.r", expr, ex.getMessage() ), ex );
      throw new IllegalArgumentException( r );
    }
  }
}
