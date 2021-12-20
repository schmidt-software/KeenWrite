package com.keenwrite.preferences;

import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.SelectionMode.MULTIPLE;

public class SimpleTableControl<K, V>
  extends SimpleControl<MapField<K, V>, VBox> {

  private static long sCounter;

  /**
   * Data model for the table view, which must not be immutable.
   */
  private final Map<K, V> mMap;

  public SimpleTableControl( final Map<K, V> map ) {
    assert map != null;

    mMap = map;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeParts() {
    super.initializeParts();

    final var model = observableArrayList( mMap.entrySet() );
    final var table = new TableView<>( model );

    table.setEditable( true );
    table.getColumns().addAll(
      asList(
        createEditableColumnKey( table ),
        createEditableColumnValue( table )
      )
    );
    table.getSelectionModel().setSelectionMode( MULTIPLE );

    final var buttons = new ButtonBar();
    buttons.getButtons().addAll(
      createButton(
        "Add", "PLUS",
        event -> {
          sCounter++;

          final var k = (K) ("key" + sCounter);
          final var v = (V) ("value" + sCounter);

          model.add( new SimpleEntry<>( k, v ) );
        }
      ),

      createButton(
        "Delete", "TRASH",
        event -> {
          final var selectionModel = table.getSelectionModel();
          final var selection = selectionModel.getSelectedItems();

          if( selection != null && !selection.isEmpty() ) {
            final var items = table.getItems();
            final var rows = new ArrayList<>( selection );
            rows.forEach( items::remove );

            selectionModel.clearSelection();
          }
        }
      )
    );

    final var vbox = new VBox();
    vbox.setSpacing( 5 );
    vbox.setPadding( new Insets( 10, 0, 0, 10 ) );
    vbox.getChildren().addAll( table, buttons );

    super.node = vbox;
  }

  private Button createButton(
    final String label,
    final String graphic,
    final EventHandler<ActionEvent> handler ) {
    assert label != null;
    assert !label.isBlank();
    assert graphic != null;
    assert !graphic.isBlank();
    assert handler != null;

    final var button = new Button( label, createGraphic( graphic ) );
    button.setOnAction( handler );
    return button;
  }

  private <T> TableColumn<Entry<K, V>, T> createColumn(
    final TableView<Entry<K, V>> table,
    final Function<Entry<K, V>, T> mapEntry,
    final String label,
    final double width
  ) {
    final var column = new TableColumn<Entry<K, V>, T>( label );

    column.setEditable( true );
    column.setResizable( true );
    column.prefWidthProperty().bind( table.widthProperty().multiply( width ) );
    column.setCellValueFactory(
      cellData -> new SimpleObjectProperty<>(
        mapEntry.apply( cellData.getValue() )
      )
    );
    column.setCellFactory( callback -> new TextFieldTableCell<>() );

    return column;
  }

  private TableColumn<Entry<K, V>, K> createEditableColumnKey(
    final TableView<Entry<K, V>> table ) {
    final var column = createColumn( table, Entry::getKey, "Key", .3 );

    column.setOnEditCommit(
      event -> {
        final var tableEntry = event.getRowValue();
        final var key = event.getNewValue();
        final var value = tableEntry.getValue();
        final var entry = new SimpleEntry<>( key, value );

        mMap.remove( tableEntry.getKey() );
        mMap.put( event.getNewValue(), tableEntry.getValue() );

        final var items = event.getTableView().getItems();
        final var rowIndex = event.getTablePosition().getRow();

        items.set( rowIndex, entry );
      }
    );

    return column;
  }

  private TableColumn<Entry<K, V>, V> createEditableColumnValue(
    final TableView<Entry<K, V>> table ) {
    final var column = createColumn( table, Entry::getValue, "Value", .7 );

    column.setOnEditCommit(
      event -> {
        final var tableEntry = event.getRowValue();
        tableEntry.setValue( event.getNewValue() );
      }
    );

    return column;
  }

  @Override
  public void layoutParts() {
  }
}
