/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
