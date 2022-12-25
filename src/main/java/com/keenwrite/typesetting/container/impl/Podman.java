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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Provides facilities for interacting with a container environment.
 */
public final class Podman implements Container {
  public static final SysFile CONTAINER = new SysFile( "podman" );

  private final Consumer<String> mConsumer;
  private final AtomicBoolean mInstalling = new AtomicBoolean();

  /**
   * @param consumer Provides status updates when running with the container.
   */
  public Podman( final Consumer<String> consumer ) {
    mConsumer = consumer;
  }

  @Override
  public void install( final File exe, final Consumer<Integer> exitCode ) {
    // This monstrosity is required to run the installer in the background
    // without displaying a secondary command window while blocking until the
    // installer completes and an exit code can be determined. I hate Windows.
    final var builder = processBuilder(
      "cmd", "/c",
      format(
        "start /b /high /wait cmd /c %s /quiet /install & exit ^!errorlevel^!",
        exe.getAbsolutePath()
      )
    );

    try {
      mInstalling.set( true );
      final var process = runAsync( builder );

      // Wait for installation to finish (successfully or not).
      exitCode.accept( process.waitFor() );
    } catch( final Exception e ) {
      exitCode.accept( -1 );
    }

    mInstalling.set( false );
  }

  @Override
  public boolean isInstalling() {
    return mInstalling.get();
  }

  @Override
  public void start() throws CommandNotFoundException {
    machine( "init" );
    machine( "start" );
  }

  @Override
  public void stop() {
  }

  @Override
  public void pull( final String name, final String version )
    throws CommandNotFoundException {
    final var repo = format( "ghcr.io/davejarvis/%s:%s", name, version );
    podman( "pull", repo );
  }

  private void machine( final String option ) throws CommandNotFoundException {
    podman( "machine", option );
  }

  private void podman( final String... args ) throws CommandNotFoundException {
    try {
      final var exe = CONTAINER.locate();
      final var path = exe.orElseThrow();
      final var builder = processBuilder( path, args );
      final var process = runAsync( builder );

      process.waitFor();
    } catch( final Exception ex ) {
      throw new CommandNotFoundException( CONTAINER.toString() );
    }
  }

  private <T> void runAsync( final Callable<T> callable ) {
    try( final var executor = createExecutor() ) {
      executor.submit( callable );
    }
  }

  private Process runAsync( final ProcessBuilder builder ) throws IOException {
    final var process = builder.start();
    final var output = process.getInputStream();
    final var gobbler = new StreamGobbler( output, mConsumer );

    runAsync( gobbler );

    return process;
  }

  private ProcessBuilder processBuilder( final String... args ) {
    final var builder = new ProcessBuilder( args );
    builder.redirectErrorStream( true );

    return builder;
  }

  private ProcessBuilder processBuilder( final File file, final String... s ) {
    final var commands = new LinkedList<String>();
    commands.add( file.getAbsolutePath() );
    commands.addAll( Arrays.asList( s ) );

    return processBuilder( commands.toArray( new String[ 0 ] ) );
  }

  private ProcessBuilder processBuilder( final Path path, final String... s ) {
    return processBuilder( path.toFile(), s );
  }

  private ExecutorService createExecutor() {
    return newFixedThreadPool( 1 );
  }
}
