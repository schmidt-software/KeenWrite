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
package com.scrivenvar;

import static com.scrivenvar.Constants.LOGO_128;
import static com.scrivenvar.Constants.LOGO_16;
import static com.scrivenvar.Constants.LOGO_256;
import static com.scrivenvar.Constants.LOGO_32;
import static com.scrivenvar.Constants.LOGO_512;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.events.AlertService;
import com.scrivenvar.util.StageState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main application entry point. The application allows users to edit Markdown
 * files and see a real-time preview of the edits.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class Main extends Application {

  private static Application app;

  private final MainWindow mainWindow = new MainWindow();
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
  public void start( final Stage stage ) throws Exception {
    initApplication();
    initState( stage );
    initStage( stage );
    initAlertService();
    
    stage.show();
  }

  private void initApplication() {
    app = this;
  }

  private Options getOptions() {
    return this.options;
  }

  private String getApplicationTitle() {
    return Messages.get( "Main.title" );
  }

  private StageState initState( Stage stage ) {
    return new StageState( stage, getOptions().getState() );
  }

  private void initStage( Stage stage ) {
    stage.getIcons().addAll(
      new Image( LOGO_16 ),
      new Image( LOGO_32 ),
      new Image( LOGO_128 ),
      new Image( LOGO_256 ),
      new Image( LOGO_512 ) );
    stage.setTitle( getApplicationTitle() );
    stage.setScene( getScene() );
  }

  private void initAlertService() {
    final AlertService service = Services.load( AlertService.class );
    service.setWindow( getScene().getWindow() );
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  private MainWindow getMainWindow() {
    return this.mainWindow;
  }

  private static Application getApplication() {
    return app;
  }

  public static void showDocument( String uri ) {
    getApplication().getHostServices().showDocument( uri );
  }
}
