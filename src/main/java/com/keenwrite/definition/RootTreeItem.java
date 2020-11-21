/* Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.definition;

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
   * @see TreeItemMapper#toMap(TreeItem) for details on how this
   * class is used.
   */
  public RootTreeItem( final T value ) {
    super( value );
  }
}
