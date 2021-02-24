/*
 * Copyright 2015 Karl Tauber <karl at jformdesigner dot com>
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

package com.keenwrite.ui.controls;

import com.keenwrite.Messages;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.keenwrite.ui.fonts.IconFactory.createGraphic;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.FILE_ALT;

/**
 * Button that opens a file chooser to select a local file for a URL.
 */
public class BrowseFileButton extends Button {

  private final List<ExtensionFilter> mExtensionFilters = new ArrayList<>();
  private final ObjectProperty<Path> mBasePath = new SimpleObjectProperty<>();
  private final ObjectProperty<String> mUrl = new SimpleObjectProperty<>();

  public BrowseFileButton() {
    setGraphic( createGraphic( FILE_ALT ) );
    setTooltip( new Tooltip( Messages.get( "BrowseFileButton.tooltip" ) ) );
    setOnAction( this::browse );

    disableProperty().bind( mBasePath.isNull() );

    // workaround for a JavaFX bug:
    //   avoid closing the dialog that contains this control when the user
    //   closes the FileChooser or DirectoryChooser using the ESC key
    addEventHandler( KeyEvent.KEY_RELEASED, e -> {
      if( e.getCode() == KeyCode.ESCAPE ) {
        e.consume();
      }
    } );
  }

  public void addExtensionFilter( ExtensionFilter extensionFilter ) {
    mExtensionFilters.add( extensionFilter );
  }

  public ObjectProperty<String> urlProperty() {
    return mUrl;
  }

  private void browse( ActionEvent e ) {
    var fileChooser = new FileChooser();
    fileChooser.setTitle( Messages.get( "BrowseFileButton.chooser.title" ) );
    fileChooser.getExtensionFilters().addAll( mExtensionFilters );
    fileChooser.getExtensionFilters()
               .add( new ExtensionFilter( Messages.get(
                   "BrowseFileButton.chooser.allFilesFilter" ), "*.*" ) );
    fileChooser.setInitialDirectory( getInitialDirectory() );
    var result = fileChooser.showOpenDialog( getScene().getWindow() );
    if( result != null ) {
      updateUrl( result );
    }
  }

  private File getInitialDirectory() {
    //TODO build initial directory based on current value of 'url' property
    return getBasePath().toFile();
  }

  private void updateUrl( File file ) {
    String newUrl;
    try {
      newUrl = getBasePath().relativize( file.toPath() ).toString();
    } catch( final Exception ex ) {
      newUrl = file.toString();
    }
    mUrl.set( newUrl.replace( '\\', '/' ) );
  }

  public void setBasePath( Path basePath ) {
    this.mBasePath.set( basePath );
  }

  private Path getBasePath() {
    return mBasePath.get();
  }
}
