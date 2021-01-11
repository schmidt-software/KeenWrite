/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import javafx.scene.Node;
import javafx.scene.control.*;

/**
 * Represents a {@link MenuBar} or {@link ToolBar} action that has no
 * operation, acting as a placeholder for line separators.
 */
public final class SeparatorAction implements MenuAction {
  @Override
  public MenuItem createMenuItem() {
    return new SeparatorMenuItem();
  }

  @Override
  public Node createToolBarNode() {
    return new Separator();
  }
}
