/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.keenwrite.preview.HtmlPreview;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import static com.keenwrite.Constants.MAP_SIZE_DEFAULT;

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
 * {@link HtmlPreview} without having to reload the definition file.
 * Reloading the definition file would work, but has a number of drawbacks.
 * </p>
 */
public final class TreeItemMapper {
  /**
   * Separates definition keys (e.g., the dots in {@code $root.node.var$}).
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

  public TreeItemMapper() {
  }

  /**
   * Iterate over a given root node (at any level of the tree) and process each
   * leaf node into a flat map. Values must be interpolated separately.
   */
  public Map<String, String> toMap( final TreeItem<String> root ) {
    final var map = new HashMap<String, String>( MAP_SIZE_DEFAULT );
    final var iterator = new TreeIterator( root );

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
  public <T> String toPath( TreeItem<T> node ) {
    assert node != null;

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
