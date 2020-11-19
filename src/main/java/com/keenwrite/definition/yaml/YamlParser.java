/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.definition.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.keenwrite.definition.DocumentParser;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Responsible for reading a YAML document into an object hierarchy.
 */
public class YamlParser implements DocumentParser<JsonNode> {

  /**
   * Creates a new instance that can parse the contents of a YAML
   * document.
   */
  public YamlParser() {
  }

  @Override
  public JsonNode parse( final String yaml ) {
    try {
      return new ObjectMapper( new YAMLFactory() ).readTree( yaml );
    } catch( final Exception ex ) {
      // Ensure that a document root node exists by relying on the
      // default failure condition when processing.
      return new ObjectMapper().createObjectNode();
    }
  }

  /**
   * Parses the given path containing YAML data into an object hierarchy.
   *
   * @param path {@link Path} to the YAML resource to parse.
   * @return The parsed contents, or an empty object hierarchy.
   * @deprecated Use parse(String) instead.
   */
  private JsonNode parse( final Path path ) {
    try( final InputStream in = Files.newInputStream( path ) ) {
      return new ObjectMapper( new YAMLFactory() ).readTree( in );
    } catch( final Exception e ) {
      // Ensure that a document root node exists by relying on the
      // default failure condition when processing. This is required
      // because the input stream could not be read.
      return new ObjectMapper().createObjectNode();
    }
  }
}
