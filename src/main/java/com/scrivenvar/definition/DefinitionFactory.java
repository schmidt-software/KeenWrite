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

import com.scrivenvar.Services;
import com.scrivenvar.definition.yaml.YamlFileDefinitionSource;
import com.scrivenvar.predicates.files.FileTypePredicate;
import com.scrivenvar.service.Settings;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Responsible for creating objects that can read and write definition data
 * sources. The data source could be YAML, TOML, JSON, flat files, or from a
 * database.
 *
 * @author White Magic Software, Ltd.
 */
public class DefinitionFactory {

  /**
   * Refers to filename extension settings in the configuration file. Do not
   * terminate this key prefix with a period.
   */
  private static final String EXTENSIONS_PREFIX = "file.ext.definition";

  private final Settings settings = Services.load( Settings.class );

  /**
   * Default (empty) constructor.
   */
  public DefinitionFactory() {
  }

  /**
   * Creates a definition source that can read and write files that match the
   * given file type (from the path).
   *
   * @param path Reference to a variable definition file.
   *
   * @return
   */
  public DefinitionSource fileDefinitionSource( final Path path ) {
    final Settings properties = getSettings();
    final Iterator<String> keys = properties.getKeys( EXTENSIONS_PREFIX );

    DefinitionSource definitions = null;

    while( keys.hasNext() ) {
      final String key = keys.next();
      final List<String> patterns = properties.getStringSettingList( key );
      final FileTypePredicate predicate = new FileTypePredicate( patterns );

      if( predicate.test( path.toFile() ) ) {
        final String filetype = key.replace( EXTENSIONS_PREFIX + ".", "" );

        definitions = createFileDefinitionSource( filetype, path );
      }
    }

    return definitions;
  }

  /**
   * Creates a definition source based on the file type.
   *
   * @param filetype Property key name suffix from settings.properties file.
   * @param path Path to the file that corresponds to the extension.
   *
   * @return A DefinitionSource capable of parsing the data stored at the path.
   */
  private DefinitionSource createFileDefinitionSource(
    final String filetype, final Path path ) {
    final DefinitionSource result;

    switch( filetype ) {
      case "yaml":
        result = new YamlFileDefinitionSource( path );
        break;

      default:
        result = new EmptyDefinitionSource();
        break;
    }

    return result;
  }

  private Settings getSettings() {
    return this.settings;
  }
}
