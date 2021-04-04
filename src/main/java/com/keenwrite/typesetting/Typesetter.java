/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.File;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.keenwrite.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.Messages.get;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_ENV;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_PATH;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.*;

/**
 * Represents the executable responsible for typesetting text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 */
public class Typesetter {
  private static final File TYPESETTER = new File( "context");

  private static final ExecutorService sService = newFixedThreadPool( 5 );

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
    throws IOException {
    if( TYPESETTER.canRun() ) {
      sService.submit( new TypesetTask( input, output ) );
    }
  }

  /**
   * Launches a task to typeset a document.
   */
  public class TypesetTask implements Callable<Integer> {
    private final List<String> mArgs = new ArrayList<>();

    /**
     * Working directory must be set because ConTeXt cannot write the
     * result to an arbitrary location.
     */
    private final Path mDirectory;

    /**
     * Fully qualified destination file name.
     */
    private final Path mOutput;

    public TypesetTask( final Path input, final Path output ) {
      final var filename = output.getFileName();
      final var parentDir = output.getParent();
      mDirectory = (parentDir == null ? DEFAULT_DIRECTORY : parentDir);
      mOutput = output;

      final var paths = getProperty( KEY_TYPESET_CONTEXT_PATH );
      final var envs = getProperty( KEY_TYPESET_CONTEXT_ENV );

      mArgs.add( TYPESETTER.getName() );
      mArgs.add( "--batchmode" );
      mArgs.add( "--purgeall" );
      mArgs.add( "--path='" + paths + "'" );
      mArgs.add( "--environment='" + envs + "'" );
      mArgs.add( "--result='" + filename + "'" );
      mArgs.add( input.toString() );
    }

    @Override
    public Integer call() throws Exception {
      final var elapsed = currentTimeMillis();
      final var output = mOutput.toString();
      clue( get( "Main.status.typeset.began", output ) );

      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );
      final var process = builder.start();
      process.waitFor();

      final var code = process.exitValue();
      final var time = asElapsed( currentTimeMillis() - elapsed );
      clue(
        code == 0
          ? get( "Main.status.typeset.ended.success", output, time )
          : get( "Main.status.typeset.ended.failure", output, time, code )
      );

      return code;
    }
  }

  private String getProperty( final Key key ) {
    return mWorkspace.stringProperty( key ).get();
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
