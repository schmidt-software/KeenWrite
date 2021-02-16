/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.tree;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Responsible for creating new {@link TreeCell} instances.
 * <p>
 * TODO: #22 -- Upon refactoring variable functionality, re-instate drag & drop.
 * </p>
 *
 * @param <T> The data type stored in the tree.
 */
public class AltTreeCellFactory<T>
  implements Callback<TreeView<T>, TreeCell<T>> {
  private final StringConverter<T> mConverter;

  public AltTreeCellFactory( final StringConverter<T> converter ) {
    mConverter = converter;
  }

  @Override
  public TreeCell<T> call( final TreeView<T> treeView ) {
    return new AltTreeCell<>( mConverter );
  }
}
