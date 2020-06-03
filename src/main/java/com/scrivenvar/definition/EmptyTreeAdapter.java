package com.scrivenvar.definition;

import javafx.scene.control.TreeItem;

/**
 * Facilitates adapting empty documents into a single node object model.
 */
public class EmptyTreeAdapter implements TreeAdapter {
  @Override
  public TreeItem<String> adapt( String root ) {
    return new TreeItem<>( root );
  }
}
