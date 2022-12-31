/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.collections.BoundedCache;
import com.keenwrite.io.SysFile;
import com.keenwrite.util.Time;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.Time.toElapsedTime;
import static java.lang.ProcessBuilder.Redirect.DISCARD;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.nio.file.Files.*;
import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 * This uses a version of the typesetter installed on the host system.
 */
public final class HostTypesetter extends Typesetter implements Callable<Void> {
  private static final SysFile TYPESETTER = new SysFile( TYPESETTER_EXE );

  HostTypesetter( final Mutator mutator ) {
    super( mutator );
  }

  /**
   * Answers whether the typesetting software is installed locally.
   *
   * @return {@code true} if the typesetting software is installed on the host.
   */
  public static boolean isReady() {
    return TYPESETTER.canRun();
  }

  /**
   * Calculates the time that has elapsed from the current time to the
   * given moment in time.
   *
   * @param start The starting time, which should be before the current time.
   * @return A human-readable formatted time.
   * @see Time#toElapsedTime(Duration)
   */
  private static String since( final long start ) {
    return toElapsedTime( ofMillis( currentTimeMillis() - start ) );
  }

  /**
   * Launches a task to typeset a document.
   */
  private class TypesetTask implements Callable<Boolean> {
    private final List<String> mArgs = new ArrayList<>();

    /**
     * Working directory must be set because ConTeXt cannot write the
     * result to an arbitrary location.
     */
    private final Path mDirectory;

    private TypesetTask() {
      final var parentDir = getOutputPath().getParent();
      mDirectory = parentDir == null ? DEFAULT_DIRECTORY : parentDir;
    }

    /**
     * Initializes ConTeXt, which means creating the cache directory if it
     * doesn't already exist. The theme entry point must be named 'main.tex'.
     *
     * @return {@code true} if the cache directory exists.
     */
    private boolean reinitialize() {
      final var filename = getOutputPath().getFileName();
      final var theme = getThemePath();
      final var cacheExists = !isEmpty( getCacheDir().toPath() );

      // Ensure invoking multiple times will load the correct arguments.
      mArgs.clear();
      mArgs.add( TYPESETTER_EXE );

      if( cacheExists ) {
        mArgs.add( "--autogenerate" );
        mArgs.add( "--script" );
        mArgs.add( "mtx-context" );
        mArgs.add( "--batchmode" );
        mArgs.add( "--nonstopmode" );
        mArgs.add( "--purgeall" );
        mArgs.add( "--path='" + theme + "'" );
        mArgs.add( "--environment='main'" );
        mArgs.add( "--result='" + filename + "'" );
        mArgs.add( getInputPath().toString() );

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
     * Setting {@code TEXMFCACHE} when run on a fresh system fails on the first
     * try. If the cache directory doesn't exist, attempt to create it, then
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
      final var stdout = new BoundedCache<String, String>( 150 );
      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );
      builder.environment().put( "TEXMFCACHE", getCacheDir().toString() );

      // Without redirecting (or draining) stderr, the command may not
      // terminate successfully.
      builder.redirectError( DISCARD );

      final var process = builder.start();
      final var stream = process.getInputStream();

      // Reading from stdout allows slurping page numbers while generating.
      final var listener = new PaginationListener( stream, stdout );
      listener.start();

      // Even though the process has completed, there may be incomplete I/O.
      process.waitFor();

      // Allow time for any incomplete I/O to take place.
      process.waitFor( 1, SECONDS );

      final var exit = process.exitValue();
      process.destroy();

      // If there was an error, the typesetter will leave behind log, pdf, and
      // error files.
      if( exit > 0 ) {
        final var xmlName = getInputPath().getFileName().toString();
        final var srcName = getOutputPath().getFileName().toString();
        final var logName = newExtension( xmlName, ".log" );
        final var errName = newExtension( xmlName, "-error.log" );
        final var pdfName = newExtension( xmlName, ".pdf" );
        final var tuaName = newExtension( xmlName, ".tua" );
        final var badName = newExtension( srcName, ".log" );

        log( badName );
        log( logName );
        log( errName );
        log( stdout.keySet().stream().toList() );

        // Users may opt to keep these files around for debugging purposes.
        if( autoclean() ) {
          deleteIfExists( logName );
          deleteIfExists( errName );
          deleteIfExists( pdfName );
          deleteIfExists( badName );
          deleteIfExists( tuaName );
        }
      }

      // Exit value for a successful invocation of the typesetter. This value
      // is returned when creating the cache on the first run as well as
      // creating PDFs on subsequent runs (after the cache has been created).
      // Users don't care about exit codes, only whether the PDF was generated.
      return exit == 0;
    }

