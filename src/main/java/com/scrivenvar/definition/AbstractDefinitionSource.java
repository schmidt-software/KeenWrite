/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.definition;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

import java.util.List;

import static com.scrivenvar.Messages.get;

/**
 * Implements common behaviour for definition sources.
 *
 * @author White Magic Software, Ltd.
 */
public abstract class AbstractDefinitionSource implements DefinitionSource {

  private TreeView<String> mTreeView;

  /**
   * Returns this definition source as an editable graphical user interface
   * component.
   *
   * @return The TreeView for this definition source.
   */
  @Override
  public TreeView<String> asTreeView() {
    if( mTreeView == null ) {
      mTreeView = createTreeView();
      mTreeView.setContextMenu( createContextMenu( mTreeView ) );
      mTreeView.setEditable( true );
      mTreeView.setCellFactory( treeView -> createTreeCell() );
    }

    return mTreeView;
  }

  /**
   * Creates a newly instantiated tree view ready for adding to the definition
   * pane.
   *
   * @return A new tree view instance, never null.
   */
  protected abstract TreeView<String> createTreeView();

  private ContextMenu createContextMenu( final TreeView<String> treeView ) {
    final ContextMenu menu = new ContextMenu();
    final ObservableList<MenuItem> items = menu.getItems();

    addMenuItem( items, "Definition.menu.create" ).setOnAction(
        e -> getSiblings( getSelectedItem() ).add( createTreeItem() )
    );

    addMenuItem( items, "Definition.menu.rename" ).setOnAction(
        e -> treeView.edit( getSelectedItem() )
    );

    addMenuItem( items, "Definition.menu.remove" ).setOnAction(
        e -> {
          final TreeItem<String> c = getSelectedItem();
          getSiblings( c ).remove( c );
        }
    );

    return menu;
  }

  private ObservableList<TreeItem<String>> getSiblings(
      final TreeItem<String> item ) {
    final TreeItem<String> root = getTreeView().getRoot();
    final TreeItem<String> parent = item == root ? item : item.getParent();

    return parent.getChildren();
  }

  private MenuItem addMenuItem(
      final List<MenuItem> items, final String labelKey ) {
    final MenuItem menuItem = createMenuItem( labelKey );
    items.add( menuItem );
    return menuItem;
  }

  private MenuItem createMenuItem( final String labelKey ) {
    return new MenuItem( get( labelKey ) );
  }

  private VariableTreeItem<String> createTreeItem() {
    return new VariableTreeItem<>( get( "Definition.menu.add.default" ) );
  }

  private TreeCell<String> createTreeCell() {
    return new TextFieldTreeCell<>( createStringConverter() );
  }

  private StringConverter<String> createStringConverter() {
    return new StringConverter<>() {
      @Override
      public String toString( final String object ) {
        return object == null ? "" : object;
      }

      @Override
      public String fromString( final String string ) {
        return string == null ? "" : string;
      }
    };
  }

  private TreeView<String> getTreeView() {
    return mTreeView;
  }

  private TreeItem<String> getSelectedItem() {
    return getTreeView().getSelectionModel().getSelectedItem();
  }

  /**
   * Prevent an {@link EmptyDefinitionSource} from being saved literally as its
   * memory reference (the default value returned by {@link Object#toString()}).
   *
   * @return Empty string.
   */
  @Override
  public String toString() {
    return "";
  }

}
