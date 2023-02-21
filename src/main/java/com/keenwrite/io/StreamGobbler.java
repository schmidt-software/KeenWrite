package com.keenwrite.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Consumes the standard output of a {@link Process} created from a
 * {@link ProcessBuilder}. Directs the output to a {@link Consumer} of
 * strings. This will run on its own thread and close the stream when
 * no more data can be processed.
 * <p>
 * <strong>Warning:</strong> Do not use this with binary data, it is only
 * meant for text streams, such as standard out from running command-line
 * applications.
 * </p>
 */
public class StreamGobbler implements Callable<Boolean> {
  private final InputStream mInput;
  private final Consumer<String> mConsumer;

  /**
   * Constructs a new instance of {@link StreamGobbler} that is capable of
   * reading an {@link InputStream} and passing each line of textual data from
   * that stream over to a string {@link Consumer}.
   *
   * @param input    The stream having input to pass to the consumer.
   * @param consumer The {@link Consumer} that receives each line.
   */
  private StreamGobbler(
    final InputStream input,
    final Consumer<String> consumer ) {
    assert input != null;
    assert consumer != null;

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
    try( final var input = new InputStreamReader( mInput, UTF_8 );
         final var buffer = new BufferedReader( input ) ) {
      buffer.lines().forEach( mConsumer );
    }

    return Boolean.TRUE;
  }

  /**
   * Reads the given {@link InputStream} on a separate thread and passes
   * each line of text input to the given {@link Consumer}.
   *
   * @param inputStream The stream having input to pass to the consumer.
   * @param consumer    The {@link Consumer} that receives each line.
   */
  public static void gobble(
    final InputStream inputStream, final Consumer<String> consumer ) {
    try( final var executor = newFixedThreadPool( 1 ) ) {
      executor.submit( new StreamGobbler( inputStream, consumer ) );
    }
  }
}
