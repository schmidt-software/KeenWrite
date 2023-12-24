/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.dialogs;

import com.keenwrite.Messages;
import com.keenwrite.service.events.impl.ButtonOrderPane;
import com.keenwrite.util.Strings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.util.LinkedList;
import java.util.List;

import static com.keenwrite.Messages.get;
import static com.keenwrite.util.Strings.*;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.layout.Priority.ALWAYS;
import static javafx.scene.layout.Priority.NEVER;

/**
 * TODO: Replace {@link AbstractDialog} with this class, then remove
 * {@link AbstractDialog}.
 *
 * @param <T> The type of data returned from the dialog upon acceptance.
 */
public abstract class CustomDialog<T> extends Dialog<T> {
  private final GridPane mContentPane = new GridPane( 10, 10 );
  private final List<TextField> mInputFields = new LinkedList<>();

  public CustomDialog( final Window owner, final String title ) {
    assert owner != null;
    assert validate( title );

    initOwner( owner );
    setTitle( get( title ) );
    setResizable( true );
  }

  protected void initialize() {
    initDialogPane();
    initDialogButtons();
    initInputFields();
    initContentPane();

    assert !mInputFields.isEmpty();

    final var first = mInputFields.getFirst();
    assert first != null;

    Platform.runLater( first::requestFocus );

    setResultConverter( button -> {
      final ButtonData data = button == null ? null : button.getButtonData();
      return data == ButtonData.OK_DONE ? handleAccept() : null;
    } );
  }

  /**
   * Invoked when the user selects the OK button to confirm the input values.
   *
   * @return The type of data provided by using the dialog.
   */
  protected abstract T handleAccept();

  /**
   * Subclasses must call this method at least once.
   *
   * @param id     The unique identifier for the input field.
   * @param label  The input field's label property key.
   * @param prompt The prompt property key, which provides context.
   * @param value  The initial value to provide for the field.
   * @see Messages#get(String)
   */
  protected void addInputField(
    final String id,
    final String label,
    final String prompt,
    final String value,
    final ChangeListener<String> listener ) {
    assert validate( id );
    assert validate( label );
    assert validate( prompt );
    assert validate( value );

    final int row = mInputFields.size();
    final Label fieldLabel = new Label( get( label ) );
    final TextField fieldInput = new TextField();

    fieldInput.setPromptText( get( prompt ) );
    fieldInput.setId( id );
    fieldInput.textProperty().addListener( listener );
    fieldInput.setText( value );

    mContentPane.add( fieldLabel, 0, row );
    mContentPane.add( fieldInput, 1, row );
    mInputFields.add( fieldInput );
  }

  /**
   * Subclasses must add at least one input field.
   */
  protected abstract void initInputFields();

  /**
   * Set the dialog to use a button order pane with an OK and a CANCEL button.
   */
  protected void initDialogPane() {
    setDialogPane( new ButtonOrderPane() );
  }

  /**
   * Set an OK and CANCEL button on the dialog.
   */
  protected void initDialogButtons() {
    getDialogPane().getButtonTypes().addAll( OK, CANCEL );
  }

  /**
   * Called after the input fields have been added. This adds the input
   * fields to the main dialog pane.
   */
  protected void initContentPane() {
    mContentPane.setPadding( new Insets( 20, 10, 10, 10 ) );

    final var cc1 = new ColumnConstraints();
    final var cc2 = new ColumnConstraints();

    cc1.setHgrow( NEVER );
    cc2.setHgrow( ALWAYS );
    cc2.setMinWidth( 250 );
    mContentPane.getColumnConstraints().addAll( cc1, cc2 );

    getDialogPane().setContent( mContentPane );
  }
}
