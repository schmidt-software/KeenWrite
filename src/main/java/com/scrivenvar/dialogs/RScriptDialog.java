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
import javafx.application.Platform;
import javafx.geometry.Insets;
import static javafx.scene.control.ButtonType.OK;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * Responsible for managing the R startup script that is run when an R source
 * file is loaded.
 *
 * @author White Magic Software, Ltd.
 */
public class RScriptDialog extends AbstractDialog<String> {

  private TextArea scriptArea;

  public RScriptDialog(
    final Window parent, final String title, final String script ) {
    super( parent, title );
    getScriptArea().setText( script );
  }

  @Override
  protected void initComponents() {
    final DialogPane pane = getDialogPane();

    final GridPane grid = new GridPane();
    grid.setHgap( 10 );
    grid.setVgap( 10 );
    grid.setPadding( new Insets( 10, 10, 10, 10 ) );

    final Label label = new Label( get( "Dialog.rScript.content" ) );

    final TextArea textArea = getScriptArea();
    textArea.setEditable( true );
    textArea.setWrapText( true );

    grid.add( label, 0, 0 );
    grid.add( textArea, 0, 1 );
    pane.setContent( grid );

    Platform.runLater( () -> textArea.requestFocus() );

    setResultConverter( dialogButton -> {
      return dialogButton == OK ? textArea.getText() : "";
    } );
  }

  private TextArea getScriptArea() {
    if( this.scriptArea == null ) {
      this.scriptArea = new TextArea();
    }

    return this.scriptArea;
  }
}
