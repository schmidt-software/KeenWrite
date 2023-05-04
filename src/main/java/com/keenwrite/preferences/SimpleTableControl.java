/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl;
import com.keenwrite.ui.cells.AltTableCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static java.util.Arrays.asList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN;

public class SimpleTableControl<K, V, F extends TableField<Entry<K, V>>>
  extends SimpleControl<F, VBox> {

  private static long sCounter;

  public SimpleTableControl() {}

  @Override
  public void initializeParts() {
    super.initializeParts();

    final var model = field.viewProperty();
    final var table = new TableView<>( model );

    table.setColumnResizePolicy( CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN );
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

          model.add( createEntry( "key" + sCounter, "value" + sCounter ) );
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

  @SuppressWarnings( "unchecked" )
  private Entry<K, V> createEntry( final String k, final String v ) {
    return new SimpleEntry<>( (K) k, (V) v );
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

  private TableColumn<Entry<K, V>, K> createEditableColumnKey(
    final TableView<Entry<K, V>> table ) {
    return createColumn(
      table,
      Entry::getKey,
      ( e, o ) -> new SimpleEntry<>( e.getNewValue(), o.getValue() ),
      "Key",
      .2
    );
  }

  private TableColumn<Entry<K, V>, V> createEditableColumnValue(
    final TableView<Entry<K, V>> table ) {
    return createColumn(
      table,
      Entry::getValue,
      ( e, o ) -> new SimpleEntry<>( o.getKey(), e.getNewValue() ),
      "Value",
      .8
    );
  }

  /**
   * Creates a table column having cells that be edited.
   *
   * @param table    The table to which the column belongs.
   * @param mapEntry Data model backing the edited text.
   * @param label    Column name.
   * @param width    Fraction of table width (1 = 100%).
   * @param <T>      The return type for the column (i.e., key or value).
   * @return The newly configured column.
   */
  private <T, E extends Entry<K, V>> TableColumn<E, T> createColumn(
    final TableView<E> table,
    final Function<E, T> mapEntry,
    final BiFunction<CellEditEvent<E, T>, E, E> creator,
    final String label,
    final double width
  ) {
    final var column = new TableColumn<E, T>( label );

    column.setEditable( true );
    column.setResizable( true );
    column.prefWidthProperty().bind( table.widthProperty().multiply( width ) );

    column.setOnEditCommit( event -> {
      final var index = event.getTablePosition().getRow();
      final var view = event.getTableView();
      final var old = view.getItems().get( index );

      // Update the data model with the new column value.
      view.getItems().set( index, creator.apply( event, old ) );
    } );

    column.setCellValueFactory(
      cellData ->
        new SimpleObjectProperty<>( mapEntry.apply( cellData.getValue() ) )
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

  /**
   * Calling {@link #initializeParts()} also performs layout because no handles
   * are kept to the widgets after initialization.
   */
  @Override
  public void layoutParts() {}
}
