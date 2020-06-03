package com.scrivenvar.definition;

import javafx.scene.control.TreeItem;

public interface TreeAdapter {
  /**
   * Adapts the document produced by the given parser into a {@link TreeItem}
   * object that can be presented to the user within a GUI.
   *
   * @param root The default root node name.
   * @return The parsed document in a {@link TreeItem} that can be displayed
   * in a panel.
   */
  TreeItem<String> adapt( final String root );
}
