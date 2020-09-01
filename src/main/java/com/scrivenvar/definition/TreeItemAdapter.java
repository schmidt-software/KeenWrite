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
package com.scrivenvar.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.scrivenvar.sigils.YamlSigilOperator;
import com.scrivenvar.preview.HTMLPreviewPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import static com.scrivenvar.Constants.DEFAULT_MAP_SIZE;

/**
 * Given a {@link TreeItem}, this will generate a flat map with all the
 * values in the tree recursively interpolated. The application integrates
 * definition files as follows:
 * <ol>
 *   <li>Load YAML file into {@link JsonNode} hierarchy.</li>
 *   <li>Convert JsonNode to a {@link TreeItem} hierarchy.</li>
 *   <li>Interpolate {@link TreeItem} hierarchy as a flat map.</li>
 *   <li>Substitute flat map variables into document as required.</li>
 * </ol>
 *
 * <p>
 * This class is responsible for producing the interpolated flat map. This
 * allows dynamic edits of the {@link TreeView} to be displayed in the
 * {@link HTMLPreviewPane} without having to reload the definition file.
 * Reloading the definition file would work, but has a number of drawbacks.
 * </p>
 */
public class TreeItemAdapter {
  /**
   * Separates YAML definition keys (e.g., the dots in {@code $root.node.var$}).
   */
  public static final String SEPARATOR = ".";

  /**
   * Default buffer length for keys ({@link StringBuilder} has 16 character
   * buffer) that should be large enough for most keys to avoid reallocating
   * memory to increase the {@link StringBuilder}'s buffer.
   */
  public static final int DEFAULT_KEY_LENGTH = 64;

  /**
   * In-order traversal of a {@link TreeItem} hierarchy, exposing each item
   * as a consecutive list.
   */
  private static final class TreeIterator
      implements Iterator<TreeItem<String>> {
    private final Stack<TreeItem<String>> mStack = new Stack<>();

    public TreeIterator( final TreeItem<String> root ) {
      if( root != null ) {
        mStack.push( root );
      }
    }

    @Override
    public boolean hasNext() {
      return !mStack.isEmpty();
    }

    @Override
    public TreeItem<String> next() {
      final TreeItem<String> next = mStack.pop();
      next.getChildren().forEach( mStack::push );

      return next;
    }
  }

  private TreeItemAdapter() {
  }

  /**
   * Iterate over a given root node (at any level of the tree) and process each
   * leaf node into a flat map. Values must be interpolated separately.
   */
  public static Map<String, String> toMap( final TreeItem<String> root ) {
    final Map<String, String> map = new HashMap<>( DEFAULT_MAP_SIZE );
    final TreeIterator iterator = new TreeIterator( root );

    iterator.forEachRemaining( item -> {
      if( item.isLeaf() ) {
        map.put( toPath( item.getParent() ), item.getValue() );
      }
    } );

    return map;
  }


  /**
   * For a given node, this will ascend the tree to generate a key name
   * that is associated with the leaf node's value.
   *
   * @param node Ascendants represent the key to this node's value.
   * @param <T>  Data type that the {@link TreeItem} contains.
   * @return The string representation of the node's unique key.
   */
  public static <T> String toPath( TreeItem<T> node ) {
    assert node != null;

    final StringBuilder key = new StringBuilder( DEFAULT_KEY_LENGTH );
    final Stack<TreeItem<T>> stack = new Stack<>();

    while( node != null && !(node instanceof RootTreeItem) ) {
      stack.push( node );
      node = node.getParent();
    }

    // Gets set at end of first iteration (to avoid an if condition).
    String separator = "";

    while( !stack.empty() ) {
      final T subkey = stack.pop().getValue();
      key.append( separator );
      key.append( subkey );
      separator = SEPARATOR;
    }

    return YamlSigilOperator.entoken( key.toString() );
  }
}
