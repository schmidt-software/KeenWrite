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

import com.scrivenvar.AbstractPane;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Provides key/value pairs that can be referenced from within the editor.
 *
 * @author White Magic Software, Ltd.
 */
public final class DefinitionPane extends AbstractPane {

  /**
   * Trimmed off the end of a word to match a variable name.
   */
  private final static String TERMINALS = ":;,.!?-/\\¡¿";

  private final TreeView<String> mTreeView = new TreeView<>();

  /**
   * Constructs a definition pane with a given tree view root.
   * See {@link com.scrivenvar.definition.yaml.YamlTreeAdapter#adapt(String)}
   * for details.
   */
  public DefinitionPane() {
    initTreeView();
  }

  /**
   * Changes the root of the {@link TreeView} to the root of the given
   * {@link TreeView}.
   *
   * @param treeView The new hierarchy of key/value pairs to replace the
   *                 existing hierarchy.
   */
  public void update( final TreeView<String> treeView ) {
    assert treeView != null;
    mTreeView.setRoot( treeView.getRoot() );
  }

  /**
   * Returns the leaf that matches the given value. If the value is terminally
   * punctuated, the punctuation is removed if no match was found.
   *
   * @param value    The value to find, never null.
   * @param findMode Defines how to match words.
   * @return The leaf that contains the given value, or null if neither the
   * original value nor the terminally-trimmed value was found.
   */
  public VariableTreeItem<String> findLeaf(
      final String value, final FindMode findMode ) {
    final VariableTreeItem<String> root = getTreeRoot();
    final VariableTreeItem<String> leaf = root.findLeaf( value, findMode );

    return leaf == null
        ? root.findLeaf( rtrimTerminalPunctuation( value ) )
        : leaf;
  }

  /**
   * Removes punctuation from the end of a string.
   *
   * @param s The string to trim, never null.
   * @return The string trimmed of all terminal characters from the end
   */
  private String rtrimTerminalPunctuation( final String s ) {
    assert s != null;
    int index = s.length() - 1;

    while( index > 0 && (TERMINALS.indexOf( s.charAt( index ) ) >= 0) ) {
      index--;
    }

    return s.substring( 0, index );
  }

  private void initTreeView() {
    getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
  }

  /**
   * Expands the node to the root, recursively.
   *
   * @param <T>  The type of tree item to expand (usually String).
   * @param node The node to expand.
   */
  public <T> void expand( final TreeItem<T> node ) {
    if( node != null ) {
      expand( node.getParent() );

      if( !node.isLeaf() ) {
        node.setExpanded( true );
      }
    }
  }

  public void select( final TreeItem<String> item ) {
    clearSelection();
    selectItem( getTreeView().getRow( item ) );
  }

  private void clearSelection() {
    getSelectionModel().clearSelection();
  }

  private void selectItem( final int row ) {
    getSelectionModel().select( row );
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
   * @param <T>   The type of tree item to expand (usually String).
   * @param nodes The nodes to collapse.
   */
  private <T> void collapse( ObservableList<TreeItem<T>> nodes ) {
    for( final TreeItem<T> node : nodes ) {
      node.setExpanded( false );
      collapse( node.getChildren() );
    }
  }

  /**
   * Returns the root node to the tree view.
   *
   * @return getTreeView()
   */
  public Node getNode() {
    return getTreeView();
  }

  private MultipleSelectionModel<TreeItem<String>> getSelectionModel() {
    return getTreeView().getSelectionModel();
  }

  /**
   * Returns the tree view that contains the YAML definition hierarchy.
   *
   * @return A non-null instance.
   */
  private TreeView<String> getTreeView() {
    return mTreeView;
  }

  /**
   * Returns the root of the tree.
   *
   * @return The first node added to the YAML definition tree.
   */
  private VariableTreeItem<String> getTreeRoot() {
    final TreeItem<String> root = getTreeView().getRoot();

    return root instanceof VariableTreeItem ?
        (VariableTreeItem<String>) root : new VariableTreeItem<>( "root" );
  }
}
