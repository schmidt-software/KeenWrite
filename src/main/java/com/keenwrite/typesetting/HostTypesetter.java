/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.typesetting;

import com.keenwrite.collections.CircularQueue;
import com.keenwrite.io.StreamGobbler;
import com.keenwrite.io.SysFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static com.keenwrite.constants.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static java.lang.ProcessBuilder.Redirect.DISCARD;
import static java.lang.System.getProperty;
import static java.nio.file.Files.*;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Responsible for invoking an executable to typeset text. This will
 * construct suitable command-line arguments to invoke the typesetting engine.
 * This uses a version of the typesetter installed on the host system.
 */
public final class HostTypesetter extends Typesetter
  implements Callable<Boolean> {
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
      final var parentDir = getTargetPath().getParent();
      mDirectory = parentDir == null ? DEFAULT_DIRECTORY : parentDir;
    }

    /**
     * Initializes ConTeXt, which means creating the cache directory if it
     * doesn't already exist. The theme entry point must be named 'main.tex'.
     *
     * @return {@code true} if the cache directory exists.
     */
    private boolean reinitialize() {
      final var cacheExists = !isEmpty( getCacheDir().toPath() );

      // Ensure invoking multiple times will load the correct arguments.
      mArgs.clear();
      mArgs.add( TYPESETTER_EXE );

      if( cacheExists ) {
        mArgs.addAll( options() );

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
      final var stdout = new CircularQueue<String>( 150 );
      final var builder = new ProcessBuilder( mArgs );
      builder.directory( mDirectory.toFile() );
      builder.environment().put( "TEXMFCACHE", getCacheDir().toString() );

      // Without redirecting (or draining) stderr, the command may not
      // terminate successfully.
      builder.redirectError( DISCARD );

      final var process = builder.start();
      final var listener = new PaginationListener();

      // Slurp page numbers in a separate thread while typesetting.
      StreamGobbler.gobble( process.getInputStream(), line -> {
        listener.accept( line );
        stdout.add( line );
      } );

      // Even though the process has completed, there may be incomplete I/O.
      process.waitFor();

      // Allow time for any incomplete I/O to take place.
      process.waitFor( 1, SECONDS );

      final var exit = process.exitValue();
      process.destroy();

      // If there was an error, the typesetter will leave behind log, pdf, and
      // error files.
      if( exit > 0 ) {
        final var xmlName = SysFile.getFileName( getSourcePath() );
        final var srcName = SysFile.getFileName( getTargetPath() );
        final var logName = newExtension( xmlName, ".log" );
        final var errName = newExtension( xmlName, "-error.log" );
        final var pdfName = newExtension( xmlName, ".pdf" );
        final var tuaName = newExtension( xmlName, ".tua" );
        final var badName = newExtension( srcName, ".log" );

        log( badName );
        log( logName );
        log( errName );
        log( stdout.stream().toList() );

        // Users may opt to keep these files around for debugging purposes.
        if( autoRemove() ) {
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
      final var path = getTargetPath();
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
    @SuppressWarnings( "SpellCheckingInspection" )
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
  public Boolean call()
    throws IOException, InterruptedException, TypesetterNotFoundException {
    final var task = new HostTypesetter.TypesetTask();
    return task.typeset();
  }
}
