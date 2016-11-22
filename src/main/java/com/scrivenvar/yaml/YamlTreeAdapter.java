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
package com.scrivenvar.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Transforms a JsonNode hierarchy into a tree that can be displayed in a user
 * interface.
 *
 * @author White Magic Software, Ltd.
 */
public class YamlTreeAdapter {

  protected YamlTreeAdapter() {
  }
  
  /**
   * Converts a YAML document to a TreeView based on the document keys. Only the
   * first document in the stream is adapted. This does not close the stream.
   *
   * @param in Contains a YAML document.
   * @param name Name of the root TreeItem.
   *
   * @return A TreeView populated with all the keys in the YAML document.
   *
   * @throws IOException Could not read from the stream.
   */
  public static TreeView<String> adapt(
    final InputStream in, final String name ) throws IOException {
    final YamlTreeAdapter adapter = new YamlTreeAdapter();
    final JsonNode rootNode = YamlParser.parse( in );
    final TreeItem<String> rootItem = new TreeItem<>( name );

    rootItem.setExpanded( true );
    adapter.adapt( rootNode, rootItem );
    return new TreeView<>( rootItem );
  }

  /**
   * Iterate over a given root node (at any level of the tree) and adapt each
   * leaf node.
   *
   * @param rootNode A JSON node (YAML node) to adapt.
   * @param rootItem The tree item to use as the root when processing the node.
   */
  protected void adapt(
    final JsonNode rootNode,
    final TreeItem<String> rootItem ) {
    rootNode.fields().forEachRemaining(
      (Entry<String, JsonNode> leaf) -> adapt( leaf, rootItem )
    );
  }

  /**
   * Recursively adapt each rootNode to a corresponding rootItem.
   *
   * @param rootNode The node to adapt.
   * @param rootItem The item to adapt using the node's key.
   */
  protected void adapt(
    final Entry<String, JsonNode> rootNode,
    final TreeItem<String> rootItem ) {
    final JsonNode leafNode = rootNode.getValue();
    final String key = rootNode.getKey();
    final TreeItem<String> leafItem = new TreeItem<>( key );

    if( leafNode.isValueNode() ) {
      leafItem.getChildren().add( new TreeItem<>( rootNode.getValue().asText() ) );
    }

    rootItem.getChildren().add( leafItem );

    if( leafNode.isObject() ) {
      adapt( leafNode, leafItem );
    }
  }
}
