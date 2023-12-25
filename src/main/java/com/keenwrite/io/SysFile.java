/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.keenwrite.constants.Constants.USER_DIRECTORY;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.WindowsRegistry.pathsWindows;
import static com.keenwrite.util.DataTypeConverter.toHex;
import static com.keenwrite.util.SystemUtils.IS_OS_WINDOWS;
import static java.lang.System.getenv;
import static java.nio.file.Files.isExecutable;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for file-related functionality.
 */
public final class SysFile extends java.io.File {
  /**
   * For finding executable programs. These are used in an O( n^2 ) search,
   * so don't add more entries than necessary.
   */
  private static final String[] EXTENSIONS = new String[]
    { "", ".exe", ".bat", ".cmd", ".msi", ".com" };

  private static final String WHERE_COMMAND =
    IS_OS_WINDOWS ? "where" : "which";

  /**
   * Number of bytes to read at a time when computing this file's checksum.
   */
  private static final int BUFFER_SIZE = 16384;

  /**
   * Creates a new instance for a given file name.
   *
   * @param filename Filename to query existence as executable.
   */
  public SysFile( final String filename ) {
    super( filename );
  }

  /**
   * Creates a new instance for a given {@link File}. This is useful for
   * validating checksums against an existing {@link File} instance that
   * may optionally exist in a directory listed in the PATH environment
   * variable.
   *
   * @param file The file to change into a "system file".
   */
  public SysFile( final File file ) {
    super( file.getAbsolutePath() );
  }

  /**
   * Answers whether an executable can be found that can be run using a
   * {@link ProcessBuilder}.
   *
   * @return {@code true} if the executable is runnable.
   */
  public boolean canRun() {
    return locate().isPresent();
  }

  /**
   * For a file name that represents an executable (without an extension)
   * file, this determines the first matching executable found in the PATH
   * environment variable. This will search the PATH each time the method
   * is invoked, triggering a full directory scan for all paths listed in
   * the environment variable. The result is not cached, so avoid calling
   * this in a critical loop.
   * <p>
   * After installing software, the software might be located in the PATH,
   * but not available to run by its name alone. In such cases, we need the
   * absolute path to the executable to run it. This will always return
   * the fully qualified path, otherwise an empty result.
   *
   * @return Fully qualified path to the executable, if found.
   */
  public Optional<Path> locate() {
    final var dirList = new ArrayList<String>();
    final var paths = pathsSane();
    int began = 0;
    int ended;

    while( (ended = paths.indexOf( pathSeparatorChar, began )) != -1 ) {
      final var dir = paths.substring( began, ended );
      began = ended + 1;

      dirList.add( dir );
    }

    final var dirs = dirList.toArray( new String[]{} );
    var path = locate( dirs, "Wizard.container.executable.path" );

    if( path.isEmpty() ) {
      clue();

      try {
        path = where();
      } catch( final IOException ex ) {
        clue( "Wizard.container.executable.which", ex );
      }
    }

    return path.isPresent()
      ? path
      : locate( System::getenv,
                IS_OS_WINDOWS
                  ? "Wizard.container.executable.registry"
                  : "Wizard.container.executable.path" );
  }

  private Optional<Path> locate( final String[] dirs, final String msg ) {
    final var exe = getName();

    for( final var dir : dirs ) {
      Path p;

      try {
        p = Path.of( dir ).resolve( exe );
      } catch( final Exception ex ) {
        clue( ex );
        continue;
      }

      for( final var extension : EXTENSIONS ) {
        final var filename = Path.of( p + extension );

        if( isExecutable( filename ) ) {
          return Optional.of( filename );
        }
      }
    }

    clue( msg );
    return Optional.empty();
  }

  private Optional<Path> locate(
    final Function<String, String> map, final String msg ) {
    final var paths = paths( map ).split( quote( pathSeparator ) );

    return locate( paths, msg );
  }

