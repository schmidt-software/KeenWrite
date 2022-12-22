/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.io;

import java.nio.file.Path;
import java.util.Optional;

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
   * @param filename Filename to query existence as executable.
   */
  public SysFile( final String filename ) {
    super( filename );
  }

  /**
   * Answers whether the path returned from {@link #locate()} is an executable
   * that can be run using a {@link ProcessBuilder}.
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
   * @return The fully qualified {@link Path} to the executable filename
   * provided at construction time.
   */
  public Optional<Path> locate() {
    final var exe = getName();
    final var paths = getenv( "PATH" ).split( quote( pathSeparator ) );

    for( final var path : paths ) {
      final var p = Path.of( path ).resolve( exe );

      for( final var extension : EXTENSIONS ) {
        final var filename = Path.of( p + extension );

        if( isExecutable( filename ) ) {
          return Optional.of( filename );
        }
      }
    }

    return Optional.empty();
  }
}
