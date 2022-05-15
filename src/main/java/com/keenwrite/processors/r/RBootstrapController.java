/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.r;

import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.sigils.RKeyOperator;

import java.util.HashMap;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.processors.r.RVariableProcessor.escape;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class RBootstrapController {

  private final static RKeyOperator KEY_OPERATOR = new RKeyOperator();

  private RBootstrapController() {}

  /**
   * Initializes the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   * <p>
   * If the R code to bootstrap contained variables, and they were all updated
   * successfully, this will update the internal ready flag to {@code true}.
   */
  public static void init( final ProcessorContext context ) {
    final var bootstrap = context.getRScript();

    if( !bootstrap.isBlank() ) {
      final var wd = context.getRWorkingDir();
      final var dir = wd.toString().replace( '\\', '/' );
      final var definitions = context.getDefinitions();
      final var map = new HashMap<String, String>( definitions.size() + 1 );

      definitions.forEach(
        ( k, v ) -> map.put( KEY_OPERATOR.apply( k ), escape( v ) )
      );
      map.put(
        KEY_OPERATOR.apply( "application.r.working.directory" ),
        escape( dir )
      );

      try {
        Engine.eval( replace( bootstrap, map ) );
      } catch( final Exception ex ) {
        clue( ex );
        // A problem with the bootstrap script is likely caused by variables
        // not being loaded. This implies that the R processor is being invoked
        // too soon.
      }
    }
  }
}
