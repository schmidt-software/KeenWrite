package com.keenwrite.ui.table;


import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import static javafx.scene.control.TableColumn.CellEditEvent;
import static javafx.scene.control.TableColumn.editCommitEvent;
import static javafx.scene.input.KeyCode.ESCAPE;

public class AltTableCell<S, T> extends TextFieldTableCell<S, T> {

  private TextField mTextField;

  public AltTableCell( final StringConverter<T> converter ) {
    super( converter );
  }

  @Override
  public void startEdit() {
    if( !isEmpty() ) {
      super.startEdit();
      createTextField();
      setText( null );
      setGraphic( mTextField );
      mTextField.selectAll();
    }
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    setText( getString() );
    setGraphic( null );
  }

  @Override
  public void updateItem( final T item, final boolean empty ) {
    super.updateItem( item, empty );

    if( empty ) {
      setText( null );
      setGraphic( null );
    }
    else {
      if( isEditing() ) {
        if( mTextField != null ) {
          mTextField.setText( getString() );
        }

        setText( null );
        setGraphic( mTextField );
      }
      else {
        setText( getString() );
        setGraphic( null );
      }
    }
  }

  private void createTextField() {
    final var converter = converterProperty().get();

    mTextField = new TextField( getString() );

    // Commit on pressing ENTER.
    mTextField.setOnAction(
      event -> commitEdit( converter.fromString( mTextField.getText() ) )
    );

    mTextField.setMinWidth( getWidth() - getGraphicTextGap() * 2 );

    final ChangeListener<? super Boolean> listener = ( c, o, n ) -> {
      if( !n ) {
        converter.fromString( mTextField.getText() );
      }
    };

    mTextField.focusedProperty().addListener( listener );

    mTextField.setOnKeyPressed( key -> {
      if( ESCAPE.equals( key.getCode() ) ) {
        mTextField.focusedProperty().removeListener( listener );
        cancelEdit();
      }
    } );
  }

  private String getString() {
    final var item = getItem();
    return item == null ? "" : item.toString();
  }

  @Override
  public void commitEdit( final T item ) {
    if( isEditing() ) {
      super.commitEdit( item );
    }
    else {
      final var table = getTableView();

      if( table != null ) {
        final var column = getTableColumn();

        final var position = new TablePosition<>(
          table, getTableRow().getIndex(), column );
        final var editEvent = new CellEditEvent<>(
          table, position, editCommitEvent(), item );
        Event.fireEvent( column, editEvent );
      }

      updateItem( item, false );

      if( table != null ) {
        table.edit( -1, null );
      }
    }
  }
}
