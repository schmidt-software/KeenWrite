/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.tree;

import com.keenwrite.ui.cells.AltTreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.StringConverter;

/**
 * Responsible for allowing users to edit items in the tree as well as
 * drag and drop. The goal is to be a drop-in replacement for the regular
 * JavaFX {@link TreeView}, which does not offer editing and moving {@link
 * TreeItem} instances.
 *
 * @param <T> The type of data to edit.
 */
public class AltTreeView<T> extends TreeView<T> {
  public AltTreeView(
    final TreeItem<T> root, final StringConverter<T> converter ) {
    super( root );

    assert root != null;
    assert converter != null;

    setEditable( true );
    setCellFactory( treeView -> new AltTreeCell<>( converter ) );
    setShowRoot( false );

    // When focus is lost while not editing, deselect all items.
    focusedProperty().addListener( ( c, o, n ) -> {
      if( o && getEditingItem() == null ) {
        getSelectionModel().clearSelection();
      }
    } );
  }
}
