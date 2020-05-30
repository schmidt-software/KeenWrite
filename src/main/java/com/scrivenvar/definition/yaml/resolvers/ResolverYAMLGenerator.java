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
package com.scrivenvar.definition.yaml.resolvers;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.scrivenvar.definition.yaml.YamlParser;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;

/**
 * Intercepts the string writing functionality to resolve the definition
 * value.
 */
public class ResolverYAMLGenerator extends YAMLGenerator {

  private YamlParser yamlParser;

  public ResolverYAMLGenerator(
      final YamlParser yamlParser,
      final IOContext ctxt,
      final int jsonFeatures,
      final int yamlFeatures,
      final ObjectCodec codec,
      final Writer out,
      final DumperOptions.Version version ) throws IOException {
    super( ctxt, jsonFeatures, yamlFeatures, codec, out, version );
    setYamlParser( yamlParser );
  }

  @Override
  public void writeString( final String text ) throws IOException {
    final YamlParser parser = getYamlParser();
    super.writeString( parser.substitute( text ) );
  }

  private YamlParser getYamlParser() {
    return yamlParser;
  }

  private void setYamlParser( final YamlParser yamlParser ) {
    this.yamlParser = yamlParser;
  }
}
