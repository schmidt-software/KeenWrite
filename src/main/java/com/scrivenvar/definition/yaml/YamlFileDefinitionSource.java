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
package com.scrivenvar.definition.yaml;

import com.scrivenvar.definition.FileDefinitionSource;
import javafx.scene.control.TreeView;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.scrivenvar.Messages.get;

/**
 * Represents a definition data source for YAML files.
 *
 * @author White Magic Software, Ltd.
 */
public class YamlFileDefinitionSource extends FileDefinitionSource {

  private final YamlTreeAdapter mYamlTreeAdapter;
  private final YamlParser mYamlParser;

  /**
   * Constructs a new YAML definition source, populated from the given file.
   *
   * @param path Path to the YAML definition file.
   */
  public YamlFileDefinitionSource( final Path path ) {
    super( path );

    mYamlParser = createYamlParser( path );
    mYamlTreeAdapter = createYamlTreeAdapter( mYamlParser );
  }

  @Override
  public Map<String, String> getResolvedMap() {
    return getYamlParser().createResolvedMap();
  }

  private YamlTreeAdapter getYamlTreeAdapter() {
    return mYamlTreeAdapter;
  }

  private YamlParser getYamlParser() {
    return mYamlParser;
  }

  private YamlParser createYamlParser( final Path path ) {
    try( final InputStream in = Files.newInputStream( path ) ) {
      return new YamlParser( in );
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  private YamlTreeAdapter createYamlTreeAdapter( final YamlParser yamlParser ) {
    return new YamlTreeAdapter( yamlParser );
  }

  @Override
  protected TreeView<String> createTreeView() {
    return getYamlTreeAdapter().adapt(
        get( "Pane.definition.node.root.title" )
    );
  }

  @Override
  public String getError() {
    return getYamlParser().getError();
  }
}
