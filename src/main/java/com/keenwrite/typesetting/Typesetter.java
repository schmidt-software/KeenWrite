/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.File;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_ENV;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_PATH;
import static java.lang.ProcessBuilder.Redirect.DISCARD;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
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
   * @param in  The input document to typeset.
   * @param out Path to the finished typeset document.
   */
  public void typeset( final Path in, final Path out )
    throws Exception {
    if( TYPESETTER.canRun() ) {
      clue( get( "Main.status.typeset.began", out ) );
      final var task = new TypesetTask( in, out );
      final var time = currentTimeMillis();
      final var success = task.typeset();

      clue( get(
        "Main.status.typeset.ended." + (success ? "success" : "failure"),
        out, since( time ) )
      );
    }
  }

  /**
   * Launches a task to typeset a document.
   */
  private class TypesetTask implements Callable<Boolean> {
    private final List<String> mArgs = new ArrayList<>();
    private final Path mInput;
    private final Path mOutput;

    /**
     * Working directory must be set because ConTeXt cannot write the
     * result to an arbitrary location.
     */
    private final Path mDirectory;

    private TypesetTask( final Path input, final Path output ) {
      assert input != null;
      assert output != null;

      final var parentDir = output.getParent();
      mInput = input;
      mOutput = output;
      mDirectory = (parentDir == null ? DEFAULT_DIRECTORY : parentDir);
    }

    /**
     * Initializes ConTeXt, which means creating the cache directory if it
     * doesn't already exist.
     *
     * @return {@code true} if the cache directory exists.
     */
    private boolean reinitialize() {
      final var filename = mOutput.getFileName();
      final var paths = getProperty( KEY_TYPESET_CONTEXT_PATH );
      final var envs = getProperty( KEY_TYPESET_CONTEXT_ENV );
      final var exists = getCacheDir().exists();

      // Ensure invoking multiple times will load the correct arguments.
      mArgs.clear();
      mArgs.add( TYPESETTER.getName() );

      if( exists ) {
        mArgs.add( "--autogenerate" );
        mArgs.add( "--script" );
        mArgs.add( "mtx-context" );
        mArgs.add( "--batchmode" );
        mArgs.add( "--purgeall" );
        mArgs.add( "--path='" + paths + "'" );
        mArgs.add( "--environment='" + envs + "'" );
        mArgs.add( "--result='" + filename + "'" );
        mArgs.add( mInput.toString() );
      }
      else {
        mArgs.add( "--generate" );
      }

      return exists;
    }

    /**
     * Setting {@code TEXMFCACHE} when run on a fresh system fails on first
     * run. If the cache directory doesn't exist, attempt to create it, then
     * call ConTeXt to generate the PDF.
     *
     * @return {@code true} if the document was typeset successfully.
     * @throws Exception If the typesetter could not be invoked.
     */
    private boolean typeset() throws Exception {
      return reinitialize() ? call() : call() && reinitialize() && call();
    }

    @Override
    public Boolean call() throws Exception {
      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );

      final var cacheDir = getCacheDir();
      final var env = builder.environment();
      env.put( "TEXMFCACHE", cacheDir.toString() );

      // Without redirecting (or draining) the output, the command will not
      // terminate successfully.
      builder.redirectOutput( DISCARD );
      builder.redirectError( DISCARD );

      final var process = builder.start();
      process.waitFor();
      final var exit = process.exitValue();
      process.destroy();

      // Exit value for a successful invocation of the typesetter. This value
      // value is returned when creating the cache on the first run as well as
      // creating PDFs on subsequent runs (after the cache has been created).
      // Users don't care about exit codes, only whether the PDF was generated.
      return exit == 0;
    }

    /**
     * Returns the location of the cache directory.
     *
     * @return A fully qualified path to the location to store temporary
     * files between typesetting runs.
     */
    private java.io.File getCacheDir() {
      final var temp = System.getProperty( "java.io.tmpdir" );
      final var cache = Path.of( temp, "luatex-cache" );
      return cache.toFile();
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
