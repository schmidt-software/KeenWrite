/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.containerization;

import com.keenwrite.io.CommandNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ContainerManager {
  /**
   * Installs the container software, in quiet and headless mode if possible.
   *
   * @param exe The installer binary to run.
   * @return The exit code from the installer program, or -1 on failure.
   * @throws IOException The container installer could not be run.
   */
  int install( final File exe )
    throws IOException;

  /**
   * Runs preliminary commands against the container before starting.
   *
   * @throws CommandNotFoundException The container executable was not found.
   */
  void start() throws CommandNotFoundException;

  /**
   * Requests that the container manager load an image into the container.
   *
   * @param name The full container name of the image to pull.
   * @throws CommandNotFoundException The container executable was not found.
   */
  void pull( String name ) throws CommandNotFoundException;

  /**
   * Runs a command using the container manager.
   *
   * @param args The command and arguments to run.
   * @return The exit code returned by the installer program.
   * @throws CommandNotFoundException The container executable was not found.
   */
  int run( final String... args ) throws CommandNotFoundException;

  /**
   * Convenience method to run a command using the container manager.
   *
   * @see #run(String...)
   */
  default int run( final List<String> args ) throws CommandNotFoundException {
    return run( toArray( args ) );
  }

  /**
   * Convenience method to convert a {@link List} into an array.
   *
   * @param list The elements to convert to an array.
   * @return The converted {@link List}.
   */
  default String[] toArray( final List<String> list ) {
    return list.toArray( new String[ 0 ] );
  }
}
