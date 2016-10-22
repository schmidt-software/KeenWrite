/*
 * Copyright 2016 White Magic Software, Inc.
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
package com.scrivendor.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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

  public YamlTreeAdapter() {
  }
  
  public static TreeView<String> adapt( final InputStream in ) throws IOException {
    return adapt(YamlParser.parse(in));
  }

  /**
   * Iterate over a given root node (at any level of the tree) and process each
   * leaf node.
   *
   * @param root A node to process.
   *
   * @return
   */
  private static TreeView<String> adapt( final JsonNode root ) {
    return new TreeView( process( root ) );
  }

  private static TreeItem<String> process( final JsonNode nodeRoot ) {
    final Iterator<Entry<String, JsonNode>> fields = nodeRoot.fields();
    final TreeItem<String> itemRoot = new TreeItem<>();

    while( fields.hasNext() ) {
      final Entry<String, JsonNode> field = fields.next();
      final JsonNode leaf = field.getValue();

      if( leaf.isObject() ) {
        itemRoot.getChildren().add( process( leaf ) );
      } else {
        itemRoot.setValue( field.getKey() );
      }
    }
    
    return itemRoot;
  }
}
