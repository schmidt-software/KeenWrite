/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.r;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.sigils.RKeyOperator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.KEY_R_DIR;
import static com.keenwrite.preferences.AppKeys.KEY_R_SCRIPT;
import static com.keenwrite.processors.r.RVariableProcessor.escape;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;

/**
 * Transforms a document containing R statements into Markdown.
 */
public final class RBootstrapController {

  private static final RKeyOperator KEY_OPERATOR = new RKeyOperator();

  private final Workspace mWorkspace;
  private final Supplier<Map<String, String>> mSupplier;

  public RBootstrapController(
    final Workspace workspace,
    final Supplier<Map<String, String>> supplier ) {
    assert workspace != null;
    assert supplier != null;

    mWorkspace = workspace;
    mSupplier = supplier;

    mWorkspace.stringProperty( KEY_R_SCRIPT )
              .addListener( ( c, o, n ) -> update() );
    mWorkspace.fileProperty( KEY_R_DIR )
              .addListener( ( c, o, n ) -> update() );

    // Add the definitions immediately upon loading them.
    update();
  }

  /**
   * Updates the R code so that R can find imported libraries. Note that
   * any existing R functionality will not be overwritten if this method is
   * called multiple times.
   */
  public void update() {
    final var bootstrap = getRScript();

    if( !bootstrap.isBlank() ) {
      final var dir = getRWorkingDirectory();
      final var definitions = mSupplier.get();

      update( bootstrap, dir, definitions );
    }
  }

  public static void update(
    final String bootstrap,
    final String workingDir,
    final Map<String, String> definitions ) {

    if( !bootstrap.isBlank() ) {
      final var map = new HashMap<String, String>( definitions.size() + 1 );

      definitions.forEach(
        ( k, v ) -> map.put( KEY_OPERATOR.apply( k ), escape( v ) )
      );
      map.put(
        KEY_OPERATOR.apply( "application.r.working.directory" ),
        escape( workingDir )
      );

      try {
        Engine.eval( replace( bootstrap, map ) );
      } catch( final Exception ex ) {
        clue( ex );
      }
    }
  }

  private String getRScript() {
    return mWorkspace.getString( KEY_R_SCRIPT );
  }

  private String getRWorkingDirectory() {
    final var wd = mWorkspace.getFile( KEY_R_DIR );
    return wd.toString().replace( '\\', '/' );
  }
}
