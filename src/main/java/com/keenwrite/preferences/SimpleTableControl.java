/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.keenwrite.ui.table.AltTableCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static java.util.Arrays.asList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class SimpleTableControl<K, V>
  extends SimpleControl<MapField<K, V>, VBox> {

  private static long sCounter;

  private final ObservableList<Entry<K, V>> mModel = observableArrayList();

  public SimpleTableControl() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public void initializeParts() {
    super.initializeParts();

    final var table = new TableView<>( mModel );

    table.setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY );
    table.setEditable( true );
    table.getColumns().addAll(
      asList(
        createEditableColumnKey( table ),
        createEditableColumnValue( table )
      )
    );
    table.getSelectionModel().setSelectionMode( MULTIPLE );

    final var inserted = workaroundBug( table );

    final var buttons = new ButtonBar();
    buttons.getButtons().addAll(
      createButton(
        "Add", "PLUS",
        event -> {
          sCounter++;

          inserted.set( true );
          mModel.add(
            new SimpleEntry<>(
              (K) ("key" + sCounter),
              (V) ("value" + sCounter)
            )
          );

//          map.clear();
//          model.forEach( item -> map.put( item.getKey(), item.getValue() ) );
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

  /**
   * TODO: Delete method when bug is fixed. See the
   * <a href="https://github.com/dlsc-software-consulting-gmbh/PreferencesFX/issues/413">issue
   * tracker</a> for details about the bug.
   *
   * @param table Add a width listener to correct a slight width change.
   * @return A Boolean lock so that the bug fix and "Add" button can
   * be used to ensure regular resizes don't interfere with programmatic ones.
   */
  private AtomicBoolean workaroundBug( final TableView<Entry<K, V>> table ) {
    final var inserted = new AtomicBoolean( true );

    table.widthProperty().addListener( ( c, o, n ) -> {
      if( (o != null && n != null)
        && o.intValue() == n.intValue() - 2
        && inserted.getAndSet( false ) ) {
        table.setPrefWidth( table.getPrefWidth() - 2 );
      }
    } );

    return inserted;
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
    column.setCellFactory(
      tableColumn -> new AltTableCell<>(
        new StringConverter<>() {
          @Override
          public String toString( final T object ) {
            return object.toString();
          }

          @Override
          @SuppressWarnings( "unchecked" )
          public T fromString( final String string ) {
            return (T) string;
          }
        }
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

        System.out.println( "LA MAP: " + field.mapProperty().get() );

//        mMap.remove( tableEntry.getKey() );
//        mMap.put( event.getNewValue(), tableEntry.getValue() );

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
