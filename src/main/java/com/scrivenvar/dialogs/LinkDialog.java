/*
 * Copyright 2016 Karl Tauber and White Magic Software, Ltd.
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

import com.scrivenvar.controls.EscapeTextField;
import com.scrivenvar.editors.markdown.HyperlinkModel;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Window;
import org.tbee.javafx.scene.layout.fxml.MigPane;

import static com.scrivenvar.Messages.get;
import static javafx.scene.control.ButtonType.OK;

/**
 * Dialog to enter a markdown link.
 */
public class LinkDialog extends AbstractDialog<String> {

  private final StringProperty link = new SimpleStringProperty();

  public LinkDialog(
    final Window owner, final HyperlinkModel hyperlink ) {
    super( owner, "Dialog.link.title" );

    final DialogPane dialogPane = getDialogPane();
    dialogPane.setContent( pane );

    dialogPane.lookupButton( OK ).disableProperty().bind(
      urlField.escapedTextProperty().isEmpty() );

    textField.setText( hyperlink.getText() );
    urlField.setText( hyperlink.getUrl() );
    titleField.setText( hyperlink.getTitle() );

    link.bind( Bindings.when( titleField.escapedTextProperty().isNotEmpty() )
      .then( Bindings.format( "[%s](%s \"%s\")", textField.escapedTextProperty(), urlField.escapedTextProperty(), titleField.escapedTextProperty() ) )
      .otherwise( Bindings.when( textField.escapedTextProperty().isNotEmpty() )
        .then( Bindings.format( "[%s](%s)", textField.escapedTextProperty(), urlField.escapedTextProperty() ) )
        .otherwise( urlField.escapedTextProperty() ) ) );

    setResultConverter( dialogButton -> {
      ButtonData data = (dialogButton != null) ? dialogButton.getButtonData() : null;
      return (data == ButtonData.OK_DONE) ? link.get() : null;
    } );

    Platform.runLater( () -> {
      urlField.requestFocus();
      urlField.selectRange( 0, urlField.getLength() );
    } );
  }

  @Override
  protected void initComponents() {
    // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
    pane = new MigPane();
    Label urlLabel = new Label();
    urlField = new EscapeTextField();
    Label textLabel = new Label();
    textField = new EscapeTextField();
    Label titleLabel = new Label();
    titleField = new EscapeTextField();

    //======== pane ========
    {
      pane.setCols( "[shrink 0,fill][300,grow,fill][fill][fill]" );
      pane.setRows( "[][][][]" );

      //---- urlLabel ----
      urlLabel.setText( get( "Dialog.link.urlLabel.text" ) );
      pane.add( urlLabel, "cell 0 0" );

      //---- urlField ----
      urlField.setEscapeCharacters( "()" );
      pane.add( urlField, "cell 1 0" );

      //---- textLabel ----
      textLabel.setText( get( "Dialog.link.textLabel.text" ) );
      pane.add( textLabel, "cell 0 1" );

      //---- textField ----
      textField.setEscapeCharacters( "[]" );
      pane.add( textField, "cell 1 1 3 1" );

      //---- titleLabel ----
      titleLabel.setText( get( "Dialog.link.titleLabel.text" ) );
      pane.add( titleLabel, "cell 0 2" );
      pane.add( titleField, "cell 1 2 3 1" );
    }
    // JFormDesigner - End of component initialization  //GEN-END:initComponents
  }

  // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
  private MigPane pane;
  private EscapeTextField urlField;
  private EscapeTextField textField;
  private EscapeTextField titleField;
  // JFormDesigner - End of variables declaration  //GEN-END:variables
}
