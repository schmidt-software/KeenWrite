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

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import static com.scrivenvar.Messages.get;

/**
 * Provides behaviour of adding, removing, and editing tree view items.
 *
 * @author White Magic Software, Ltd.
 */
public class TextFieldTreeCell extends TreeCell<String> {

  private TextField textField;
  private final ContextMenu editMenu = new ContextMenu();

  public TextFieldTreeCell() {
    initEditMenu();
  }

  private void initEditMenu() {
    final MenuItem addItem = createMenuItem( "Definition.menu.add" );
    final MenuItem removeItem = createMenuItem( "Definition.menu.remove" );

    addItem.setOnAction( ( ActionEvent e ) -> {
      final VariableTreeItem<String> treeItem = new VariableTreeItem<>(
          "Undefined" );
      getTreeItem().getChildren().add( treeItem );
    } );

    removeItem.setOnAction( ( ActionEvent e ) -> {
      final TreeItem<?> c = getTreeItem();
      c.getParent().getChildren().remove( c );
    } );

    getEditMenu().getItems().add( addItem );
    getEditMenu().getItems().add( removeItem );
  }

  private ContextMenu getEditMenu() {
    return this.editMenu;
  }

  private MenuItem createMenuItem( String label ) {
    return new MenuItem( get( label ) );
  }

  @Override
  public void startEdit() {
    if( getTreeItem().isLeaf() ) {
      super.startEdit();

      final TextField inputField = getTextField();

      setText( null );
      setGraphic( inputField );
      inputField.selectAll();
      inputField.requestFocus();
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    setText( getItem() );
    setGraphic( getTreeItem().getGraphic() );
  }

  @Override
  public void updateItem( String item, boolean empty ) {
    super.updateItem( item, empty );

    if( empty ) {
      setText( null );
      setGraphic( null );
    }
    else if( isEditing() ) {
      TextField tf = getTextField();
      tf.setText( getItemValue() );

      setText( null );
      setGraphic( tf );
    }
    else {
      setText( getItemValue() );
      setGraphic( getTreeItem().getGraphic() );

      if( !getTreeItem().isLeaf() && getTreeItem().getParent() != null ) {
        setContextMenu( getEditMenu() );
      }
    }
  }

  private TextField createTextField() {
    final TextField tf = new TextField( getItemValue() );

    tf.setOnKeyReleased( ( KeyEvent t ) -> {
      switch( t.getCode() ) {
        case ENTER:
          commitEdit( tf.getText() );
          break;
        case ESCAPE:
          cancelEdit();
          break;
      }
    } );

    return tf;
  }

  /**
   * Returns the item's text value.
   *
   * @return A non-null String, possibly empty.
   */
  private String getItemValue() {
    return getItem() == null ? "" : getItem();
  }

  private TextField getTextField() {
    if( this.textField == null ) {
      this.textField = createTextField();
    }

    return this.textField;
  }
}
