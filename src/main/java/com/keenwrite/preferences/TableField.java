/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.util.BindingMode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.TableView;

import java.util.ArrayList;

import static com.dlsc.formsfx.model.util.BindingMode.CONTINUOUS;
import static javafx.collections.FXCollections.observableList;

/**
 * Responsible for binding a form field to a map of values that, ultimately,
 * users may edit.
 *
 * @param <P> The type of {@link Property} to store in the list.
 */
public class TableField<P> extends Field<TableField<P>> {

  /**
   * Create a writeable list as the data model.
   */
  private final ListProperty<P> mViewProperty = new SimpleListProperty<>(
    observableList( new ArrayList<>() )
  );

  /**
   * Contains the data model entries to persist.
   */
  private final ListProperty<P> mSaveProperty;

  /**
   * Creates a new {@link TableField} with a reference to the list that is to
   * be persisted.
   *
   * @param persist A list of items that will be persisted.
   * @param <P>     The type of elements in the list to persist.
   * @return A new {@link TableField} used to help render a UI widget.
   */
  public static <P> TableField<P> ofListType( final ListProperty<P> persist ) {
    return new TableField<>( persist );
  }

  private TableField( final ListProperty<P> property ) {
    mSaveProperty = property;
  }

  public TableView<P> createTableView() {
    return new TableView<>( mViewProperty );
  }

  public void add( final P entry ) {
    mViewProperty.add( entry );
  }

  /**
   * Called when a new UI instance is opened.
   *
   * @param bindingMode Indicates how the view data model is bound to the
   *                    persistence data model.
   */
  @Override
  public void setBindingMode( final BindingMode bindingMode ) {
    if( CONTINUOUS.equals( bindingMode ) ) {
      mViewProperty.addAll( mSaveProperty );
    }
  }

  /**
   * Answers whether the user input is valid.
   *
   * @return {@code true} Users may provide any key or value strings.
   */
  @Override
  protected boolean validate() {
    return true;
  }

  /**
   * Update the properties to save by copying the properties updated in the
   * user interface (i.e., the view). To be clear, the properties are not
   * persisted after calling this method, merely moved out of the UI data
   * model and into the to-be-saved data model.
   */
  @Override
  public void persist() {
    mSaveProperty.clear();
    mSaveProperty.addAll( mViewProperty );
  }

  /**
   * The {@link TableField} doesn't bind values, as such the reset can be
   * a no-op because only {@link #persist()} will update the properties to
   * save.
   */
  @Override
  public void reset() {}
}
