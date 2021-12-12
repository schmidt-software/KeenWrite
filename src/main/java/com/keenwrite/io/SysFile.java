package com.keenwrite.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.lang.System.getenv;
import static java.nio.file.Files.isExecutable;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for file-related functionality.
 */
public class SysFile extends java.io.File {
  /**
   * For finding executable programs.
   */
  private static final String[] EXTENSIONS = new String[]
    {"", ".com", ".exe", ".bat", ".cmd"};

  /**
   * Creates a new instance for a given file name.
   *
   * @param pathname File name to represent for subsequent operations.
   */
  public SysFile( final String pathname ) {
    super( pathname );
  }

  /**
   * For a file name that represents an executable (without an extension)
   * file, this determines whether the executable is found in the PATH
   * environment variable. This will search the PATH each time the method
   * is invoked, triggering a full directory scan for all paths listed in
   * the environment variable. The result is not cached, so avoid calling
   * this in a critical loop.
   *
   * @return {@code true} when the given file name references an executable
   * file located in the PATH environment variable.
   */
  public boolean canRun() {
    final var exe = getName();
    final var paths = getenv( "PATH" ).split( quote( pathSeparator ) );
    return Stream.of( paths ).map( Paths::get ).anyMatch(
      path -> {
        final var p = path.resolve( exe );

        for( final var extension : EXTENSIONS ) {
          if( isExecutable( Path.of( p + extension ) ) ) {
            return true;
          }
        }

        return false;
      }
    );
  }
}
