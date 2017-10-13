/*
 * The MIT License
 *
 * Copyright 2017 White Magic Software, Ltd..
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.scrivenvar.definition.yaml.resolvers;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.scrivenvar.definition.yaml.YamlParser;
import java.io.IOException;
import java.io.Writer;
import org.yaml.snakeyaml.DumperOptions;

/**
 *
 * @author White Magic Software, Ltd.
 */
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
  public void writeString( final String text )
    throws IOException, JsonGenerationException {
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
