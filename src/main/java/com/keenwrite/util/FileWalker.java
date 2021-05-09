/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static java.nio.file.FileSystems.getDefault;

/**
 * Responsible for finding files in a file system that match a particular
 * globbing file name pattern.
 *
 * @see ResourceWalker#walk(String, String, Consumer)
 */
public class FileWalker {
  /**
   * Walks the given directory hierarchy for files that match the given
   * globbing file name pattern. This will search to a depth of 10 directories
   * deep (to avoid infinite recursion).
   *
   * @param path Root directory to scan for files matching the glob.
   * @param glob Only files matching the pattern will be consumed.
   * @param c    Function to call for each matching path found.
   * @throws IOException Could not walk the tree.
   */
  public static void walk(
    final Path path, final String glob, final Consumer<Path> c )
    throws IOException {
    final var matcher = getDefault().getPathMatcher( "glob:" + glob );

    try( final var walk = Files.walk( path, 10 ) ) {
      for( final var it = walk.iterator(); it.hasNext(); ) {
        final var p = it.next();
        if( matcher.matches( p ) ) {
          c.accept( p );
        }
      }
    }
  }
}
