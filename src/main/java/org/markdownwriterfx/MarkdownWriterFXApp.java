/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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
package org.markdownwriterfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.markdownwriterfx.service.Options;
import org.markdownwriterfx.service.Settings;
import org.markdownwriterfx.util.StageState;

/**
 * Markdown Writer FX application.
 *
 * @author Karl Tauber
 */
public final class MarkdownWriterFXApp extends Application {

  private static Application app;

  private MainWindow mainWindow;
  private StageState stageState;
  private final Settings settings = Services.load( Settings.class );
  private final Options options = Services.load( Options.class );

  public static void main( String[] args ) {
    launch( args );
  }

  /**
   * Application entry point.
   *
   * @param stage The primary application stage.
   *
   * @throws Exception Could not read configuration file.
   */
  @Override
  public void start( Stage stage ) throws Exception {
    initApplication();
    initWindow();
    initState( stage );
    initStage( stage );

    stage.show();
  }

  private void initApplication() {
    app = this;
  }

  private Settings getSettings() {
    return this.settings;
  }
  
  private Options getOptions() {
    return this.options;
  }

  private String getApplicationTitle() {
    return getSettings().getSetting( "application.title", "Markdown Writer FX" );
  }

  private void initWindow() {
    setWindow( new MainWindow() );
  }

  private void initState( Stage stage ) {
    stageState = new StageState( stage, getOptions().getState() );
  }

  private void initStage( Stage stage ) {
    stage.getIcons().addAll(
      new Image( "org/markdownwriterfx/markdownwriterfx16.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx32.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx128.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx256.png" ),
      new Image( "org/markdownwriterfx/markdownwriterfx512.png" ) );
    stage.setTitle( getApplicationTitle() );
    stage.setScene( getScene() );
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  protected MainWindow getMainWindow() {
    return this.mainWindow;
  }

  private void setWindow( MainWindow mainWindow ) {
    this.mainWindow = mainWindow;
  }

  private StageState getStageState() {
    return this.stageState;
  }

  private void setStageState( StageState stageState ) {
    this.stageState = stageState;
  }

  private static Application getApplication() {
    return app;
  }

  public static void showDocument( String uri ) {
    getApplication().getHostServices().showDocument( uri );
  }
}
