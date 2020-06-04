package com.scrivenvar.definition;

import javafx.scene.control.TreeItem;

import java.nio.file.Path;

public interface TreeAdapter {
  /**
   * Adapts the document produced by the given parser into a {@link TreeItem}
   * object that can be presented to the user within a GUI.
   *
   * @param root The default root node name.
   * @return The parsed document in a {@link TreeItem} that can be displayed
   * in a panel.
   */
  TreeItem<String> adapt( String root );

  /**
   * Exports the given root node to the given path.
   *
   * @param root The root node to export.
   * @param path Where to persist the data.
   */
  void export( TreeItem<String> root, Path path );

}
