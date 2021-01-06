/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import javafx.scene.control.TreeItem;

/**
 * Responsible for converting an object hierarchy into a {@link TreeItem}
 * hierarchy.
 */
public interface TreeTransformer {
  /**
   * Adapts the document produced by the given parser into a {@link TreeItem}
   * object that can be presented to the user within a GUI. The root of the
   * tree must be merged by the view layer.
   *
   * @param document The document to transform into a viewable hierarchy.
   */
  TreeItem<String> transform( String document );

  /**
   * Exports the given root node to the given path.
   *
   * @param root The root node to export.
   */
  String transform( TreeItem<String> root );
}
