/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.function.Function;

/**
 * Responsible for reading a YAML document into an object hierarchy.
 */
class YamlParser implements Function<String, JsonNode> {

  /**
   * Creates a new instance that can parse the contents of a YAML
   * document.
   */
  YamlParser() {
  }

  @Override
  public JsonNode apply( final String yaml ) {
    try {
      return new ObjectMapper( new YAMLFactory() ).readTree( yaml );
    } catch( final Exception ex ) {
      // Ensure that a document root node exists.
      return new ObjectMapper().createObjectNode();
    }
  }
}
