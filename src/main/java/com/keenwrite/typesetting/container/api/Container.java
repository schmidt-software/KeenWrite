/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.api;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public interface Container {

  /**
   * Downloads the container software into the given directory.
   */
  void download( Path directory );

  /**
   * Installs the container software.
   *
   * @throws FileNotFoundException The container installer was not found.
   */
  void install() throws FileNotFoundException;

  /**
   * Runs preliminary commands against the container before starting.
   *
   * @throws FileNotFoundException The container executable was not found.
   */
  void initialize() throws FileNotFoundException;

  /**
   * Starts the container.
   */
  void start();

  /**
   * Loads the given image into the container.
   *
   * @param imageName The name of the image to load into the container.
   */
  void load( final String imageName );

  /**
   * Stops the container.
   */
  void stop();
}
