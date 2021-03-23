/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.keenwrite.util.ProtocolScheme.JAR;
import static com.keenwrite.util.ProtocolScheme.valueFrom;
import static java.io.File.pathSeparator;
import static java.lang.System.getenv;
import static java.nio.file.FileSystems.getDefault;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.isExecutable;
import static java.util.Collections.emptyMap;
import static java.util.regex.Pattern.quote;

/**
 * Responsible for finding file resources.
 */
public final class ResourceWalker {
  /**
   * For finding executable programs.
   */
  private static final String[] EXTENSIONS = new String[]
    {"", ".com", ".exe", ".bat", ".cmd"};

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
      final Path path;
      FileSystem fs = null;

      if( valueFrom( uri ) == JAR ) {
        fs = newFileSystem( uri, emptyMap() );
        path = fs.getPath( directory );
      }
      else {
        path = Paths.get( uri );
      }

      try( final var walk = Files.walk( path, 10 ) ) {
        for( final var it = walk.iterator(); it.hasNext(); ) {
          final Path p = it.next();
          if( matcher.matches( p ) ) {
            c.accept( p );
          }
        }
      } finally {
        if( fs != null ) { fs.close(); }
      }
    }
  }

  /**
   * Given the name of an executable (without an extension) file, this will
   * attempt to determine whether the executable is found in the PATH
   * environment variable.
   *
   * @param exe The executable file name to find.
   * @return {@code true} when the given file name references an executable
   * file located in the PATH environment variable.
   */
  public static boolean canExecute( final String exe ) {
    final var paths = getenv( "PATH" ).split( quote( pathSeparator ) );
    return Stream.of( paths ).map( Paths::get ).anyMatch(
      path -> {
        final var p = path.resolve( exe );
        var found = false;

        for( final var extension : EXTENSIONS ) {
          if( isExecutable( Path.of( p.toString() + extension ) ) ) {
            found = true;
            break;
          }
        }

        return found;
      }
    );
  }
}
