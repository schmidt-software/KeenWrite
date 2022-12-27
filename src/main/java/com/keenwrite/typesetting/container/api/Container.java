/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.api;

import com.keenwrite.io.CommandNotFoundException;

import java.io.File;
import java.io.IOException;

public interface Container {
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
   * Loads a named image into the container.
   *
   * @param name The full container name of the image to pull.
   * @throws CommandNotFoundException The container executable was not found.
   */
  void pull( String name ) throws CommandNotFoundException;

  /**
   * Runs a command inside the container.
   *
   * @param args The command and arguments to run.
   * @return The exit code returned by the installer program.
   * @throws CommandNotFoundException The container executable was not found.
   */
  int run( final String... args ) throws CommandNotFoundException;
}
