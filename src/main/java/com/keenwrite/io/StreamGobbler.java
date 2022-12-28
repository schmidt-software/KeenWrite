package com.keenwrite.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Consumes the standard output of a {@link Process} created from a
 * {@link ProcessBuilder}. Directs the output to a {@link Consumer} of
 * strings.
 */
public class StreamGobbler implements Callable<Boolean> {
  private final InputStream mInput;
  private final Consumer<String> mConsumer;

  public StreamGobbler(
    final InputStream input,
    final Consumer<String> consumer ) {
    mInput = input;
    mConsumer = consumer;
  }

  /**
   * Consumes the input until no more data is available. Closes the stream.
   *
   * @return {@link Boolean#TRUE} always.
   * @throws IOException Could not read from the stream.
   */
  @Override
  public Boolean call() throws IOException {
    try( final var input = new InputStreamReader( mInput );
         final var buffer = new BufferedReader( input ) ) {
      buffer.lines().forEach( mConsumer );
    }

    return Boolean.TRUE;
  }
}
