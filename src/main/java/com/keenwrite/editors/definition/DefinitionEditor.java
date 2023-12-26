/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.editors.definition;

import com.keenwrite.constants.Constants;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.events.InsertDefinitionEvent;
import com.keenwrite.events.TextDefinitionFocusEvent;
import com.keenwrite.processors.r.Engine;
import com.keenwrite.ui.tree.AltTreeView;
import com.keenwrite.ui.tree.TreeItemConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
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
public final class DefinitionEditor extends BorderPane
  implements TextDefinition {

  /**
   * Contains the root that is added to the view.
   */
  private final DefinitionTreeItem<String> mTreeRoot = createRootTreeItem();

  /**
   * Contains a view of the definitions.
   */
  private final TreeView<String> mTreeView =
    new AltTreeView<>( mTreeRoot, new TreeItemConverter() );

  /**
   * Used to adapt the structured document into a {@link TreeView}.
   */
  private final TreeTransformer mTreeTransformer;

  /**
   * Handlers for key press events.
   */
  private final Set<EventHandler<? super KeyEvent>> mKeyEventHandlers
    = new HashSet<>();

  private final Map<String, String> mDefinitions = new HashMap<>();

  /**
   * Opened file's character encoding, or {@link Constants#DEFAULT_CHARSET} if
   * either no encoding could be determined or this is a new (empty) file.
   */
  private final Charset mEncoding;

  /**
   * Tracks whether the in-memory definitions have changed with respect to the
   * persisted definitions.
   */
  private final BooleanProperty mModified = new SimpleBooleanProperty();

  /**
   * File being edited by this editor instance, which may be renamed.
   */
  private File mFile;

  /**
   * This is provided for unit tests that are not backed by files.
   *
   * @param treeTransformer Responsible for transforming the definitions into
   *                        {@link TreeItem} instances.
   */
  public DefinitionEditor(
    final TreeTransformer treeTransformer ) {
    this( DEFINITION_DEFAULT, treeTransformer );
  }

  /**
   * Constructs a definition pane with a given tree view root.
   *
   * @param file The file of definitions to maintain through the UI.
   */
  public DefinitionEditor(
    final File file,
    final TreeTransformer treeTransformer ) {
    assert file != null;
    assert treeTransformer != null;

    mFile = file;
    mTreeTransformer = treeTransformer;

    mTreeView.setContextMenu( createContextMenu() );
    mTreeView.addEventFilter( KEY_PRESSED, this::keyEventFilter );
    mTreeView.focusedProperty().addListener( this::focused );
    getSelectionModel().setSelectionMode( MULTIPLE );

    final var buttonBar = new HBox();
    buttonBar.getChildren().addAll(
      createButton( "create", _ -> createDefinition() ),
      createButton( "rename", _ -> renameDefinition() ),
      createButton( "delete", _ -> deleteDefinitions() )
    );
    buttonBar.setAlignment( CENTER );
    buttonBar.setSpacing( UI_CONTROL_SPACING );
    setTop( buttonBar );
    setCenter( mTreeView );
    setAlignment( buttonBar, TOP_CENTER );

    mEncoding = open( mFile );
    updateDefinitions( getDefinitions(), getTreeView().getRoot() );

    // After the file is opened, watch for changes, not before. Otherwise,
    // upon saving, users will be prompted to save a file that hasn't had
    // any modifications (from their perspective).
    addTreeChangeHandler( _ -> {
      mModified.set( true );
      updateDefinitions( getDefinitions(), getTreeView().getRoot() );
    } );
  }

  /**
   * Replaces the given list of variable definitions with a flat hierarchy
   * of the converted {@link TreeView} root.
   *
   * @param definitions The definition map to update.
   * @param root        The values to flatten then insert into the map.
   */
  private void updateDefinitions(
    final Map<String, String> definitions,
    final TreeItem<String> root ) {
    definitions.clear();
    definitions.putAll( TreeItemMapper.convert( root ) );
    Engine.clear();
  }

  /**
   * Returns the variable definitions.
   *
   * @return The definition map.
   */
  @Override
  public Map<String, String> getDefinitions() {
    return mDefinitions;
  }

  @Override
  public void setText( final String document ) {
    final var foster = mTreeTransformer.transform( document );
    final var biological = getTreeRoot();

    for( final var child : foster.getChildren() ) {
      biological.getChildren().add( child );
    }

    getTreeView().refresh();
  }

  @Override
  public String getText() {
    final var result = new StringBuilder( 32768 );

    try {
      result.append( mTreeTransformer.transform( getTreeView().getRoot() ) );

      final var problem = isTreeWellFormed();
      problem.ifPresent( node -> clue( "yaml.error.tree.form", node ) );
    } catch( final Exception ex ) {
      // Catch errors while checking for a well-formed tree (e.g., stack smash).
      // Also catch any transformation exceptions (e.g., Json processing).
      clue( ex );
    }

    return result.toString();
  }

  @Override
  public File getFile() {
    return mFile;
  }

  @Override
  public void rename( final File file ) {
    mFile = file;
  }

  @Override
  public Charset getEncoding() {
    return mEncoding;
  }

  @Override
  public Node getNode() {
    return this;
  }

  @Override
  public ReadOnlyBooleanProperty modifiedProperty() {
    return mModified;
  }

  @Override
  public void clearModifiedProperty() {
    mModified.setValue( false );
  }

  private Button createButton(
    final String msgKey, final EventHandler<ActionEvent> eventHandler ) {
    final var keyPrefix = STR."\{Constants.ACTION_PREFIX}definition.\{msgKey}";
    final var button = new Button( get( STR."\{keyPrefix}.text" ) );
    final var graphic = createGraphic( get( STR."\{keyPrefix}.icon" ) );

    button.setOnAction( eventHandler );
    button.setGraphic( graphic );
    button.setTooltip( new Tooltip( get( STR."\{keyPrefix}.tooltip" ) ) );

    return button;
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
  public Optional<TreeItem<String>> isTreeWellFormed() {
    final var root = getTreeView().getRoot();

    for( final var child : root.getChildren() ) {
      final var problemChild = isWellFormed( child );

      if( child.isLeaf() || problemChild != null ) {
        return Optional.ofNullable( problemChild );
      }
    }

    return Optional.empty();
  }

  /**
   * Determines whether the document is well-formed by ensuring that
   * child branches do not contain multiple leaves.
   *
   * @param item The subtree to check for well-formedness.
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
            (childBranches == 0 && childLeafs <= 1))
      ? null
      : item;
  }

  @Override
  public DefinitionTreeItem<String> findLeafExact( final String text ) {
    return getTreeRoot().findLeafExact( text );
  }

  @Override
  public DefinitionTreeItem<String> findLeafContains( final String text ) {
    return getTreeRoot().findLeafContains( text );
  }

  @Override
  public DefinitionTreeItem<String> findLeafContainsNoCase(
    final String text ) {
    return getTreeRoot().findLeafContainsNoCase( text );
  }

  @Override
  public DefinitionTreeItem<String> findLeafStartsWith( final String text ) {
    return getTreeRoot().findLeafStartsWith( text );
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
  @Override
  public void renameDefinition() {
    getTreeView().edit( getSelectedItem() );
  }

  /**
   * Removes all selected items from the {@link TreeView}.
   */
  @Override
  public void deleteDefinitions() {
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

  private void insertSelectedItem() {
    if( getSelectedItem() instanceof DefinitionTreeItem<String> node ) {
      if( node.isLeaf() ) {
        InsertDefinitionEvent.fire( node );
      }
    }
  }

  /**
   * Adds a new item under the selected item (or root if nothing is selected).
   * There are a few conditions to consider: when adding to the root,
   * when adding to a leaf, and when adding to a non-leaf. Items added to the
   * root must contain two items: a key and a value.
   */
  @Override
  public void createDefinition() {
    final var value = createDefinitionTreeItem();
    getSelectedItem().getChildren().add( value );
    expand( value );
    select( value );
  }

  private ContextMenu createContextMenu() {
    final var menu = new ContextMenu();
    final var items = menu.getItems();

    addMenuItem( items, STR."\{ACTION_PREFIX}definition.create.text" )
      .setOnAction( _ -> createDefinition() );
    addMenuItem( items, STR."\{ACTION_PREFIX}definition.rename.text" )
      .setOnAction( _ -> renameDefinition() );
    addMenuItem( items, STR."\{ACTION_PREFIX}definition.delete.text" )
      .setOnAction( _ -> deleteSelectedItem() );
    addMenuItem( items, STR."\{ACTION_PREFIX}definition.insert.text" )
      .setOnAction( _ -> insertSelectedItem() );

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

        case DELETE -> deleteDefinitions();
        case INSERT -> createDefinition();

        case R -> {
          if( event.isControlDown() ) {
            renameDefinition();
          }
        }

        default -> {}
      }

      for( final var handler : getKeyEventHandlers() ) {
        handler.handle( event );
      }
    }
  }

  /**
   * Called when the editor's input focus changes. This will fire an event
   * for subscribers.
   *
   * @param ignored Not used.
   * @param o       The old input focus property value.
   * @param n       The new input focus property value.
   */
  private void focused(
    final ObservableValue<? extends Boolean> ignored,
    final Boolean o,
    final Boolean n ) {
    if( n != null && n ) {
      TextDefinitionFocusEvent.fire( this );
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
    getTreeView().requestFocus();
  }

  /**
   * Expands the node to the root, recursively.
   *
   * @param <T>  The type of tree item to expand (usually String).
   * @param node The node to expand.
   */
  @Override
  public <T> void expand( final TreeItem<T> node ) {
    if( node != null ) {
      expand( node.getParent() );
      node.setExpanded( !node.isLeaf() );
    }
  }

  /**
   * Answers whether there are any definitions in the tree.
   *
   * @return {@code true} when there are no definitions; {@code false} when
   * there's at least one definition.
   */
  @Override
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
    final var parent = (item == null || item == root)
      ? root
      : item.getParent();

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
