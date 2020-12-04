/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.definition;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Responsible for converting an object hierarchy into a {@link TreeItem}
 * hierarchy.
 */
public interface TreeTransformer extends
    Function<String, TreeItem<String>>,
    BiConsumer<TreeItem<String>, Path> {
  /**
   * Adapts the document produced by the given parser into a {@link TreeItem}
   * object that can be presented to the user within a GUI. The root of the
   * tree must be merged by the view layer.
   *
   * @param document The document to transform into a viewable hierarchy.
   */
  @Override
  TreeItem<String> apply( String document );

  /**
   * Exports the given root node to the given path.
   *
   * @param root The root node to export.
   * @param path Where to persist the data.
   * @throws RuntimeException Could not write the data to the given path.
   */
  @Override
  void accept( TreeItem<String> root, Path path );
}
