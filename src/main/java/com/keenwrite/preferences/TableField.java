/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preferences;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.util.BindingMode;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.ArrayList;

import static javafx.collections.FXCollections.observableList;

/**
 * Responsible for binding a form field to a map of values that, ultimately,
 * users may edit.
 *
 * @param <E> The type of elements to store in the list.
 */
public class TableField<E> extends Field<TableField<E>> {

  /**
   * Create a writeable list as the data model.
   */
  private final ListProperty<E> mViewProperty = new SimpleListProperty<>(
    observableList( new ArrayList<>() )
  );

  public static <E> TableField<E> ofListType() {
    return new TableField<>();
  }

  private TableField() {
  }

  public ListProperty<E> viewProperty() {
    return mViewProperty;
  }

  @Override
  public void setBindingMode( final BindingMode bindingMode ) {
    System.out.println( "BIND TO: " + bindingMode );
  }

  /**
   * Answers whether the user input is valid.
   *
   * @return {@code true} Users may provide any strings.
   */
  @Override
  protected boolean validate() {
    return true;
  }

  @Override
  public void persist() {
    System.out.println( "PURSIST: " + mViewProperty );
  }

  @Override
  public void reset() {
    System.out.println( "RESET" );
  }
}
