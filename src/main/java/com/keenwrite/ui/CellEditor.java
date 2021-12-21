package com.keenwrite.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyEvent;

import java.util.function.Consumer;

import static javafx.application.Platform.runLater;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

public class CellEditor {
  private FocusListener mFocusListener;
  private final Property<String> mInputText = new SimpleStringProperty();
  private final Consumer<String> mConsumer;

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

  /**
   * Generalized cell editor suitable for use with {@link TableCell} or
   * {@link TreeCell} instances.
   *
   * @param consumer        Converts the field input text to the required
   *                        data type.
   * @param graphicProperty Defines the graphical user input field.
   */
  public CellEditor(
    final Consumer<String> consumer,
    final ObjectProperty<Node> graphicProperty ) {
    assert consumer != null;
    mConsumer = consumer;

    init( graphicProperty );
  }

  private void init( final ObjectProperty<Node> graphicProperty ) {
    final var keyHandler = new KeyHandler();

    // When the text field is added as the graphics context, we hook into
    // the changed value to get a handle on the text field. From there it is
    // possible to add change the keyboard and focus behaviours.
    graphicProperty.addListener( ( c, o, n ) -> {
      if( o instanceof TextField ) {
        o.removeEventHandler( KEY_RELEASED, keyHandler );
        o.focusedProperty().removeListener( mFocusListener );
      }

      if( n instanceof final TextField input ) {
        n.addEventFilter( KEY_RELEASED, keyHandler );
        mInputText.bind( input.textProperty() );
        mFocusListener = new FocusListener( input );
        n.focusedProperty().addListener( mFocusListener );
      }
    } );
  }

  private void commitEdit() {
    mConsumer.accept( mInputText.getValue() );
  }
}
