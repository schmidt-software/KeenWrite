/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.editors.definition;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Marker interface for top-most {@link TreeItem}. This class allows the
 * {@link TreeItemMapper} to ignore the topmost definition. Such contortions
 * are necessary because {@link TreeView} requires a root item that isn't part
 * of the user's definition file.
 * <p>
 * Another approach would be to associate object pairs per {@link TreeItem},
 * but that would be a waste of memory since the only "exception" case is
 * the root {@link TreeItem}.
 * </p>
 *
 * @param <T> The type of {@link TreeItem} to store in the {@link TreeView}.
 */
public final class RootTreeItem<T> extends DefinitionTreeItem<T> {
  /**
   * Default constructor, calls the superclass, no other behaviour.
   *
   * @param value The {@link TreeItem} node name to construct the superclass.
   * @see TreeItemMapper#convert(TreeItem) for details on how this
   * class is used.
   */
  public RootTreeItem( final T value ) {
    super( value );
  }
}
