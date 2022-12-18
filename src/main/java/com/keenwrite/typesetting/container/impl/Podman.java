/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.impl;

import com.keenwrite.typesetting.container.api.Container;

import java.nio.file.Path;

public class Podman implements Container {
  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public void download( final Path directory ) {

  }

  @Override
  public void install() {

  }

  @Override
  public void initialize() {

  }

  @Override
  public void start() {

  }

  @Override
  public void load( final String imageName ) {

  }

  @Override
  public void stop() {

  }
}
