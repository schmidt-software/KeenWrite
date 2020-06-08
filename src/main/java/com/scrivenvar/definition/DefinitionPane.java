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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.scrivenvar.Messages.get;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

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

  /**
   * Contains a view of the definitions.
   */
  private final TreeView<String> mTreeView = new TreeView<>();

  /**
   * Constructs a definition pane with a given tree view root.
   */
  public DefinitionPane() {
    final var treeView = getTreeView();
    treeView.setEditable( true );
    treeView.setCellFactory( cell -> createTreeCell() );
    treeView.setContextMenu( createContextMenu() );
    treeView.addEventFilter( KEY_PRESSED, this::keyEventFilter );
    treeView.setShowRoot( false );
    getSelectionModel().setSelectionMode( SelectionMode.MULTIPLE );
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
    final TreeItem<String> root = treeAdapter.adapt(
        get( "Pane.definition.node.root.title" )
    );

    getTreeView().setRoot( root );
  }

  public Map<String, String> toMap() {
    return TreeItemInterpolator.toMap( getTreeView().getRoot() );
  }

  /**
   * Informs the caller of whenever any {@link TreeItem} in the {@link TreeView}
   * is modified. The modifications include: item value changes, item additions,
   * and item removals.
   * <p>
   * Safe to call multiple times; if a handler is already registered, the
   * old handler is used.
   * </p>
   *
   * @param handler The handler to call whenever any {@link TreeItem} changes.
   */
  public void addTreeChangeHandler(
      final EventHandler<TreeItem.TreeModificationEvent<Event>> handler ) {
    final TreeItem<String> root = getTreeView().getRoot();
    root.addEventHandler( TreeItem.valueChangedEvent(), handler );
    root.addEventHandler( TreeItem.childrenModificationEvent(), handler );
  }

  /**
   * Answers whether the {@link TreeItem}s in the {@link TreeView} are suitably
   * well-formed for export. A tree is considered well-formed if the following
   * conditions are met:
   *
   * <ul>
   *   <li>The root node contains at least one child node having a leaf.</li>
   *   <li>There are no leaf nodes with sibling leaf nodes.</li>
   * </ul>
   *
   * @return {@code null} if the document is well-formed, otherwise the
   * problematic child {@link TreeItem}.
   */
  public TreeItem<String> isTreeWellFormed() {
    final var root = getTreeView().getRoot();

    for( final var child : root.getChildren() ) {
      final var problemChild = isWellFormed( child );

      if( child.isLeaf() || problemChild != null ) {
        return problemChild;
      }
    }

    return null;
  }

  /**
   * Determines whether the document is well-formed by ensuring that
   * child branches do not contain multiple leaves.
   *
   * @param item The sub-tree to check for well-formedness.
   * @return {@code null} when the tree is well-formed, otherwise the
   * problematic {@link TreeItem}.
   */
  private TreeItem<String> isWellFormed( final TreeItem<String> item ) {
    int childLeafs = 0;
    int childBranches = 0;

    for( final TreeItem<String> child : item.getChildren() ) {
      if( child.isLeaf() ) {
        childLeafs++;
      }
      else {
        childBranches++;
      }

      final var problemChild = isWellFormed( child );

      if( problemChild != null ) {
        return problemChild;
      }
    }

    return ((childBranches > 0 && childLeafs == 0) ||
        (childBranches == 0 && childLeafs <= 1)) ? null : item;
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
    getSelectionModel().clearSelection();
    getSelectionModel().select( getTreeView().getRow( item ) );
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
  private <T> void collapse( final ObservableList<TreeItem<T>> nodes ) {
    for( final TreeItem<T> node : nodes ) {
      node.setExpanded( false );
      collapse( node.getChildren() );
    }
  }

  /**
   * @return {@code true} when the user is editing a {@link TreeItem}.
   */
  private boolean isEditingTreeItem() {
    return getTreeView().editingItemProperty().getValue() != null;
  }

  /**
   * Changes to edit mode for the selected item.
   */
  private void editSelectedItem() {
    getTreeView().edit( getSelectedItem() );
  }

  /**
   * Removes all selected items from the {@link TreeView}.
   */
  private void deleteSelectedItems() {
    for( final TreeItem<String> item : getSelectedItems() ) {
      final TreeItem<String> parent = item.getParent();

      if( parent != null ) {
        parent.getChildren().remove( item );
      }
    }
  }

  /**
   * Deletes the selected item.
   */
  private void deleteSelectedItem() {
    final TreeItem<String> c = getSelectedItem();
    getSiblings( c ).remove( c );
  }

  /**
   * Adds a new item under the selected item (or root if nothing is selected).
   * There are a few conditions to consider: when adding to the root,
   * when adding to a leaf, and when adding to a non-leaf. Items added to the
   * root must contain two items: a key and a value.
   */
  private void addItem() {
    final TreeItem<String> value = createTreeItem();
    getSelectedItem().getChildren().add( value );
    expand( value );
    select( value );
  }

  private ContextMenu createContextMenu() {
    final ContextMenu menu = new ContextMenu();
    final ObservableList<MenuItem> items = menu.getItems();

    addMenuItem( items, "Definition.menu.create" )
        .setOnAction( e -> addItem() );

    addMenuItem( items, "Definition.menu.rename" )
        .setOnAction( e -> editSelectedItem() );

    addMenuItem( items, "Definition.menu.remove" )
        .setOnAction( e -> deleteSelectedItem() );

    return menu;
  }

  /**
   * Executes hot-keys for edits to the definition tree.
   *
   * @param event Contains the key code of the key that was pressed.
   */
  private void keyEventFilter( final KeyEvent event ) {
    if( !isEditingTreeItem() ) {
      switch( event.getCode() ) {
        case ENTER:
          expand( getSelectedItem() );
          event.consume();

          break;

        case DELETE:
          deleteSelectedItems();
          break;

        case INSERT:
          addItem();
          break;

        case R:
          if( event.isControlDown() ) {
            editSelectedItem();
          }

          break;
      }
    }
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
        select( getTreeItem() );
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

  /**
   * Returns the tree view that contains the definition hierarchy.
   *
   * @return A non-null instance.
   */
  public TreeView<String> getTreeView() {
    return mTreeView;
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
   * Returns the root of the tree.
   *
   * @return The first node added to the definition tree.
   */
  private VariableTreeItem<String> getTreeRoot() {
    final TreeItem<String> root = getTreeView().getRoot();

    return root instanceof VariableTreeItem ?
        (VariableTreeItem<String>) root : new VariableTreeItem<>( "root" );
  }

  private ObservableList<TreeItem<String>> getSiblings(
      final TreeItem<String> item ) {
    final TreeItem<String> root = getTreeView().getRoot();
    final TreeItem<String> parent =
        (item == null || item == root) ? root : item.getParent();

    return parent.getChildren();
  }

  private MultipleSelectionModel<TreeItem<String>> getSelectionModel() {
    return getTreeView().getSelectionModel();
  }

  /**
   * Returns a copy of all the selected items.
   *
   * @return A list, possibly empty, containing all selected items in the
   * {@link TreeView}.
   */
  private List<TreeItem<String>> getSelectedItems() {
    return new LinkedList<>( getSelectionModel().getSelectedItems() );
  }

  private TreeItem<String> getSelectedItem() {
    final TreeItem<String> item = getSelectionModel().getSelectedItem();
    return item == null ? getTreeView().getRoot() : item;
  }
}
