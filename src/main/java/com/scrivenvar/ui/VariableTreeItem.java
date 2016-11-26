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
package com.scrivenvar.ui;

import static com.scrivenvar.definition.DefinitionPane.SEPARATOR;
import static com.scrivenvar.editor.VariableNameInjector.DEFAULT_MAX_VAR_LENGTH;
import java.util.Stack;
import javafx.scene.control.TreeItem;

/**
 * Provides behaviour afforded to variable names and their corresponding value.
 *
 * @author White Magic Software, Ltd.
 * @param <T> The type of TreeItem (usually String).
 */
public class VariableTreeItem<T> extends TreeItem<T> {

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
    final Stack<TreeItem<T>> stack = new Stack<>();
    final TreeItem<T> root = this;

    stack.push( root );

    boolean found = false;
    TreeItem<T> node = null;

    while( !stack.isEmpty() && !found ) {
      node = stack.pop();

      if( node.isLeaf() && node.getValue().toString().startsWith( text ) ) {
        found = true;
      } else {
        for( final TreeItem<T> child : node.getChildren() ) {
          stack.push( child );
        }

        // No match found, yet.
        node = null;
      }
    }

    return (VariableTreeItem<T>)node;
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
}
