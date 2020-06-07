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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.scrivenvar.definition.DocumentParser;
import com.scrivenvar.definition.TreeAdapter;
import com.scrivenvar.definition.VariableTreeItem;
import javafx.scene.control.TreeItem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map.Entry;

/**
 * Transforms a JsonNode hierarchy into a tree that can be displayed in a user
 * interface and vice-versa.
 *
 * @author White Magic Software, Ltd.
 */
public class YamlTreeAdapter implements TreeAdapter {
  private final DocumentParser<JsonNode> mParser;

  /**
   * Constructs a new instance that will use the given parser to read
   * the object hierarchy from a data source.
   *
   * @param parser Provides the ability to read data into an object hierarchy.
   */
  public YamlTreeAdapter( final DocumentParser<JsonNode> parser ) {
    mParser = parser;
  }

  @Override
  public void export( final TreeItem<String> root, final Path path )
      throws IOException {

    final YAMLMapper mapper = new YAMLMapper();
    final ObjectNode node = mapper.createObjectNode();

    // Iterate over the root item's children. The root item is used by the
    // application to ensure definitions can always be added to a tree.
    for( final TreeItem<String> child : root.getChildren() ) {
      export( child, node );
    }

    // Writes as UTF8 by default.
    mapper.writeValue( path.toFile(), node );
  }

  private void export( final TreeItem<String> item, ObjectNode node ) {
    final var children = item.getChildren();

    // If the current item has more than one non-leaf child, it's an
    // object node and must become a new nested object.
    if( !(children.size() == 1 && children.get( 0 ).isLeaf()) ) {
      node = node.putObject( item.getValue() );
    }

    for( final TreeItem<String> child : children ) {
      if( child.isLeaf() ) {
        node.put( item.getValue(), child.getValue() );
      }
      else {
        export( child, node );
      }
    }
  }

  /**
   * Converts a YAML document to a {@link TreeItem} based on the document
   * keys. Only the first document in the stream is adapted.
   *
   * @param root Root {@link TreeItem} node name.
   * @return A {@link TreeItem} populated with all the keys in the YAML
   * document.
   */
  public TreeItem<String> adapt( final String root ) {
    final JsonNode rootNode = getParser().parse();
    final TreeItem<String> rootItem = createTreeItem( root );

    rootItem.setExpanded( true );
    adapt( rootNode, rootItem );
    return rootItem;
  }

  /**
   * Iterate over a given root node (at any level of the tree) and adapt each
   * leaf node.
   *
   * @param rootNode A JSON node (YAML node) to adapt.
   * @param rootItem The tree item to use as the root when processing the node.
   */
  private void adapt(
      final JsonNode rootNode, final TreeItem<String> rootItem ) {
    rootNode.fields().forEachRemaining(
        ( Entry<String, JsonNode> leaf ) -> adapt( leaf, rootItem )
    );
  }

  /**
   * Recursively adapt each rootNode to a corresponding rootItem.
   *
   * @param rootNode The node to adapt.
   * @param rootItem The item to adapt using the node's key.
   */
  private void adapt(
      final Entry<String, JsonNode> rootNode,
      final TreeItem<String> rootItem ) {
    final JsonNode leafNode = rootNode.getValue();
    final String key = rootNode.getKey();
    final TreeItem<String> leaf = createTreeItem( key );

    if( leafNode.isValueNode() ) {
      leaf.getChildren().add( createTreeItem( rootNode.getValue().asText() ) );
    }

    rootItem.getChildren().add( leaf );

    if( leafNode.isObject() ) {
      adapt( leafNode, leaf );
    }
  }

  /**
   * Creates a new tree item that can be added to the tree view.
   *
   * @param value The node's value.
   * @return A new tree item node, never null.
   */
  private TreeItem<String> createTreeItem( final String value ) {
    return new VariableTreeItem<>( value );
  }

  private DocumentParser<JsonNode> getParser() {
    return mParser;
  }
}
