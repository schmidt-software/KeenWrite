/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.definition;

import com.scrivenvar.AbstractFileFactory;
import com.scrivenvar.FileType;
import com.scrivenvar.definition.yaml.YamlDefinitionSource;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.FileType.YAML;

/**
 * Responsible for creating objects that can read and write definition data
 * sources. The data source could be YAML, TOML, JSON, flat files, or from a
 * database.
 *
 * @author White Magic Software, Ltd.
 */
public class DefinitionFactory extends AbstractFileFactory {

  /**
   * Default (empty) constructor.
   */
  public DefinitionFactory() {
  }

  /**
   * Creates a definition source capable of reading definitions from the given
   * path.
   *
   * @param path Path to a resource containing definitions.
   * @return The definition source appropriate for the given path.
   */
  public DefinitionSource createDefinitionSource( final Path path ) {
    assert path != null;

    final String protocol = getProtocol( path.toString() );
    DefinitionSource result = null;

    if( DEFINITION_PROTOCOL_FILE.equals( protocol ) ) {
      final FileType filetype = lookup( path, GLOB_PREFIX_DEFINITION );
      result = createFileDefinitionSource( filetype, path );
    }
    else {
      unknownFileType( protocol, path.toString() );
    }

    return result;
  }

  /**
   * Creates a definition source based on the file type.
   *
   * @param filetype Property key name suffix from settings.properties file.
   * @param path     Path to the file that corresponds to the extension.
   * @return A DefinitionSource capable of parsing the data stored at the path.
   */
  private DefinitionSource createFileDefinitionSource(
      final FileType filetype, final Path path ) {
    assert filetype != null;
    assert path != null;

    if( filetype == YAML ) {
      return new YamlDefinitionSource( path );
    }

    throw new IllegalArgumentException( filetype.toString() );
  }

  /**
   * Returns the protocol for a given URI or filename.
   *
   * @param source Determine the protocol for this URI or filename.
   * @return The protocol for the given source.
   */
  private String getProtocol( final String source ) {
    String protocol;

    try {
      final URI uri = new URI( source );

      if( uri.isAbsolute() ) {
        protocol = uri.getScheme();
      }
      else {
        final URL url = new URL( source );
        protocol = url.getProtocol();
      }
    } catch( final Exception e ) {
      // Could be HTTP, HTTPS?
      if( source.startsWith( "//" ) ) {
        throw new IllegalArgumentException( "Relative context: " + source );
      }
      else {
        final File file = new File( source );
        protocol = getProtocol( file );
      }
    }

    return protocol;
  }

  /**
   * Returns the protocol for a given file.
   *
   * @param file Determine the protocol for this file.
   * @return The protocol for the given file.
   */
  private String getProtocol( final File file ) {
    String result;

    try {
      result = file.toURI().toURL().getProtocol();
    } catch( final Exception e ) {
      result = DEFINITION_PROTOCOL_UNKNOWN;
    }

    return result;
  }
}
