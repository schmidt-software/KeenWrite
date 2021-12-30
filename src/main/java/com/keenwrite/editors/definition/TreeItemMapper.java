/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.scene.control.TreeItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Given a {@link TreeItem}, this will generate a flat map with all the
 * keys using a dot-separated notation to represent the tree's hierarchy.
 *
 * <ol>
 *   <li>Load YAML file into {@link JsonNode} hierarchy.</li>
 *   <li>Convert JsonNode to a {@link TreeItem} hierarchy.</li>
 *   <li>Convert the {@link TreeItem} hierarchy into a flat map.</li>
 * </ol>
 */
public final class TreeItemMapper {
  /**
   * Key name hierarchy separator (i.e., the dots in {@code root.node.var}).
   */
  public static final String SEPARATOR = ".";

  /**
   * Default buffer length for key names that should be large enough to
   * avoid reallocating memory to increase the {@link StringBuilder}'s
   * buffer.
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
      final var next = mStack.pop();
      next.getChildren().forEach( mStack::push );

      return next;
    }
  }

  /**
   * Iterate over a given root node (at any level of the tree) and process each
   * leaf node into a flat map.
   *
   * @param root The topmost item in the tree.
   */
  public static Map<String, String> convert( final TreeItem<String> root ) {
    final var map = new HashMap<String, String>();

    new TreeIterator( root ).forEachRemaining( item -> {
      if( item.isLeaf() && item.getParent() != null ) {
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
    final var key = new StringBuilder( DEFAULT_KEY_LENGTH );
    final var stack = new Stack<TreeItem<T>>();

    while( node != null && !(node instanceof RootTreeItem) ) {
      stack.push( node );
      node = node.getParent();
    }

    // Gets set at end of first iteration (to avoid an if condition).
    var separator = "";

    while( !stack.empty() ) {
      final T subkey = stack.pop().getValue();
      key.append( separator );
      key.append( subkey );
      separator = SEPARATOR;
    }

    return key.toString();
  }
}
