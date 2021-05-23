/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.tree;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import static javafx.application.Platform.runLater;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

/**
 * Responsible for enhancing the existing cell behaviour with fairly common
 * functionality, including commit on focus loss and Enter to commit.
 *
 * @param <T> The type of data stored by the tree.
 */
public class AltTreeCell<T> extends TextFieldTreeCell<T> {
  private final KeyHandler mKeyHandler = new KeyHandler();
  private final Property<String> mInputText = new SimpleStringProperty();
  private FocusListener mFocusListener;

  public AltTreeCell( final StringConverter<T> converter ) {
    super( converter );
    assert converter != null;

    // When the text field is added as the graphics context, we hook into
    // the changed value to get a handle on the text field. From there it is
    // possible to add change the keyboard and focus behaviours.
    graphicProperty().addListener( ( c, o, n ) -> {
      if( o instanceof TextField ) {
        o.removeEventHandler( KEY_RELEASED, mKeyHandler );
        o.focusedProperty().removeListener( mFocusListener );
      }

      if( n instanceof final TextField input ) {
        n.addEventFilter( KEY_RELEASED, mKeyHandler );
        mInputText.bind( input.textProperty() );
        mFocusListener = new FocusListener( input );
        n.focusedProperty().addListener( mFocusListener );
      }
    } );
  }

  private void commitEdit() {
    commitEdit( getConverter().fromString( mInputText.getValue() ) );
  }

  /**
   * Responsible for accepting the text when users press the Enter or Tab key.
   */
  private class KeyHandler implements EventHandler<KeyEvent> {
    @Override
    public void handle( final KeyEvent event ) {
      if( event.getCode() == ENTER || event.getCode() == TAB ) {
        commitEdit();
        event.consume();
      }
    }
  }

  /**
   * Responsible for committing edits when focus is lost. This will also
   * deselect the input field when focus is gained so that typing text won't
   * overwrite the entire existing text.
   */
  private class FocusListener implements ChangeListener<Boolean> {
    private final TextField mInput;

    private FocusListener( final TextField input ) {
      mInput = input;
    }

    @Override
    public void changed(
      final ObservableValue<? extends Boolean> c,
      final Boolean endedFocus, final Boolean beganFocus ) {

      if( beganFocus ) {
        runLater( mInput::deselect );
      }
      else if( endedFocus ) {
        commitEdit();
      }
    }
  }
}
