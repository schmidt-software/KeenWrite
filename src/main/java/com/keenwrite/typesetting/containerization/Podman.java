/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.typesetting.containerization;

import com.keenwrite.Messages;
import com.keenwrite.io.CommandNotFoundException;
import com.keenwrite.io.SysFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.SysFile.toFile;
import static com.keenwrite.util.SystemUtils.IS_OS_WINDOWS;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

/**
 * Provides facilities for interacting with a container environment.
 */
public final class Podman implements ContainerManager {
  private static final String BINARY = "podman";
  private static final Path BINARY_PATH =
    Path.of(
      format( IS_OS_WINDOWS
                ? "C:\\Program Files\\RedHat\\Podman\\%s.exe"
                : "/usr/bin/%s",
              BINARY
      )
    );
  private static final SysFile MANAGER = new SysFile( BINARY );

  private final List<String> mMountPoints = new LinkedList<>();

  public Podman() { }

  /**
   * Answers whether the container is installed and runnable on the host.
   *
   * @return {@code true} if the container is available.
   */
  public static boolean canRun() {
    try {
      return toFile( getExecutable() ).isFile();
    } catch( final Exception ex ) {
      clue( "Wizard.container.executable.run.error", ex );

      // If the binary couldn't be found, then indicate that it cannot run.
      return false;
    }
  }

  private static Path getExecutable() {
    final var executable = Files.isExecutable( BINARY_PATH );

    clue( "Wizard.container.executable.run.scan", BINARY_PATH, executable );

    return executable
      ? BINARY_PATH
      : MANAGER.locate().orElseThrow();
  }

  @Override
  public int install( final File exe ) {
    // This monstrosity runs the installer in the background without displaying
    // a secondary command window, while blocking until the installer completes
    // and an exit code can be determined. I hate Windows.
    final var cmd = format(
      "start /b /high /wait cmd /c %s /quiet /install & exit ^!errorlevel^!",
      exe.getAbsolutePath()
    );

    clue( "Wizard.container.install.command", cmd );

    final var builder = processBuilder( "cmd", "/c", cmd );

    try {
      clue( "Wizard.container.install.await", cmd );

      // Wait for installation to finish (successfully or not).
      return wait( builder.start() );
    } catch( final Exception ignored ) {
      return -1;
    }
  }

  @Override
  public void start( final StreamProcessor processor )
    throws CommandNotFoundException {
    machine( processor, "stop" );
    podman( processor, "system", "prune", "--force" );
    machine( processor, "rm", "--force" );
    machine( processor, "init" );
    machine( processor, "start" );
  }

  @Override
  public void load( final StreamProcessor processor )
    throws CommandNotFoundException {
    final var url = Messages.get( "Wizard.typesetter.container.image.url" );

    podman( processor, "load", "-i", url );
  }

  /**
   * Runs:
   * <p>
   * <code>podman run --network=host --rm -t IMAGE /bin/sh -lc</code>
   * </p>
   * followed by the given arguments.
   *
   * @param args The command and arguments to run against the container.
   * @return The exit code from running the container manager (not the
   * exit code from running the command).
   * @throws CommandNotFoundException Container manager couldn't be found.
   */
  @Override
  public int run(
    final StreamProcessor processor,
    final String... args ) throws CommandNotFoundException {
    final var tag = Messages.get( "Wizard.typesetter.container.image.tag" );

    final var options = new LinkedList<String>();
    options.add( "run" );
    options.add( "--rm" );
    options.add( "--network=host" );
    options.addAll( mMountPoints );
    options.add( "-t" );
    options.add( tag );
    options.add( "/bin/sh" );
    options.add( "-lc" );

    final var command = toArray( toArray( options ), args );
    return podman( processor, command );
  }

  /**
   * Generates a command-line argument representing a mount point between
   * the host and guest systems.
   *
   * @param hostDir  The host directory to mount in the container.
   * @param guestDir The guest directory to map from the container to host.
   * @param readonly Set {@code true} to make the mount point read-only.
   */
  public void mount(
    final Path hostDir, final String guestDir, final boolean readonly ) {
    assert hostDir != null;
    assert guestDir != null;
    assert !guestDir.isBlank();
    assert toFile( hostDir ).isDirectory();

    mMountPoints.add(
      format( "-v%s:%s:%s", hostDir, guestDir, readonly ? "ro" : "Z" )
    );
  }

  private static void machine(
    final StreamProcessor processor,
    final String... args )
    throws CommandNotFoundException {
    podman( processor, toArray( "machine", args ) );
  }

  private static int podman(
    final StreamProcessor processor, final String... args )
    throws CommandNotFoundException {
    try {
      final var path = getExecutable();
      final var joined = join( ",", args );

      clue( "Wizard.container.process.enter", path, joined );

      final var builder = processBuilder( path, args );
      final var process = builder.start();

      processor.start( process.getInputStream() );

      return wait( process );
    } catch( final Exception ex ) {
      clue( ex );
      throw new CommandNotFoundException( MANAGER.toString() );
    }
  }

  /**
   * Performs a blocking wait until the {@link Process} completes.
   *
   * @param process The {@link Process} to await completion.
   * @return The exit code from running a command.
   * @throws InterruptedException The {@link Process} was interrupted.
   */
  private static int wait( final Process process ) throws InterruptedException {
    final var exitCode = process.waitFor();

    clue( "Wizard.container.process.exit", exitCode );

    process.destroy();

    return exitCode;
  }

  private static ProcessBuilder processBuilder( final String... args ) {
    final var builder = new ProcessBuilder( args );
    builder.redirectErrorStream( true );

    return builder;
  }

  private static ProcessBuilder processBuilder(
    final File file, final String... s ) {
    return processBuilder( toArray( file.getAbsolutePath(), s ) );
  }

  private static ProcessBuilder processBuilder(
    final Path path, final String... s ) {
    return processBuilder( toFile( path ), s );
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
  private static <T> T[] toArray( final T[] first, final T[] second ) {
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
  private static String[] toArray( final String first, String... second ) {
    assert first != null;
    assert second != null;
    assert second.length > 0;

    return toArray( new String[]{first}, second );
  }
}
