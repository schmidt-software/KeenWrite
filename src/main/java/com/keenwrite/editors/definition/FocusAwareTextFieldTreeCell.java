/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.definition;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.util.StringConverter;

/**
 * Responsible for fixing a focus lost bug in the JavaFX implementation.
 * See https://bugs.openjdk.java.net/browse/JDK-8089514 for details.
 * This implementation borrows from the official documentation on creating
 * tree views: https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm
 */
public class FocusAwareTextFieldTreeCell extends TextFieldTreeCell<String> {
  private TextField mTextField;

  public FocusAwareTextFieldTreeCell(
      final StringConverter<String> converter ) {
    super( converter );
  }

  @Override
  public void startEdit() {
    super.startEdit();
    var textField = mTextField;

    if( textField == null ) {
      textField = createTextField();
    }
    else {
      textField.setText( getItem() );
    }

    setText( null );
    setGraphic( textField );
    textField.selectAll();
    textField.requestFocus();

    // When the focus is lost, commit the edit then close the input field.
    // This fixes the unexpected behaviour when user clicks away.
    textField.focusedProperty().addListener( ( l, o, n ) -> {
      if( !n ) {
        commitEdit( mTextField.getText() );
      }
    } );

    mTextField = textField;
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

    String text = null;
    Node graphic = null;

    if( !empty ) {
      if( isEditing() ) {
        final var textField = mTextField;

        if( textField != null ) {
          textField.setText( getString() );
        }

        graphic = textField;
      }
      else {
        text = getString();
        graphic = getTreeItem().getGraphic();
      }
    }

    setText( text );
    setGraphic( graphic );
  }

  private TextField createTextField() {
    final var textField = new TextField( getString() );

    textField.setOnKeyReleased( t -> {
      switch( t.getCode() ) {
        case ENTER -> commitEdit( textField.getText() );
        case ESCAPE -> cancelEdit();
      }
    } );

    return textField;
  }

  private String getString() {
    return getConverter().toString( getItem() );
  }
}
