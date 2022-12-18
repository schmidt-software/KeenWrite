/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.api;

import java.nio.file.Path;

public interface Container {

  /**
   * Answers whether the container software is installed and runnable.
   *
   * @return {@code false} when the container must be downloaded.
   */
  boolean exists();

  /**
   * Downloads the container software into the given directory.
   */
  void download( Path directory );

  /**
   * Installs the container software.
   */
  void install();

  /**
   * Runs preliminary commands against the container before starting.
   */
  void initialize();

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
