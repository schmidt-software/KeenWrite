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
package com.scrivendor.definition;

import static com.scrivendor.Messages.get;
import static com.scrivendor.definition.Lists.getFirst;
import com.scrivendor.ui.AbstractPane;
import static com.scrivendor.yaml.YamlTreeAdapter.adapt;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Provides a list of variables that can be referenced in the editor.
 *
 * @author White Magic Software, Ltd.
 */
public class DefinitionPane extends AbstractPane {

  private final static String SEPARATOR = ".";

  private TreeView<String> treeView;

  /**
   * Reads YAML variables into a tree view.
   */
  public DefinitionPane() {
    try {
      setTreeView(
        adapt(
          // TODO: Allow user loading of variables file.
          asStream( "/com/scrivendor/variables.yaml" ),
          get( "Pane.defintion.node.root.title" )
        )
      );

      initTreeView();
    } catch( IOException e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Finds a tree item with a value that exactly matches the given word.
   *
   * @param trunk The root item containing a list of nodes to search.
   * @param word The value of the item to find.
   * @param predicate Helps determine whether the node value matches the word.
   *
   * @return The item that matches the given word, or null if not found.
   */
  private TreeItem<String> findNode(
    final TreeItem<String> trunk,
    final String word,
    final Predicate predicate ) {
    final List<TreeItem<String>> branches = trunk.getChildren();
    TreeItem<String> result = null;

    for( final TreeItem<String> leaf : branches ) {
      if( predicate.pass( leaf.getValue(), word ) ) {
        result = leaf;
        break;
      }
    }

    return result;
  }

  /**
   * Calls findNode with the ContainsPredicate.
   *
   * @see findNode( TreeItem, String, Predicate )
   * @return The result from findNode.
   */
  private TreeItem<String> findPartialNode(
    final TreeItem<String> trunk,
    final String word ) {
    return findNode( trunk, word, new ContainsPredicate() );
  }

  /**
   * Finds a node that matches a prefix and suffix specified by the given path
   * variable. The prefix must match a valid node value. The suffix refers to
   * the start of a string that matches zero or more children of the node
   * specified by the prefix. The algorithm has the following cases:
   *
   * <ol>
   * <li>Path is empty, return first child.</li>
   * <li>Path contains a complete match, return corresponding node.</li>
   * <li>Path contains a partial match, return nearest node.</li>
   * <li>Path contains a complete and partial match, return nearest node.</li>
   * </ol>
   *
   * @param path The word typed by the user, which contains dot-separated node
   * names that represent a path within the YAML tree plus a partial variable
   * name match (for a node).
   *
   * @return The node value that starts with the suffix portion of the given
   * path, never null.
   */
  public TreeItem<String> findNearestNode( String path ) {
    TreeItem<String> cItem = getTreeRoot();
    TreeItem<String> pItem = cItem;

    int index = path.indexOf( getSeparator() );

    // Find an exact node.
    while( index >= 0 ) {
      final String node = path.substring( 0, index );
      path = path.substring( index + 1 );

      if( (cItem = findPartialNode( cItem, node )) == null ) {
        break;
      }

      index = path.indexOf( getSeparator(), index );
      pItem = cItem;
    }

    // Find a partial node for the remaining text.
    cItem = findPartialNode( pItem, path );

    if( cItem == null ) {
      cItem = pItem;
    }

    return cItem == getTreeRoot() ? getFirst( cItem.getChildren() ) : cItem;
  }

  /**
   * Returns the path for a node, with nodes made distinct using the separator
   * character. This is the antithesis of the findExactNode method.
   *
   * @param t The tree item to path into a string.
   *
   * @return A non-null string, possibly empty.
   */
  public String toPath( TreeItem<String> t ) {
    return t.getParent() == null ? "" : toPath( t.getParent() ) + getSeparator() + t.getValue();
  }

  /**
   * Expands the node to the root, recursively.
   *
   * @param <T> The type of tree item to expand (usually String).
   * @param node The node to expand.
   */
  public <T> void expand( TreeItem<T> node ) {
    if( node != null ) {
      expand( node.getParent() );

      if( !node.isLeaf() ) {
        node.setExpanded( true );
      }
    }
  }

  /**
   * Collapses the tree, recursively.
   */
  public void collapse() {
    collapse( getTreeRoot().getChildren() );
  }

  /**
   * Collapses the tree, recursively.
   *
   * @param <T> The type of tree item to expand (usually String).
   * @param node The nodes to collapse.
   */
  private <T> void collapse( ObservableList<TreeItem<T>> nodes ) {
    for( final TreeItem<T> node : nodes ) {
      node.setExpanded( false );
      collapse( node.getChildren() );
    }
  }

  private void initTreeView() {
    getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
  }

  private InputStream asStream( String resource ) {
    return getClass().getResourceAsStream( resource );
  }

  /**
   * Returns the root node to the tree view.
   *
   * @return getTreeView()
   */
  public Node getNode() {
    return getTreeView();
  }

  /**
   * Returns the tree view that contains the YAML definition hierarchy.
   *
   * @return A non-null instance.
   */
  private TreeView<String> getTreeView() {
    return this.treeView;
  }

  /**
   * Returns the root of the tree.
   *
   * @return The first node added to the YAML definition tree.
   */
  private TreeItem<String> getTreeRoot() {
    return getTreeView().getRoot();
  }

  public <T> boolean isRoot( final TreeItem<T> item ) {
    return getTreeRoot().equals( item );
  }

  /**
   * Given a string, this will attempt to match the first letters in the tree.
   * In so doing, the tree will collapse
   *
   * @param s
   *
   * @return
   */
  public String select( final String s ) {
    getSelectionModel().clearSelection();

    return s;
  }

  private MultipleSelectionModel getSelectionModel() {
    return getTreeView().getSelectionModel();
  }

  /**
   * Sets the tree view (called by the constructor).
   *
   * @param treeView
   */
  private void setTreeView( TreeView<String> treeView ) {
    if( treeView != null ) {
      this.treeView = treeView;
    }
  }

  public String getSeparator() {
    return SEPARATOR;
  }
}
