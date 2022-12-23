/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.impl;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.io.SysFile;
import com.keenwrite.typesetting.container.api.Container;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class Podman implements Container {
  public static final SysFile CONTAINER = new SysFile( "podman" );

  private final Consumer<String> mConsumer;

  /**
   * @param consumer Provides status updates.
   */
  public Podman( final Consumer<String> consumer ) {
    mConsumer = consumer;
  }

  @Override
  public void install() throws CommandNotFoundException {
  }

  @Override
  public void start() throws CommandNotFoundException {
    machine( "init" );
    machine( "start" );
  }

  private void machine( final String option ) throws CommandNotFoundException {
    run( "machine", option );
  }

  private void run( final String... args ) throws CommandNotFoundException {
    try {
      final var exe = CONTAINER.locate();
      final var path = exe.orElseThrow();
      final var commands = new LinkedList<String>();
      commands.add( path.toAbsolutePath().toString() );
      commands.addAll( Arrays.asList( args ) );

      final var builder = new ProcessBuilder( commands );
      builder.redirectErrorStream( true );

      final var process = builder.start();
      final var output = process.getInputStream();

      try( final var executor = newFixedThreadPool( 1 ) ) {
        final var gobbler = new StreamGobbler( output, mConsumer );
        executor.submit( gobbler );
      }
    } catch( final Exception ex ) {
      throw new CommandNotFoundException( CONTAINER.toString() );
    }
  }

  @Override
  public void pull( final String name, final String version )
    throws CommandNotFoundException {
    final var repo = format( "ghcr.io/davejarvis/%s:%s", name, version );
    run( "pull", repo );
  }

  @Override
  public void stop() {
  }
}
