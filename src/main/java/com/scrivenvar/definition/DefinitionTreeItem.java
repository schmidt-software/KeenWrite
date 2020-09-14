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

import javafx.scene.control.TreeItem;

import java.util.Stack;
import java.util.function.BiFunction;

import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;

/**
 * Provides behaviour afforded to definition keys and corresponding value.
 *
 * @param <T> The type of {@link TreeItem} (usually string).
 */
public class DefinitionTreeItem<T> extends TreeItem<T> {

  /**
   * Constructs a new item with a default value.
   *
   * @param value Passed up to superclass.
   */
  public DefinitionTreeItem( final T value ) {
    super( value );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value. Search is performed case-sensitively.
   *
   * @param text The text to match against each leaf in the tree.
   * @return The leaf that has a value exactly matching the given text.
   */
  public DefinitionTreeItem<T> findLeafExact( final String text ) {
    return findLeaf( text, DefinitionTreeItem::valueEquals );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value. Search is performed case-sensitively.
   *
   * @param text The text to match against each leaf in the tree.
   * @return The leaf that has a value that contains the given text.
   */
  public DefinitionTreeItem<T> findLeafContains( final String text ) {
    return findLeaf( text, DefinitionTreeItem::valueContains );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value. Search is performed case-insensitively.
   *
   * @param text The text to match against each leaf in the tree.
   * @return The leaf that has a value that contains the given text.
   */
  public DefinitionTreeItem<T> findLeafContainsNoCase( final String text ) {
    return findLeaf( text, DefinitionTreeItem::valueContainsNoCase );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value. Search is performed case-sensitively.
   *
   * @param text The text to match against each leaf in the tree.
   * @return The leaf that has a value that starts with the given text.
   */
  public DefinitionTreeItem<T> findLeafStartsWith( final String text ) {
    return findLeaf( text, DefinitionTreeItem::valueStartsWith );
  }

  /**
   * Finds a leaf starting at the current node with text that matches the given
   * value.
   *
   * @param text     The text to match against each leaf in the tree.
   * @param findMode What algorithm is used to match the given text.
   * @return The leaf that has a value starting with the given text, or {@code
   * null} if there was no match found.
   */
  public DefinitionTreeItem<T> findLeaf(
      final String text,
      final BiFunction<DefinitionTreeItem<T>, String, Boolean> findMode ) {
    final var stack = new Stack<DefinitionTreeItem<T>>();
    stack.push( this );

    // Don't hunt for blank (empty) keys.
    boolean found = text.isBlank();

    while( !found && !stack.isEmpty() ) {
      final var node = stack.pop();

      for( final var child : node.getChildren() ) {
        final var result = (DefinitionTreeItem<T>) child;

        if( result.isLeaf() ) {
          if( found = findMode.apply( result, text ) ) {
            return result;
          }
        }
        else {
          stack.push( result );
        }
      }
    }

    return null;
  }

  /**
   * Returns the value of the string without diacritic marks.
   *
   * @return A non-null, possibly empty string.
   */
  private String getDiacriticlessValue() {
    return normalize( getValue().toString(), NFD )
        .replaceAll( "\\p{M}", "" );
  }

  /**
   * Returns true if this node is a leaf and its value equals the given text.
   *
   * @param s The text to compare against the node value.
   * @return true Node is a leaf and its value equals the given value.
   */
  private boolean valueEquals( final String s ) {
    return isLeaf() && getValue().equals( s );
  }

  /**
   * Returns true if this node is a leaf and its value contains the given text.
   *
   * @param s The text to compare against the node value.
   * @return true Node is a leaf and its value contains the given value.
   */
  private boolean valueContains( final String s ) {
    return isLeaf() && getDiacriticlessValue().contains( s );
  }

  /**
   * Returns true if this node is a leaf and its value contains the given text.
   *
   * @param s The text to compare against the node value.
   * @return true Node is a leaf and its value contains the given value.
   */
  private boolean valueContainsNoCase( final String s ) {
    return isLeaf() && getDiacriticlessValue()
        .toLowerCase().contains( s.toLowerCase() );
  }

  /**
   * Returns true if this node is a leaf and its value starts with the given
   * text.
   *
   * @param s The text to compare against the node value.
   * @return true Node is a leaf and its value starts with the given value.
   */
  private boolean valueStartsWith( final String s ) {
    return isLeaf() && getDiacriticlessValue().startsWith( s );
  }

  /**
   * Returns the path for this node, with nodes made distinct using the
   * separator character. This uses two loops: one for pushing nodes onto a
   * stack and one for popping them off to create the path in desired order.
   *
   * @return A non-null string, possibly empty.
   */
  public String toPath() {
    return TreeItemAdapter.toPath( getParent() );
  }

  /**
   * Answers whether there are any definitions in this tree.
   *
   * @return {@code true} when there are no definitions in the tree; {@code
   * false} when there is at least one definition present.
   */
  public boolean isEmpty() {
    return getChildren().isEmpty();
  }
}
