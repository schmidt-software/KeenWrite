/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.definition;

import com.keenwrite.TextResource;
import com.keenwrite.editors.TextDefinition;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.*;

import static com.keenwrite.Messages.get;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.TOP_CENTER;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.TreeItem.childrenModificationEvent;
import static javafx.scene.control.TreeItem.valueChangedEvent;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * Provides the user interface that holds a {@link TreeView}, which
 * allows users to interact with key/value pairs loaded from the
 * document parser and adapted using a {@link TreeTransformer}.
 */
public final class DefinitionEditor extends BorderPane implements
    TextDefinition {

  /**
   * Contains the root that is added to the view.
   */
  private final DefinitionTreeItem<String> mTreeRoot = createRootTreeItem();

  /**
   * Contains a view of the definitions.
   */
  private final TreeView<String> mTreeView = new TreeView<>( mTreeRoot );

  /**
   * Used to adapt the structured document into a {@link TreeView}.
   */
  private final TreeTransformer mTreeTransformer;

  /**
   * Handlers for key press events.
   */
  private final Set<EventHandler<? super KeyEvent>> mKeyEventHandlers
      = new HashSet<>();

  /**
   * Constructs a definition pane with a given tree view root.
   */
  public DefinitionEditor( final TreeTransformer treeTransformer ) {
    mTreeTransformer = treeTransformer;

    mTreeView.setEditable( true );
    mTreeView.setCellFactory( new TreeCellFactory(  ) );
    mTreeView.setContextMenu( createContextMenu() );
    mTreeView.addEventFilter( KEY_PRESSED, this::keyEventFilter );
    mTreeView.setShowRoot( false );
    getSelectionModel().setSelectionMode( MULTIPLE );

    final var bCreate = createButton(
        "create", TREE, e -> addItem() );
    final var bRename = createButton(
        "rename", EDIT, e -> editSelectedItem() );
    final var bDelete = createButton(
        "delete", TRASH, e -> deleteSelectedItems() );

    final var buttonBar = new HBox();
    buttonBar.getChildren().addAll( bCreate, bRename, bDelete );
    buttonBar.setAlignment( CENTER );
    buttonBar.setSpacing( 10 );

    setTop( buttonBar );
    setCenter( mTreeView );
    setAlignment( buttonBar, TOP_CENTER );
  }

  @Override
  public void setText( final String document ) {
    final var foster = mTreeTransformer.apply( document );
    final var biological = getTreeRoot();

    for( final var child : foster.getChildren() ) {
      biological.getChildren().add( child );
    }

    getTreeView().refresh();
  }

  @Override
  public String getText() {
    return "";
  }

  @Override
  public Node getNode() {
    return this;
  }

  private Button createButton(
      final String msgKey,
      final FontAwesomeIcon icon,
      final EventHandler<ActionEvent> eventHandler ) {
    final var keyPrefix = "Pane.definition.button." + msgKey;
    final var button = new Button( get( keyPrefix + ".label" ) );
    button.setOnAction( eventHandler );

    button.setGraphic(
        FontAwesomeIconFactory.get().createIcon( icon )
    );
    button.setTooltip( new Tooltip( get( keyPrefix + ".tooltip" ) ) );

    return button;
  }

  public Map<String, String> toMap() {
    return TreeItemMapper.toMap( getTreeView().getRoot() );
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
    final var root = getTreeView().getRoot();
    root.addEventHandler( valueChangedEvent(), handler );
    root.addEventHandler( childrenModificationEvent(), handler );
  }

  public void addKeyEventHandler(
      final EventHandler<? super KeyEvent> handler ) {
    getKeyEventHandlers().add( handler );
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

    for( final var child : item.getChildren() ) {
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
   * Delegates to {@link DefinitionTreeItem#findLeafExact(String)}.
   *
   * @param text The value to find, never {@code null}.
   * @return The leaf that contains the given value, or {@code null} if
   * not found.
   */
  public DefinitionTreeItem<String> findLeafExact( final String text ) {
    return getTreeRoot().findLeafExact( text );
  }

  /**
   * Delegates to {@link DefinitionTreeItem#findLeafContains(String)}.
   *
   * @param text The value to find, never {@code null}.
   * @return The leaf that contains the given value, or {@code null} if
   * not found.
   */
  public DefinitionTreeItem<String> findLeafContains( final String text ) {
    return getTreeRoot().findLeafContains( text );
  }

  /**
   * Delegates to {@link DefinitionTreeItem#findLeafContains(String)}.
   *
   * @param text The value to find, never {@code null}.
   * @return The leaf that contains the given value, or {@code null} if
   * not found.
   */
  public DefinitionTreeItem<String> findLeafContainsNoCase(
      final String text ) {
    return getTreeRoot().findLeafContainsNoCase( text );
  }

  /**
   * Delegates to {@link DefinitionTreeItem#findLeafStartsWith(String)}.
   *
   * @param text The value to find, never {@code null}.
   * @return The leaf that contains the given value, or {@code null} if
   * not found.
   */
  public DefinitionTreeItem<String> findLeafStartsWith( final String text ) {
    return getTreeRoot().findLeafStartsWith( text );
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
    for( final var node : nodes ) {
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
    for( final var item : getSelectedItems() ) {
      final var parent = item.getParent();

      if( parent != null ) {
        parent.getChildren().remove( item );
      }
    }
  }

  /**
   * Deletes the selected item.
   */
  private void deleteSelectedItem() {
    final var c = getSelectedItem();
    getSiblings( c ).remove( c );
  }

  /**
   * Adds a new item under the selected item (or root if nothing is selected).
   * There are a few conditions to consider: when adding to the root,
   * when adding to a leaf, and when adding to a non-leaf. Items added to the
   * root must contain two items: a key and a value.
   */
  public void addItem() {
    final var value = createDefinitionTreeItem();
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
        case ENTER -> {
          expand( getSelectedItem() );
          event.consume();
        }

        case DELETE -> deleteSelectedItems();
        case INSERT -> addItem();

        case R -> {
          if( event.isControlDown() ) {
            editSelectedItem();
          }
        }
      }

      for( final var handler : getKeyEventHandlers() ) {
        handler.handle( event );
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

  /**
   * Creates a new {@link TreeItem} that is intended to be the root-level item
   * added to the {@link TreeView}. This allows the root item to be
   * distinguished from the other items so that reference keys do not include
   * "Definition" as part of their name.
   *
   * @return A new {@link TreeItem}, never {@code null}.
   */
  private RootTreeItem<String> createRootTreeItem() {
    return new RootTreeItem<>( get( "Pane.definition.node.root.title" ) );
  }

  private DefinitionTreeItem<String> createDefinitionTreeItem() {
    return new DefinitionTreeItem<>( get( "Definition.menu.add.default" ) );
  }

  @Override
  public void requestFocus() {
    super.requestFocus();
    getTreeView().requestFocus();
  }

  /**
   * Answers whether there are any definitions in the tree.
   *
   * @return {@code true} when there are no definitions; {@code false} when
   * there's at least one definition.
   */
  public boolean isEmpty() {
    return getTreeRoot().isEmpty();
  }

  /**
   * Returns the actively selected item in the tree.
   *
   * @return The selected item, or the tree root item if no item is selected.
   */
  public TreeItem<String> getSelectedItem() {
    final var item = getSelectionModel().getSelectedItem();
    return item == null ? getTreeRoot() : item;
  }

  /**
   * Returns the {@link TreeView} that contains the definition hierarchy.
   *
   * @return A non-null instance.
   */
  private TreeView<String> getTreeView() {
    return mTreeView;
  }

  /**
   * Returns the root of the tree.
   *
   * @return The first node added to the definition tree.
   */
  private DefinitionTreeItem<String> getTreeRoot() {
    return mTreeRoot;
  }

  private ObservableList<TreeItem<String>> getSiblings(
      final TreeItem<String> item ) {
    final var root = getTreeView().getRoot();
    final var parent = (item == null || item == root) ? root : item.getParent();

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
    return new ArrayList<>( getSelectionModel().getSelectedItems() );
  }

  private Set<EventHandler<? super KeyEvent>> getKeyEventHandlers() {
    return mKeyEventHandlers;
  }
}
