/* Copyright 2017-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.dialogs;

import com.keenwrite.service.events.impl.ButtonOrderPane;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.stage.Window;

import static com.keenwrite.GraphicsConstants.ICON_DIALOG;
import static com.keenwrite.Messages.get;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;

/**
 * Superclass that abstracts common behaviours for all dialogs.
 *
 * @param <T> The type of dialog to create (usually String).
 */
public abstract class AbstractDialog<T> extends Dialog<T> {

  /**
   * Ensures that all dialogs can be closed.
   *
   * @param owner The parent window of this dialog.
   * @param title The messages title to display in the title bar.
   */
  @SuppressWarnings( "OverridableMethodCallInConstructor" )
  public AbstractDialog( final Window owner, final String title ) {
    setTitle( get( title ) );
    setResizable( true );

    initOwner( owner );
    initCloseAction();
    initDialogPane();
    initDialogButtons();
    initComponents();
    initIcon( (Stage) owner );
  }

  /**
   * Initialize the component layout.
   */
  protected abstract void initComponents();

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
   * Attaches a close request to the dialog's [X] button so that the user
   * can always close the window, even if there's an error.
   */
  protected final void initCloseAction() {
    final var window = getDialogPane().getScene().getWindow();
    window.setOnCloseRequest( event -> window.hide() );
  }

  private void initIcon( final Stage owner ) {
    owner.getIcons().add( ICON_DIALOG );
  }
}
