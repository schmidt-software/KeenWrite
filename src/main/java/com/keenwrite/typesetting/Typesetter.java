/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.io.SysFile;
import com.keenwrite.preferences.Workspace;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_THEMES_PATH;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_TYPESET_CONTEXT_THEME_SELECTION;
import static java.lang.ProcessBuilder.Redirect.DISCARD;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.concurrent.TimeUnit.*;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 */
public class Typesetter {
  private static final SysFile TYPESETTER = new SysFile( "mtxrun" );

  private final Workspace mWorkspace;

  /**
   * Creates a new {@link Typesetter} instance capable of configuring the
   * typesetter used to generate a typeset document.
   */
  public Typesetter( final Workspace workspace ) {
    mWorkspace = workspace;
  }

  public static boolean canRun() {
    return TYPESETTER.canRun();
  }

  /**
   * This will typeset the document using a new process. The return value only
   * indicates whether the typesetter exists, not whether the typesetting was
   * successful.
   *
   * @param in  The input document to typeset.
   * @param out Path to the finished typeset document.
   * @throws IOException                 If the process could not be started.
   * @throws InterruptedException        If the process was killed.
   * @throws TypesetterNotFoundException When no typesetter is along the PATH.
   */
  public void typeset( final Path in, final Path out )
    throws IOException, InterruptedException, TypesetterNotFoundException {
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
    else {
      throw new TypesetterNotFoundException( TYPESETTER.toString() );
    }
  }

  /**
   * Calculates the time that has elapsed from the current time to the
   * given moment in time.
   *
   * @param start The starting time, which should be before the current time.
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
      mDirectory = parentDir == null ? DEFAULT_DIRECTORY : parentDir;
    }

    /**
     * Initializes ConTeXt, which means creating the cache directory if it
     * doesn't already exist. The theme entry point must be named 'main.tex'.
     *
     * @return {@code true} if the cache directory exists.
     */
    private boolean reinitialize() {
      final var filename = mOutput.getFileName();
      final var themes = getThemesPath();
      final var theme = getThemesSelection();
      final var cacheExists = !isEmpty( getCacheDir().toPath() );

      // Ensure invoking multiple times will load the correct arguments.
      mArgs.clear();
      mArgs.add( TYPESETTER.getName() );

      if( cacheExists ) {
        mArgs.add( "--autogenerate" );
        mArgs.add( "--script" );
        mArgs.add( "mtx-context" );
        mArgs.add( "--batchmode" );
        mArgs.add( "--purgeall" );
        mArgs.add( "--path='" + Path.of( themes.toString(), theme ) + "'" );
        mArgs.add( "--environment='main'" );
        mArgs.add( "--result='" + filename + "'" );
        mArgs.add( mInput.toString() );

        final var sb = new StringBuilder( 128 );
        mArgs.forEach( arg -> sb.append( arg ).append( " " ) );
        clue( sb.toString() );
      }
      else {
        mArgs.add( "--generate" );
      }

      return cacheExists;
    }

    /**
     * Setting {@code TEXMFCACHE} when run on a fresh system fails on first
     * run. If the cache directory doesn't exist, attempt to create it, then
     * call ConTeXt to generate the PDF. This is brittle because if the
     * directory is empty, or not populated with cached data, a false positive
     * will be returned, resulting in no PDF being created.
     *
     * @return {@code true} if the document was typeset successfully.
     * @throws IOException          If the process could not be started.
     * @throws InterruptedException If the process was killed.
     */
    private boolean typeset() throws IOException, InterruptedException {
      return reinitialize() ? call() : call() && reinitialize() && call();
    }

    @Override
    public Boolean call() throws IOException, InterruptedException {
      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );
      builder.environment().put( "TEXMFCACHE", getCacheDir().toString() );

      // Without redirecting (or draining) stderr, the command may not
      // terminate successfully.
      builder.redirectError( DISCARD );

      final var process = builder.start();

      // Reading from stdout allows slurping page numbers while generating.
      final var listener = new PaginationListener( process.getInputStream() );
      listener.start();

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

    /**
     * Answers whether the given directory is empty.
     *
     * @param path The directory to check for emptiness.
     * @return {@code true} if the directory is empty.
     */
    private boolean isEmpty( final Path path ) {
      try( final var stream = newDirectoryStream( path ) ) {
        return !stream.iterator().hasNext();
      } catch( final NoSuchFileException | FileNotFoundException ex ) {
        // A missing directory means it doesn't exist, ergo is empty.
        return true;
      } catch( final IOException ex ) {
        throw new RuntimeException( ex );
      }
    }
  }

  /**
   * Responsible for parsing the output from the typesetting engine and
   * updating the status bar to provide assurance that typesetting is
   * executing.
   *
   * <p>
   * Example lines written to standard output:
   * </p>
   * <pre>{@code
   * pages           > flushing realpage 15, userpage 15, subpage 15
   * pages           > flushing realpage 16, userpage 16, subpage 16
   * pages           > flushing realpage 1, userpage 1, subpage 1
   * pages           > flushing realpage 2, userpage 2, subpage 2
   * }</pre>
   * <p>
   * The lines are parsed; the first number is displayed in a status bar
   * message.
   * </p>
   */
  private static class PaginationListener extends Thread {
    private static final Pattern DIGITS = Pattern.compile( "[^\\d]+" );

    private final InputStream mInputStream;

    public PaginationListener( final InputStream in ) {
      mInputStream = in;
    }

    @Override
    public void run() {
      try( final var reader = createReader() ) {
        int pageCount = 1;
        int passCount = 1;
        int pageTotal = 0;
        String line;

        while( (line = reader.readLine()) != null ) {
          if( line.startsWith( "pages" ) ) {
            // The bottleneck will be the typesetting engine writing to stdout,
            // not the parsing of stdout.
            final var scanner = new Scanner( line ).useDelimiter( DIGITS );
            final var digits = scanner.next();
            final var page = Integer.parseInt( digits );

            // If the page number is less than the previous page count, it
            // means that the typesetting engine has started another pass.
            if( page < pageCount ) {
              passCount++;
              pageTotal = pageCount;
            }

            pageCount = page;

            // Let the user know that something is happening in the background.
            clue( get(
              "Main.status.typeset.page",
              pageCount, pageTotal < 1 ? "?" : pageTotal, passCount
            ) );
          }
        }
      } catch( final IOException ex ) {
        throw new RuntimeException( ex );
      }
    }

    private BufferedReader createReader() {
      return new BufferedReader( new InputStreamReader( mInputStream ) );
    }
  }

  private File getThemesPath() {
    return mWorkspace.toFile( KEY_TYPESET_CONTEXT_THEMES_PATH );
  }

  private String getThemesSelection() {
    return mWorkspace.toString( KEY_TYPESET_CONTEXT_THEME_SELECTION );
  }
}
