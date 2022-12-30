/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.containerization;

import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.io.StreamGobbler;
import com.keenwrite.io.SysFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.keenwrite.Bootstrap.APP_VERSION_CLEAN;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Provides facilities for interacting with a container environment.
 */
public final class Podman implements ContainerManager {
  public static final SysFile MANAGER = new SysFile( "podman" );
  public static final String CONTAINER_SHORTNAME = "typesetter";
  public static final String CONTAINER_NAME =
    format( "%s:%s", CONTAINER_SHORTNAME, APP_VERSION_CLEAN );

  private final Consumer<String> mConsumer;

  /**
   * @param consumer Receives stdout/stderr from commands run on a container.
   */
  public Podman( final Consumer<String> consumer ) {
    mConsumer = consumer;
  }

  @Override
  public int install( final File exe ) {
    // This monstrosity runs the installer in the background without displaying
    // a secondary command window, while blocking until the installer completes
    // and an exit code can be determined. I hate Windows.
    final var builder = processBuilder(
      "cmd", "/c",
      format(
        "start /b /high /wait cmd /c %s /quiet /install & exit ^!errorlevel^!",
        exe.getAbsolutePath()
      )
    );

    try {
      final var process = runAsync( builder );

      // Wait for installation to finish (successfully or not).
      return process.waitFor();
    } catch( final Exception ignored ) {
      return -1;
    }
  }

  @Override
  public void start() throws CommandNotFoundException {
    machine( "stop" );
    podman( "system", "prune", "--force" );
    machine( "rm", "--force" );
    machine( "init" );
    machine( "start" );
  }

  @Override
  public void pull( final String name ) throws CommandNotFoundException {
    podman( "pull", "ghcr.io/davejarvis/" + name );
  }

  /**
   * Runs:
   * <p>
   * <code>podman run --network="host" --rm -t IMAGE /bin/sh -lc</code>
   * </p>
   * followed by the given arguments.
   *
   * @param args The command and arguments to run against the container.
   * @return The exit code from running the container manager (not the
   * exit code from running the command).
   * @throws CommandNotFoundException Container manager couldn't be found.
   */
  @Override
  public int run( final String... args ) throws CommandNotFoundException {
    final var prefix = new String[]{
      "run", "--rm", "--network=host", "-t", CONTAINER_NAME, "/bin/sh", "-lc"
    };

    return podman( toArray( prefix, args ) );
  }

  private void machine( final String... args ) throws CommandNotFoundException {
    podman( toArray( "machine", args ) );
  }

  private int podman( final String... args ) throws CommandNotFoundException {
    try {
      final var exe = MANAGER.locate();
      final var path = exe.orElseThrow();
      final var builder = processBuilder( path, args );
      final var process = runAsync( builder );

      return process.waitFor();
    } catch( final Exception ex ) {
      throw new CommandNotFoundException( MANAGER.toString() );
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
    return processBuilder( toArray( file.getAbsolutePath(), s ) );
  }

  private ProcessBuilder processBuilder( final Path path, final String... s ) {
    return processBuilder( path.toFile(), s );
  }

  private ExecutorService createExecutor() {
    return newFixedThreadPool( 1 );
  }

  /**
   * Merges two arrays into a single array.
   *
   * @param first  The first array to merge before the second array.
   * @param second The second array to merge after the first array.
   * @param <T>    The type of arrays to merge.
   * @return The merged arrays, with the first array elements preceding the
   * second array's elements.
   */
  private <T> T[] toArray( final T[] first, final T[] second ) {
    assert first != null;
    assert second != null;
    assert first.length > 0;
    assert second.length > 0;

    final var merged = copyOf( first, first.length + second.length );
    arraycopy( second, 0, merged, first.length, second.length );
    return merged;
  }

  /**
   * Convenience method to merge a single string with an array of strings.
   *
   * @param first  The first item to prepend to the secondary items.
   * @param second The second item to combine with the first item.
   * @return A new array with the first element at index 0 and the second
   * elements starting at index 1.
   */
  private String[] toArray( final String first, String... second ) {
    assert first != null;
    assert second != null;
    assert second.length > 0;

    return toArray( new String[]{first}, second );
  }
}
