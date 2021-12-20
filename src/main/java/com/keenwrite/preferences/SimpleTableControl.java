package com.keenwrite.preferences;

import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;

public class SimpleTableControl<K, V>
  extends SimpleControl<MapField<K, V>, TableView<Entry<K, V>>> {

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

    final var table = new TableView<>( observableArrayList( mMap.entrySet() ) );

    table.setEditable( true );
    table.getColumns().addAll(
      asList(
        createEditableColumnKey( table ),
        createEditableColumnValue( table )
      )
    );

    super.node = table;
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
