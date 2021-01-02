/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;

/**
 * Responsible for finding file resources.
 */
public class ResourceWalker {
  /**
   * Globbing pattern to match font names.
   */
  public static final String GLOB_FONTS = "**.{ttf,otf}";

  /**
   * @param directory The root directory to scan for files matching the glob.
   * @param c         The consumer function to call for each matching path
   *                  found.
   * @throws URISyntaxException Could not convert the resource to a URI.
   * @throws IOException        Could not walk the tree.
   */
  public static void walk(
      final String directory, final String glob, final Consumer<Path> c )
      throws URISyntaxException, IOException {
    final var resource = ResourceWalker.class.getResource( directory );
    final var matcher = getDefault().getPathMatcher( "glob:" + glob );

    if( resource != null ) {
      final var uri = resource.toURI();
      final var jar = uri.getScheme().equals( "jar" );
      final var path = jar ? toFileSystem( uri, directory ) : Paths.get( uri );

      try( final var walk = Files.walk( path, 10 ) ) {
        for( final var it = walk.iterator(); it.hasNext(); ) {
          final Path p = it.next();
          if( matcher.matches( p ) ) {
            c.accept( p );
          }
        }
      }
    }
  }

  private static Path toFileSystem( final URI uri, final String directory )
      throws IOException {
    try( final var fs = newFileSystem( uri, emptyMap() ) ) {
      return fs.getPath( directory );
    }
  }
}
