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
import com.scrivendor.ui.AbstractPane;
import static com.scrivendor.yaml.YamlTreeAdapter.adapt;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;
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
   * Finds a node that matches a prefix and suffix specified by the given
   * variable. The prefix must match a valid node value. The suffix refers to
   * the start of a string that matches zero or more children of the node
   * specified by the prefix.
   *
   * @param path The word typed by the user, which contains dot-separated node
   * names that represent a path within the YAML tree plus a partial variable
   * name match (for a node).
   *
   * @return The node value that starts with the suffix portion of the given
   * path.
   */
  public String findNode( final String path ) {
    TreeItem<String> cItem = getTreeView().getRoot();
    TreeItem<String> pItem = cItem;

    final StringTokenizer st = new StringTokenizer( path, getSeparator() );

    while( st.hasMoreTokens() ) {
      final String word = st.nextToken();

      // Search along a single branch while the tokenized path matches nodes.
      cItem = findLeaf( cItem, word );

      if( cItem == null ) {
        break;
      }

      pItem = cItem;
    }

    String value = (pItem == null ? "" : pItem.getValue());

    System.out.println( "Current = " + value );

    return value;
  }

  /**
   * Finds an exact match
   *
   * @param trunk The root item containing a list of nodes to search.
   * @param word The value of the item to find.
   *
   * @return The item that matches the given word, or null if not found.
   */
  private TreeItem<String> findLeaf(
    final TreeItem<String> trunk,
    final String word ) {
    final List<TreeItem<String>> branches = trunk.getChildren();
    TreeItem<String> result = null;

    for( final TreeItem<String> leaf : branches ) {
      if( areEqual( leaf.getValue(), word ) ) {
        result = leaf;
        break;
      }
    }

    return result;
  }

  /**
   * Starting at the given parent node, this will return a dot-separated path of
   * all the parent node values concatenated together.
   *
   * @param ti The leaf parent tree item.
   *
   * @return The dot-separated path for this node.
  private String toPath( final TreeItem<String> ti ) {
    // Recurse to the root node, then append the nodes in dot-formation.
    // Iteration would be possible as well, but that requires string
    // insertion, which would end up creating a copy of the string each
    // loop. Plus, it's one line of code.
    return ti == null || ti.getParent() == null
      ? ""
      : toPath( ti.getParent() ) + getSeparator() + ti.getValue();
  }
   */

  /**
   * Compares two strings taking into consideration options for case.
   *
   * @param s1 A non-null string.
   * @param s2
   *
   * @return
   */
  private boolean areEqual( final String s1, final String s2 ) {
    return s1.equalsIgnoreCase( s2 );
  }

  private void initTreeView() {
    getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
  }

  private InputStream asStream( String resource ) {
    return getClass().getResourceAsStream( resource );
  }

  public Node getNode() {
    return getTreeView();
  }

  public final TreeView<String> getTreeView() {
    return this.treeView;
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

  private String getSeparator() {
    return SEPARATOR;
  }
}
