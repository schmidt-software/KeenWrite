/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting.containerization;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Implementations receive an {@link InputStream} for reading, which happens
 * on a separate thread. Implementations are responsible for starting the
 * thread. This class helps avoid relying on {@link PipedInputStream} and
 * {@link PipedOutputStream} to connect the {@link InputStream} from an
 * instance of {@link ProcessBuilder} to process standard output and standard
 * error for a running command.
 */
@FunctionalInterface
public interface StreamProcessor {
  /**
   * Processes the given {@link InputStream} on a separate thread.
   */
  void start( InputStream in );
}