  /**
   * Runs {@code where} or {@code which} to determine the fully qualified path
   * to an executable.
   *
   * @return The path to the executable for this file, if found.
   * @throws IOException Could not determine the location of the command.
   */
  public Optional<Path> where() throws IOException {
    // The "where" command on Windows will automatically add the extension.
    final var args = new String[]{ WHERE_COMMAND, getName() };
    final var output = run( _ -> true, args );
    final var result = output.lines().findFirst();

    return result.map( Path::of );
  }

  /**
   * Changes to the PATH environment variable aren't reflected for the
   * currently running task. The registry, however, contains the updated
   * value. Reading the registry is a hack.
   *
   * @param map The mapping function of registry variable names to values.
   * @return The revised PATH variables as stored in the registry.
   */
  private static String paths( final Function<String, String> map ) {
    return IS_OS_WINDOWS ? pathsWindows( map ) : pathsSane();
  }

  /**
   * Answers whether this file's SHA-256 checksum equals the given
   * hexadecimal-encoded checksum string.
   *
   * @param hex The string to compare against the checksum for this file.
   * @return {@code true} if the checksums match; {@code false} on any
   * error or checksums don't match.
   */
  public boolean isChecksum( final String hex ) {
    assert hex != null;

    try {
      return checksum( "SHA-256" ).equalsIgnoreCase( hex );
    } catch( final Exception ex ) {
      return false;
    }
  }

  /**
   * Returns the hash code for this file.
   *
   * @return The hex-encoded hash code for the file contents.
   */
  @SuppressWarnings( "SameParameterValue" )
  private String checksum( final String algorithm )
    throws NoSuchAlgorithmException, IOException {
    final var digest = MessageDigest.getInstance( algorithm );

    try( final var in = new FileInputStream( this ) ) {
      final var bytes = new byte[ BUFFER_SIZE ];
      int count;

      while( (count = in.read( bytes )) != -1 ) {
        digest.update( bytes, 0, count );
      }

      return toHex( digest.digest() );
    }
  }

  /**
   * Runs a command and collects standard output into a buffer.
   *
   * @param filter Provides an injected test to determine whether the line
   *               read from the command's standard output is to be added to
   *               the result buffer.
   * @param args   The command and its arguments to run.
   * @return The standard output from the command, filtered.
   * @throws IOException Could not run the command.
   */
  @NotNull
  public static String run(
    final Predicate<String> filter,
    final String[] args ) throws IOException {
    final var process = Runtime.getRuntime().exec( args );
    final var stream = process.getInputStream();
    final var stdout = new StringBuffer( 2048 );

    StreamGobbler.gobble( stream, text -> {
      if( filter.test( text ) ) {
        stdout.append( WindowsRegistry.parseRegEntry( text ) );
      }
    } );

    try {
      process.waitFor();
    } catch( final InterruptedException ex ) {
      throw new IOException( ex );
    } finally {
      process.destroy();
    }

    return stdout.toString();
  }

  /**
   * Provides {@code null}-safe machinery to get a file name.
   *
   * @param p The path to the file name to retrieve (may be {@code null}).
   * @return The file name or the empty string if the path is not found.
   */
  public static String getFileName( final Path p ) {
    return p == null ? "" : getPathFileName( p );
  }

  /**
   * If the path doesn't exist right before typesetting, switch the path
   * to the user's home directory to increase the odds of the typesetter
   * succeeding. This could help, for example, if the images directory was
   * deleted or moved.
   *
   * @param path The path to verify existence, may be null.
   * @return The given path, if it exists, otherwise the user's home directory.
   */
  public static Path normalize( final Path path ) {
    return path == null
      ? USER_DIRECTORY.toPath()
      : path.toFile().exists()
      ? path
      : USER_DIRECTORY.toPath();
  }

  public static File toFile( final Path path ) {
    return path == null
      ? USER_DIRECTORY
      : path.toFile();
  }

  private static String pathsSane() {
    return getenv( "PATH" );
  }

  private static String getPathFileName( final Path p ) {
    assert p != null;

    final var f = p.getFileName();

    return f == null ? "" : f.toString();
  }
}
