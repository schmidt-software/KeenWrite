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
package com.scrivenvar.definition;

import com.scrivenvar.AbstractFileFactory;
import com.scrivenvar.FileType;
import com.scrivenvar.definition.yaml.YamlDefinitionSource;
import com.scrivenvar.util.ProtocolResolver;

import java.nio.file.Path;

import static com.scrivenvar.Constants.GLOB_PREFIX_DEFINITION;
import static com.scrivenvar.FileType.YAML;

/**
 * Responsible for creating objects that can read and write definition data
 * sources. The data source could be YAML, TOML, JSON, flat files, or from a
 * database.
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

    final String protocol = ProtocolResolver.getProtocol( path.toString() );
    DefinitionSource result = null;

    if( ProtocolResolver.isFile( protocol ) ) {
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
}
