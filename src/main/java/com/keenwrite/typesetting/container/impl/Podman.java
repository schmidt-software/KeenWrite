/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.container.impl;

import com.keenwrite.io.SysFile;
import com.keenwrite.typesetting.container.api.Container;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class Podman implements Container {
  public static final SysFile CONTAINER = new SysFile( "podman" );

  @Override
  public void download( final Path directory ) {
  }

  @Override
  public void install() throws FileNotFoundException {
  }

  @Override
  public void initialize() throws FileNotFoundException {
    if( !CONTAINER.canRun() ) {
      throw new FileNotFoundException( CONTAINER.getAbsolutePath() );
    }

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
