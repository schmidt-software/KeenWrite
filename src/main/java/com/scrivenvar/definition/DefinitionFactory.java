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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    DefinitionSource result = new EmptyDefinitionSource();

    while( keys.hasNext() ) {
      final String key = keys.next();
      final List<String> patterns = properties.getStringSettingList( key );
      final FileTypePredicate predicate = new FileTypePredicate( patterns );

      if( predicate.test( path.toFile() ) ) {
        final String filetype = key.replace( EXTENSIONS_PREFIX + ".", "" );

        result = createFileDefinitionSource( filetype, path );
      }
    }

    return result;
  }

  public DefinitionSource createDefinitionSource( final String path ) {

    final String protocol = getProtocol( path );
    DefinitionSource result = new EmptyDefinitionSource();

    switch( protocol ) {
      case "file":
        result = fileDefinitionSource( Paths.get( path ) );
        break;

      default:
        unknownDefinitionSource( protocol, path );
        break;
    }

    return result;
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
    DefinitionSource result = new EmptyDefinitionSource();

    switch( filetype ) {
      case "yaml":
        result = new YamlFileDefinitionSource( path );
        break;

      default:
        unknownDefinitionSource( filetype, path.toString() );
        break;
    }

    return result;
  }

  /**
   * Throws IllegalArgumentException because the given path could not be
   * recognized.
   *
   * @param type The detected path type (protocol, file extension, etc.).
   * @param path The path to a source of definitions.
   */
  private void unknownDefinitionSource( final String type, final String path ) {
    throw new IllegalArgumentException(
      "Unknown type '" + type + "' for " + path + "."
    );
  }

  private Settings getSettings() {
    return this.settings;
  }

  /**
   * Returns the protocol for a given URI or filename.
   *
   * @param source Determine the protocol for this URI or filename.
   *
   * @return The protocol for the given source.
   */
  private String getProtocol( final String source ) {
    String protocol = null;

    try {
      final URI uri = new URI( source );

      if( uri.isAbsolute() ) {
        protocol = uri.getScheme();
      } else {
        final URL url = new URL( source );
        protocol = url.getProtocol();
      }
    } catch( final URISyntaxException | MalformedURLException e ) {
      // Could be HTTP, HTTPS?
      if( source.startsWith( "//" ) ) {
        throw new IllegalArgumentException( "Relative context: " + source );
      } else {
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
   *
   * @return The protocol for the given file.
   */
  private String getProtocol( final File file ) {
    String result;

    try {
      result = file.toURI().toURL().getProtocol();
    } catch( Exception e ) {
      result = "unknown";
    }

    return result;
  }
}
