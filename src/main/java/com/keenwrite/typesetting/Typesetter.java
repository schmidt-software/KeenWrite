/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.File;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;
import javafx.concurrent.Task;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_ENV;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_PATH;
import static java.lang.Long.MAX_VALUE;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.*;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 */
public class Typesetter {
  private static final File TYPESETTER = new File( "mtxrun" );

  private final Workspace mWorkspace;

  /**
   * Creates a new {@link Typesetter} instance capable of configuring the
   * typesetter used to generate a typeset document.
   */
  public Typesetter( final Workspace workspace ) {
    mWorkspace = workspace;
  }

  /**
   * This will typeset the document using a new process.
   *
   * @param input  The input document to typeset.
   * @param output Path to the finished typeset document.
   */
  public void typeset( final Path input, final Path output )
    throws Exception {
    if( TYPESETTER.canRun() ) {
      final var executor = newFixedThreadPool( 5 );
      final var task = new TypesetTask( input, output );
      final var elapsed = currentTimeMillis();

      task.setOnRunning(
        e -> clue( get(
          "Main.status.typeset.began", output
        ) )
      );

      task.setOnSucceeded(
        e -> clue( get(
          "Main.status.typeset.ended.success", output, since( elapsed )
        ) )
      );

      task.setOnFailed(
        e -> clue( get(
          "Main.status.typeset.ended.failure",
          output, since( elapsed ), task.getValue()
        ) )
      );

      executor.execute( task );
      executor.shutdown();
      if( !executor.awaitTermination( MAX_VALUE, NANOSECONDS ) ) {
        throw new TimeoutException();
      }
    }
  }

  /**
   * Launches a task to typeset a document.
   */
  private class TypesetTask extends Task<Integer> {
    private final List<String> mArgs = new ArrayList<>();

    /**
     * Working directory must be set because ConTeXt cannot write the
     * result to an arbitrary location.
     */
    private final Path mDirectory;

    public TypesetTask( final Path input, final Path output ) {
      final var filename = output.getFileName();
      final var parentDir = output.getParent();
      mDirectory = (parentDir == null ? DEFAULT_DIRECTORY : parentDir);

      final var paths = getProperty( KEY_TYPESET_CONTEXT_PATH );
      final var envs = getProperty( KEY_TYPESET_CONTEXT_ENV );

      mArgs.add( TYPESETTER.getName() );
      mArgs.add( "--autogenerate" );
      mArgs.add( "--script" );
      mArgs.add( "mtx-context" );
      mArgs.add( "--batchmode" );
      mArgs.add( "--purgeall" );
      mArgs.add( "--path='" + paths + "'" );
      mArgs.add( "--environment='" + envs + "'" );
      mArgs.add( "--result='" + filename + "'" );
      mArgs.add( input.toString() );
    }

    @Override
    public Integer call() throws Exception {
      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );

      final var env = builder.environment();
      env.put( "TEXMFCACHE", System.getProperty( "java.io.tmpdir" ) );

      //final var process = builder.inheritIO().start();
      final var process = builder.start();
      process.waitFor();
      final int exit = process.exitValue();
      process.destroy();
      return exit;
    }
  }

  private String getProperty( final Key key ) {
    return mWorkspace.stringProperty( key ).get();
  }

  /**
   * Calculates the time that has elapsed from the current time to the
   * given moment in time.
   *
   * @param start The starting time, which really should be before the
   *              current time.
   * @return A human-readable formatted time.
   * @see #asElapsed(long)
   */
  private static String since( final long start ) {
    return asElapsed( currentTimeMillis() - start );
  }

  /**
   * Converts an elapsed time to a human-readable format (hours, minutes,
   * seconds, and milliseconds).
   *
   * @param elapsed An elapsed time, in milliseconds.
   * @return Human-readable elapsed time.
   */
  private static String asElapsed( final long elapsed ) {
    final var hours = MILLISECONDS.toHours( elapsed );
    final var eHours = elapsed - HOURS.toMillis( hours );
    final var minutes = MILLISECONDS.toMinutes( eHours );
    final var eMinutes = eHours - MINUTES.toMillis( minutes );
    final var seconds = MILLISECONDS.toSeconds( eMinutes );
    final var eSeconds = eMinutes - SECONDS.toMillis( seconds );
    final var milliseconds = MILLISECONDS.toMillis( eSeconds );

    return format( "%02d:%02d:%02d.%03d",
                   hours, minutes, seconds, milliseconds );
  }
}
