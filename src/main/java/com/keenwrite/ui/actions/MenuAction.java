/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;

/**
 * Implementations are responsible for creating menu items and toolbar buttons.
 */
public interface MenuAction {
  /**
   * Creates a menu item based on the {@link Action} parameters.
   *
   * @return A new {@link MenuItem} instance.
   */
  MenuItem createMenuItem();

  /**
   * Creates an instance of {@link Button} or {@link Separator} based on the
   * {@link Action} parameters.
   *
   * @return A new {@link Button} or {@link Separator} instance.
   */
  Node createToolBarNode();
}
