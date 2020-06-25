/*
 * Copyright 2017 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.scrivenvar.dialogs;

import static com.scrivenvar.Messages.get;
import com.scrivenvar.service.events.impl.ButtonOrderPane;
import static javafx.scene.control.ButtonType.CANCEL;
import static javafx.scene.control.ButtonType.OK;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

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
   * Attaches a setOnCloseRequest to the dialog's [X] button so that the user
   * can always close the window, even if there's an error.
   */
  protected final void initCloseAction() {
    final Window window = getDialogPane().getScene().getWindow();
    window.setOnCloseRequest( event -> window.hide() );
  }
}
