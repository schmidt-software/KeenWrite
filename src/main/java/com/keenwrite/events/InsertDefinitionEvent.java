/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events;

import com.keenwrite.editors.definition.DefinitionTreeItem;

/**
 * Collates information about a request to insert a reference to a
 * definition value into the active document.
 */
public class InsertDefinitionEvent<T> implements AppEvent {

  private final DefinitionTreeItem<T> mLeaf;

  private InsertDefinitionEvent( final DefinitionTreeItem<T> leaf ) {
    mLeaf = leaf;
  }

  public static <T> void fire( final DefinitionTreeItem<T> leaf ) {
    assert leaf != null;
    assert leaf.isLeaf();

    new InsertDefinitionEvent<>( leaf ).publish();
  }

  /**
   * Returns the {@link DefinitionTreeItem} that is to be inserted into the
   * active document.
   *
   * @return The item to insert (as a variable).
   */
  public DefinitionTreeItem<T> getLeaf() {
    return mLeaf;
  }
}
