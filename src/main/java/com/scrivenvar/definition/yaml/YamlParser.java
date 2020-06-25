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
package com.scrivenvar.definition.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.scrivenvar.Messages;
import com.scrivenvar.definition.DocumentParser;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.scrivenvar.Constants.STATUS_BAR_OK;

/**
 * Responsible for reading a YAML document into an object hierarchy.
 */
public class YamlParser implements DocumentParser<JsonNode> {

  /**
   * Error that occurred while parsing.
   */
  private String mError;

  /**
   * Start of the Universe (the YAML document node that contains all others).
   */
  private final JsonNode mDocumentRoot;

  /**
   * Creates a new YamlParser instance that attempts to parse the contents
   * of the YAML document given from a path. In the event that the file either
   * does not exist or is empty, a fake
   *
   * @param path Path to a file containing YAML data to parse.
   */
  public YamlParser( final Path path ) {
    assert path != null;
    mDocumentRoot = parse( path );
  }

  /**
   * Returns the parent node for the entire YAML document tree.
   *
   * @return The document root, never {@code null}.
   */
  @Override
  public JsonNode getDocumentRoot() {
    return mDocumentRoot;
  }

  /**
   * Parses the given path containing YAML data into an object hierarchy.
   *
   * @param path {@link Path} to the YAML resource to parse.
   * @return The parsed contents, or an empty object hierarchy.
   */
  private JsonNode parse( final Path path ) {
    try( final InputStream in = Files.newInputStream( path ) ) {
      setError( Messages.get( STATUS_BAR_OK ) );

      return new ObjectMapper( new YAMLFactory() ).readTree( in );
    } catch( final Exception e ) {
      setError( Messages.get( "yaml.error.open" ) );

      // Ensure that a document root node exists by relying on the
      // default failure condition when processing. This is required
      // because the input stream could not be read.
      return new ObjectMapper().createObjectNode();
    }
  }

  private void setError( final String error ) {
    mError = error;
  }

  /**
   * Returns the last error message, if any, that occurred during parsing.
   *
   * @return The error message or the empty string if no error occurred.
   */
  public String getError() {
    return mError;
  }
}
