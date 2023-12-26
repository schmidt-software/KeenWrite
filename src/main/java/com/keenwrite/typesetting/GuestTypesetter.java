/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.io.StreamGobbler;
import com.keenwrite.io.SysFile;
import com.keenwrite.typesetting.containerization.Podman;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.StreamGobbler.gobble;
import static com.keenwrite.io.SysFile.normalize;
import static java.lang.String.format;

/**
 * Responsible for invoking a typesetter installed inside a container.
 */
public final class GuestTypesetter extends Typesetter
  implements Callable<Boolean> {
  private static final String SOURCE = "/root/source";
  private static final String TARGET = "/root/target";
  private static final String THEMES = "/root/themes";
  private static final String IMAGES = "/root/images";
  private static final String CACHES = "/root/caches";
  private static final String FONTS = "/root/fonts";

  private static final boolean READONLY = true;
  private static final boolean READWRITE = false;

  private static final String TYPESETTER_VERSION =
    STR."\{TYPESETTER_EXE} --version > /dev/null";

  public GuestTypesetter( final Mutator mutator ) {
    super( mutator );
  }

  @Override
  public Boolean call() throws Exception {
    final var sourcePath = getSourcePath();
    final var targetPath = getTargetPath();
    final var themesPath = getThemeDir();

    final var sourceDir = normalize( sourcePath.getParent() );
    final var targetDir = normalize( targetPath.getParent() );
    final var themesDir = normalize( themesPath.getParent() );
    final var imagesDir = normalize( getImageDir() );
    final var cachesDir = normalize( getCacheDir() );
    final var fontsDir = normalize( getFontDir() );

    final var sourceFile = sourcePath.getFileName();
    final var targetFile = targetPath.getFileName();
    final var themesFile = themesPath.getFileName();

    final var manager = new Podman();
    manager.mount( sourceDir, SOURCE, READONLY );
    manager.mount( targetDir, TARGET, READWRITE );
    manager.mount( themesDir, THEMES, READONLY );
    manager.mount( imagesDir, IMAGES, READONLY );
    manager.mount( cachesDir, CACHES, READWRITE );
    manager.mount( fontsDir, FONTS, READONLY );

    final var args = new LinkedList<String>();
    args.add( TYPESETTER_EXE );
    args.addAll( commonOptions() );
    args.add( format(
      "--arguments=themesdir=%s/%s,imagesdir=%s,cachesdir=%s",
      THEMES, themesFile, IMAGES, CACHES
    ) );
    args.add( format( "--path='%s/%s'", THEMES, themesFile ) );
    args.add( format( "--result='%s'", removeExtension( targetFile ) ) );
    args.add( format( "%s/%s", SOURCE, sourceFile ) );

    final var listener = new PaginationListener();
    final var command = String.join( " ", args );

    manager.run( in -> StreamGobbler.gobble( in, listener ), command );

    return true;
  }

  static String removeExtension( final Path path ) {
    return FilenameUtils.removeExtension( SysFile.getFileName( path ) );
  }

  /**
   * @return {@code true} indicates that the containerized typesetter is
   * installed, properly configured, and ready to typeset documents.
   */
  static boolean isReady() {
    if( Podman.canRun() ) {
      final var exitCode = new StringBuilder();
      final var manager = new Podman();

      try {
        // Running blocks until the command completes.
        manager.run(
          input -> gobble( input, s -> exitCode.append( s.trim() ) ),
          STR."\{TYPESETTER_VERSION}; echo $?"
        );

        // If the typesetter ran with an exit code of 0, it is available.
        return exitCode.indexOf( "0" ) == 0;
      } catch( final CommandNotFoundException ex ) {
        clue( ex );
      }
    }

    return false;
  }
}
