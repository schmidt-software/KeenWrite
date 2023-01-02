/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.typesetting.containerization.Podman;

import java.util.concurrent.Callable;

import static com.keenwrite.typesetting.containerization.Podman.MANAGER;
import static com.keenwrite.typesetting.containerization.Podman.mountPoint;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 * This uses a version of the typesetter installed in a container.
 */
public final class GuestTypesetter extends Typesetter
  implements Callable<Void> {
  private static final boolean READONLY = true;
  private static final boolean READWRITE = !READONLY;

  private static final String TYPESETTER_VERSION =
    TYPESETTER_EXE + " --version > /dev/null";

  public GuestTypesetter( final Mutator mutator ) {
    super( mutator );
  }

  @Override
  public Void call() throws Exception {
    final var targetDir = getTargetPath().getParent();
    final var sourceDir = getSourcePath().getParent();
    final var themesDir = getThemesPath().getParent();
    final var imagesDir = getImagesPath();
    final var fontsDir = getFontsPath();

    final var source = mountPoint( sourceDir, "/root/source", READONLY );
    final var target = mountPoint( targetDir, "/root/target", READWRITE );
    final var themes = mountPoint( themesDir, "/root/themes", READONLY );
    final var images = mountPoint( imagesDir, "/root/images", READONLY );
    final var fonts = mountPoint( fontsDir, "/root/fonts", READONLY );

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
