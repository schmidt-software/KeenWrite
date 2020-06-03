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
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.List;

import static com.scrivenvar.Messages.get;

/**
 * Provides the user interface that holdsa {@link TreeView}, which
 * allows users to interact with key/value pairs loaded from the
 * {@link DocumentParser} and adapted using a {@link TreeAdapter}.
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
   */
  public DefinitionPane() {
    initTreeView();
  }

  /**
   * Changes the root of the {@link TreeView} to the root of the
   * {@link TreeView} from the {@link DefinitionSource}.
   *
   * @param definitionSource Container for the hierarchy of key/value pairs
   *                         to replace the existing hierarchy.
   */
  public void update( final DefinitionSource definitionSource ) {
    assert definitionSource != null;

    final TreeAdapter treeAdapter = definitionSource.getTreeAdapter();
    final TreeItem<String> treeItem = treeAdapter.adapt(
        get( "Pane.definition.node.root.title" )
    );

    update( treeItem );
  }

  /**
   * Changes the root of the {@link TreeView} to the root of the given
   * {@link TreeView}.
   *
   * @param treeItem The new hierarchy of key/value pairs to replace the
   *                 existing hierarchy.
   */
  private void update( final TreeItem<String> treeItem ) {
    assert treeItem != null;

    getTreeView().setRoot( treeItem );
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

  private void initTreeView() {
    final TreeView<String> treeView = getTreeView();

    treeView.setContextMenu( createContextMenu() );
    treeView.setEditable( true );
    treeView.setCellFactory( cell -> createTreeCell() );
    treeView.addEventFilter( KeyEvent.KEY_PRESSED, this::keyEventFilter );
    setSelectionMode( SelectionMode.MULTIPLE );
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

  private ContextMenu createContextMenu() {
    final ContextMenu menu = new ContextMenu();
    final ObservableList<MenuItem> items = menu.getItems();

    addMenuItem( items, "Definition.menu.create" ).setOnAction(
        e -> getSelectedItem().getChildren().add( createTreeItem() )
    );

    addMenuItem( items, "Definition.menu.rename" ).setOnAction(
        e -> getTreeView().edit( getSelectedItem() )
    );

    addMenuItem( items, "Definition.menu.remove" ).setOnAction(
        e -> {
          final TreeItem<String> c = getSelectedItem();
          getSiblings( c ).remove( c );
        }
    );

    return menu;
  }

  private ObservableList<TreeItem<String>> getSiblings(
      final TreeItem<String> item ) {
    final TreeItem<String> root = getTreeView().getRoot();
    final TreeItem<String> parent =
        (item == null || item == root) ? root : item.getParent();

    return parent.getChildren();
  }

  /**
   * Adds a menu item to a list of menu items.
   *
   * @param items    The list of menu items to append to.
   * @param labelKey The resource bundle key name for the menu item's label.
   * @return The menu item added to the list of menu items.
   */
  private MenuItem addMenuItem(
      final List<MenuItem> items, final String labelKey ) {
    final MenuItem menuItem = createMenuItem( labelKey );
    items.add( menuItem );
    return menuItem;
  }

  private MenuItem createMenuItem( final String labelKey ) {
    return new MenuItem( get( labelKey ) );
  }

  private VariableTreeItem<String> createTreeItem() {
    return new VariableTreeItem<>( get( "Definition.menu.add.default" ) );
  }

  private TreeCell<String> createTreeCell() {
    return new TextFieldTreeCell<>(
        createStringConverter() ) {
      @Override
      public void commitEdit( final String newValue ) {
        super.commitEdit( newValue );
        requestFocus();
      }
    };
  }

  private StringConverter<String> createStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString( final String object ) {
        return object == null ? "" : object;
      }

      @Override
      public String fromString( final String string ) {
        return string == null ? "" : string;
      }
    };
  }

  private void keyEventFilter( final KeyEvent event ) {
    if( event.getCode() == KeyCode.ENTER ) {
      final ObservableValue<TreeItem<String>> property =
          getTreeView().editingItemProperty();

      // Consume ENTER presses when not editing a definition value.
      if( property.getValue() == null ) {
        event.consume();
      }
    }
  }

  private TreeItem<String> getSelectedItem() {
    final TreeItem<String> item =
        getTreeView().getSelectionModel().getSelectedItem();
    return item == null ? getTreeView().getRoot() : item;
  }

  /**
   * Delegates to {@link #getSelectionModel()}.
   *
   * @param mode The new selection mode (to enable multiple select).
   */
  @SuppressWarnings("SameParameterValue")
  private void setSelectionMode( final SelectionMode mode ) {
    getSelectionModel().setSelectionMode( mode );
  }

  /**
   * Returns the tree view that contains the YAML definition hierarchy.
   *
   * @return A non-null instance.
   */
  private TreeView<String> getTreeView() {
    return mTreeView;
  }
}
