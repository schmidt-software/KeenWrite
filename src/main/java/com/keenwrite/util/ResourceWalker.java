/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static com.keenwrite.util.ProtocolScheme.JAR;
import static com.keenwrite.util.ProtocolScheme.valueFrom;
import static java.nio.file.FileSystems.newFileSystem;
import static java.util.Collections.emptyMap;

/**
 * Responsible for finding file resources, regardless if they exist within
 * a Java Archive (.jar) file or on the native file system.
 *
 * @see FileWalker#walk(Path, String, Consumer)
 */
public final class ResourceWalker {

  /**
   * Walks the given directory hierarchy for files that match the given
   * globbing file name pattern.
   *
   * @param directory Root directory to scan for files matching the glob.
   * @param glob      Only files matching the pattern will be consumed.
   * @param c         Function to call for each matching path found.
   * @throws IOException        Could not walk the tree.
   * @throws URISyntaxException Could not convert the resource to a URI.
   */
  public static void walk(
    final String directory, final String glob, final Consumer<Path> c )
    throws URISyntaxException, IOException {
    final var resource = ResourceWalker.class.getResource( directory );

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

      try {
        FileWalker.walk( path, glob, c );
      } finally {
        if( fs != null ) { fs.close(); }
      }
    }
  }
}