    private Path newExtension( final String baseName, final String ext ) {
      final var path = getOutputPath();
      return path.resolveSibling( removeExtension( baseName ) + ext );
    }

    /**
     * Fires a status message for each line in the given file. The file format
     * is somewhat machine-readable, but no effort beyond line splitting is
     * made to parse the text.
     *
     * @param path Path to the file containing error messages.
     */
    private void log( final Path path ) throws IOException {
      if( exists( path ) ) {
        log( readAllLines( path ) );
      }
    }

    private void log( final List<String> lines ) {
      final var splits = new ArrayList<String>( lines.size() * 2 );

      for( final var line : lines ) {
        splits.addAll( asList( line.split( "\\\\n" ) ) );
      }

      clue( splits );
    }

    /**
     * Returns the location of the cache directory.
     *
     * @return A fully qualified path to the location to store temporary
     * files between typesetting runs.
     */
    private java.io.File getCacheDir() {
      final var temp = getProperty( "java.io.tmpdir" );
      final var cache = Path.of( temp, "luatex-cache" );
      return cache.toFile();
    }

    /**
     * Answers whether the given directory is empty. The typesetting software
     * creates a non-empty directory by default. The return value from this
     * method is a proxy to answering whether the typesetter has been run for
     * the first time or not.
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
   * The lines are parsed; the first number is displayed as a status bar
   * message.
   * </p>
   */
  private static class PaginationListener extends Thread {
    private static final Pattern DIGITS = Pattern.compile( "\\D+" );

    private final InputStream mInputStream;

    private final Map<String, String> mCache;

    public PaginationListener(
      final InputStream in, final Map<String, String> cache ) {
      mInputStream = in;
      mCache = cache;
    }

    public void run() {
      try( final var reader = createReader( mInputStream ) ) {
        int pageCount = 1;
        int passCount = 1;
        int pageTotal = 0;
        String line;

        while( (line = reader.readLine()) != null ) {
          mCache.put( line, "" );

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

            // Inform the user of pages being typeset.
            clue( "Main.status.typeset.page",
                  pageCount, pageTotal < 1 ? "?" : pageTotal, passCount
            );
          }
        }
      } catch( final IOException ex ) {
        clue( ex );
        throw new RuntimeException( ex );
      }
    }

    private BufferedReader createReader( final InputStream inputStream ) {
      return new BufferedReader( new InputStreamReader( inputStream ) );
    }
  }

  /**
   * This will typeset the document using a new process. The return value only
   * indicates whether the typesetter exists, not whether the typesetting was
   * successful. The typesetter must be known to exist prior to calling this
   * method.
   *
   * @throws IOException                 If the process could not be started.
   * @throws InterruptedException        If the process was killed.
   * @throws TypesetterNotFoundException When no typesetter is along the PATH.
   */
  @Override
  public Void call()
    throws IOException, InterruptedException, TypesetterNotFoundException {
    final var outputPath = getOutputPath();
    final var prefix = "Main.status.typeset";

    clue( prefix + ".began", outputPath );

    final var task = new TypesetTask();
    final var time = currentTimeMillis();
    final var success = task.typeset();
    final var suffix = success ? ".success" : ".failure";

    clue( prefix + ".ended" + suffix, outputPath, since( time ) );

    return null;
  }
}
