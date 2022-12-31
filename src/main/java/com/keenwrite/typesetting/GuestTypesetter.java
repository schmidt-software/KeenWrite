/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.containerization.Podman;

import java.util.concurrent.Callable;

import static com.keenwrite.typesetting.containerization.Podman.MANAGER;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 * This uses a version of the typesetter installed in a container.
 */
public final class GuestTypesetter extends Typesetter
  implements Callable<Void> {
  private static final String TYPESETTER_VERSION =
    TYPESETTER_EXE + " --version > /dev/null";

  public GuestTypesetter( final Mutator mutator ) {
    super( mutator );
  }

  @Override
  public Void call() throws Exception {
    final var sb = new StringBuilder( 128 );
    options().forEach( arg -> sb.append( arg ).append( " " ) );
    System.out.println( sb );

    return null;
  }

  static boolean isReady() {
    if( MANAGER.canRun() ) {
      final var exitCode = new StringBuilder();
      final var manager = new Podman( s -> exitCode.append( s.trim() ) );

      try {
        manager.run( TYPESETTER_VERSION + "; echo $?" );

        // If the typesetter ran with an exit code of 0, it is available.
        return exitCode.indexOf( "0" ) == 0;
      } catch( final CommandNotFoundException ignored ) { }
    }

    return false;
  }
}
