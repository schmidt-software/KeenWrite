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
package com.scrivenvar.definition;

import com.scrivenvar.decorators.VariableDecorator;
import com.scrivenvar.decorators.YamlVariableDecorator;
import static com.scrivenvar.definition.yaml.YamlParser.SEPARATOR;
import static com.scrivenvar.editors.VariableNameInjector.DEFAULT_MAX_VAR_LENGTH;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javafx.scene.control.TreeItem;

/**
 * Provides behaviour afforded to variable names and their corresponding value.
 *
 * @author White Magic Software, Ltd.
 * @param <T> The type of TreeItem (usually String).
 */
public class VariableTreeItem<T> extends TreeItem<T> {

  private final static int DEFAULT_MAP_SIZE = 1000;

  private final static VariableDecorator VARIABLE_DECORATOR
    = new YamlVariableDecorator();

  /**
   * Flattened tree.
   */
  private Map<String, String> map;

  /**
   * Constructs a new item with a default value.
   *
   * @param value Passed up to superclass.
   */
  public VariableTreeItem( final T value ) {
    super( value );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value.
   *
   * @param text The text to match against each leaf in the tree.
   *
   * @return The leaf that has a value starting with the given text.
   */
  public VariableTreeItem<T> findLeaf( final String text ) {
    return findLeaf( text, false );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value.
   *
   * @param text The text to match against each leaf in the tree.
   * @param contains Set to true to perform a substring match if starts with
   * fails.
   *
   * @return The leaf that has a value starting with the given text.
   */
  public VariableTreeItem<T> findLeaf(
    final String text,
    final boolean contains ) {

    final Stack<VariableTreeItem<T>> stack = new Stack<>();
    final VariableTreeItem<T> root = this;

    stack.push( root );

    boolean found = false;
    VariableTreeItem<T> node = null;

    while( !found && !stack.isEmpty() ) {
      node = stack.pop();

      if( contains && node.valueContains( text ) ) {
        found = true;
      }
      else if( !contains && node.valueStartsWith( text ) ) {
        found = true;
      }
      else {
        for( final TreeItem<T> child : node.getChildren() ) {
          stack.push( (VariableTreeItem<T>)child );
        }

        // No match found, yet.
        node = null;
      }
    }

    return (VariableTreeItem<T>)node;
  }

  /**
   * Returns true if this node is a leaf and its value starts with the given
   * text.
   *
   * @param s The text to compare against the node value.
   *
   * @return true Node is a leaf and its value starts with the given value.
   */
  private boolean valueStartsWith( final String s ) {
    return isLeaf() && getValue().toString().startsWith( s );
  }

  /**
   * Returns true if this node is a leaf and its value contains the given text.
   *
   * @param s The text to compare against the node value.
   *
   * @return true Node is a leaf and its value contains the given value.
   */
  private boolean valueContains( final String s ) {
    return isLeaf() && getValue().toString().contains( s );
  }

  /**
   * Returns the path for this node, with nodes made distinct using the
   * separator character. This uses two loops: one for pushing nodes onto a
   * stack and one for popping them off to create the path in desired order.
   *
   * @return A non-null string, possibly empty.
   */
  public String toPath() {
    final Stack<TreeItem<T>> stack = new Stack<>();
    TreeItem<T> node = this;

    while( node.getParent() != null ) {
      stack.push( node );
      node = node.getParent();
    }

    final StringBuilder sb = new StringBuilder( DEFAULT_MAX_VAR_LENGTH );

    while( !stack.isEmpty() ) {
      node = stack.pop();

      if( !node.isLeaf() ) {
        sb.append( node.getValue() );

        // This will add a superfluous separator, but instead of peeking at
        // the stack all the time, the last separator will be removed outside
        // the loop (one operation executed once).
        sb.append( SEPARATOR );
      }
    }

    // Remove the trailing SEPARATOR.
    if( sb.length() > 0 ) {
      sb.setLength( sb.length() - 1 );
    }

    return sb.toString();
  }

  /**
   * Returns the hierarchy, flattened to key-value pairs.
   *
   * @return A map of this tree's key-value pairs.
   */
  public Map<String, String> getMap() {
    if( this.map == null ) {
      this.map = new HashMap<>( DEFAULT_MAP_SIZE );
      populate( this, this.map );
    }

    return this.map;
  }

  private void populate( final TreeItem<T> parent, final Map<String, String> map ) {
    for( final TreeItem<T> child : parent.getChildren() ) {
      if( child.isLeaf() ) {
        @SuppressWarnings( "unchecked" )
        final String key = toVariable( ((VariableTreeItem<String>)child).toPath() );
        final String value = child.getValue().toString();

        map.put( key, value );
      }
      else {
        populate( child, map );
      }
    }
  }

  /**
   * Converts the name of the key to a simple variable by enclosing it with
   * dollar symbols.
   *
   * @param key The key name to change to a variable.
   *
   * @return $key$
   */
  public String toVariable( final String key ) {
    return VARIABLE_DECORATOR.decorate( key );
  }
}
