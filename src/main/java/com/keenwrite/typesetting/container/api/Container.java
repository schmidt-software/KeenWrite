/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.api;

import com.keenwrite.io.CommandNotFoundException;

public interface Container {

  /**
   * Installs the container software.
   *
   * @throws CommandNotFoundException The container installer was not found.
   */
  void install() throws CommandNotFoundException;

  /**
   * Runs preliminary commands against the container before starting.
   *
   * @throws CommandNotFoundException The container executable was not found
   *                               anywhere in any directory listed in the
   *                               PATH environment variable.
   */
  void start() throws CommandNotFoundException;

  /**
   * Loads an image name into the container.
   *
   * @param name    The name of the image to pull.
   * @param version The image version number.
   */
  void pull( String name, String version )
    throws CommandNotFoundException;

  /**
   * Stops the container.
   */
  void stop();
}
