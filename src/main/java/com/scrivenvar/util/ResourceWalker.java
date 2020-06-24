package com.scrivenvar.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;

/**
 * Responsible for finding file resources.
 */
public class ResourceWalker {
  private static final PathMatcher PATH_MATCHER =
      FileSystems.getDefault().getPathMatcher( "glob:**.ttf" );

  /**
   * @param dirName The root directory to scan for files matching the glob.
   * @param c       The consumer function to call for each matching path found.
   * @throws URISyntaxException Could not convert the resource to a URI.
   * @throws IOException        Could not walk the tree.
   */
  public static void walk( final String dirName, final Consumer<Path> c )
      throws URISyntaxException, IOException {
    final var resource = ResourceWalker.class.getResource( dirName );

    if( resource != null ) {
      final var uri = resource.toURI();
      final var path = uri.getScheme().equals( "jar" )
          ? newFileSystem( uri, emptyMap() ).getPath( dirName )
          : Paths.get( uri );
      final var walk = Files.walk( path, 10 );

      for( final var it = walk.iterator(); it.hasNext(); ) {
        final Path p = it.next();
        if( PATH_MATCHER.matches( p ) ) {
          c.accept( p );
        }
      }
    }
  }
}
