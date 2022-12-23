/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.impl;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.io.SysFile;
import com.keenwrite.typesetting.container.api.Container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Provides facilities for interacting with a container environment.
 */
public class Podman implements Container {
  public static final SysFile CONTAINER = new SysFile( "podman" );

  private final Consumer<String> mConsumer;

  /**
   * @param consumer Provides status updates when running with the container.
   */
  public Podman( final Consumer<String> consumer ) {
    mConsumer = consumer;
  }

  @Override
  public void install( final File exe ) throws IOException {
    final var builder = processBuilder( exe, "/quiet", "/install" );
    run( builder );
  }

  @Override
  public void start() throws CommandNotFoundException {
    machine( "init" );
    machine( "start" );
  }

  private void machine( final String option ) throws CommandNotFoundException {
    podman( "machine", option );
  }

  private void podman( final String... args ) throws CommandNotFoundException {
    try {
      final var exe = CONTAINER.locate();
      final var path = exe.orElseThrow();
      final var builder = processBuilder( path, args );

      run( builder );
    } catch( final Exception ex ) {
      throw new CommandNotFoundException( CONTAINER.toString() );
    }
  }

  private void run( final ProcessBuilder builder ) throws IOException {
    final var process = builder.start();
    final var output = process.getInputStream();

    try( final var executor = newFixedThreadPool( 1 ) ) {
      final var gobbler = new StreamGobbler( output, mConsumer );
      executor.submit( gobbler );
    }
  }

  @Override
  public void pull( final String name, final String version )
    throws CommandNotFoundException {
    final var repo = format( "ghcr.io/davejarvis/%s:%s", name, version );
    podman( "pull", repo );
  }

  @Override
  public void stop() {
  }

  private ProcessBuilder processBuilder( final File file, final String... s ) {
    final var commands = new LinkedList<String>();
    commands.add( file.getAbsolutePath() );
    commands.addAll( Arrays.asList( s ) );

    final var builder = new ProcessBuilder( commands );
    builder.redirectErrorStream( true );

    return builder;
  }

  private ProcessBuilder processBuilder( final Path path, final String... s ) {
    return processBuilder( path.toFile(), s );
  }
}
