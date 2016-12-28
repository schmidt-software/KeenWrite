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

import static com.scrivenvar.Constants.*;
import com.scrivenvar.preferences.FilePreferencesFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.util.StageState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.scrivenvar.service.events.Notifier;

/**
 * Main application entry point. The application allows users to edit Markdown
 * files and see a real-time preview of the edits.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class Main extends Application {

  private Options options;
  private Snitch snitch;
  private Thread snitchThread;

  private static Application app;
  private final MainWindow mainWindow = new MainWindow();

  public static void main( final String[] args ) {
    initPreferences();
    launch( args );
  }

  /**
   * Sets the factory used for reading user preferences.
   */
  private static void initPreferences() {
    System.setProperty(
      "java.util.prefs.PreferencesFactory",
      FilePreferencesFactory.class.getName()
    );
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
    initNotifyService();
    initState( stage );
    initStage( stage );
    initSnitch();

    stage.show();
  }

  public static void showDocument( final String uri ) {
    getApplication().getHostServices().showDocument( uri );
  }

  private void initApplication() {
    app = this;
  }

  /**
   * Constructs the notify service and appends the main window to the list of
   * notification observers.
   */
  private void initNotifyService() {
    final Notifier service = Services.load(Notifier.class );
    service.addObserver( getMainWindow() );
  }

  private StageState initState( final Stage stage ) {
    return new StageState( stage, getOptions().getState() );
  }

  private void initStage( final Stage stage ) {
    stage.getIcons().addAll(
      createImage( FILE_LOGO_16 ),
      createImage( FILE_LOGO_32 ),
      createImage( FILE_LOGO_128 ),
      createImage( FILE_LOGO_256 ),
      createImage( FILE_LOGO_512 ) );
    stage.setTitle( getApplicationTitle() );
    stage.setScene( getScene() );
  }

  private void initSnitch() {
    setSnitchThread( new Thread( getSnitch() ) );
    getSnitchThread().start();
  }

  /**
   * Stops the snitch service, if its running.
   *
   * @throws InterruptedException Couldn't stop the snitch thread.
   */
  @Override
  public void stop() throws InterruptedException {
    getSnitch().stop();

    final Thread thread = getSnitchThread();

    if( thread != null ) {
      thread.interrupt();
      thread.join();
    }
  }

  private synchronized Snitch getSnitch() {
    if( this.snitch == null ) {
      this.snitch = Services.load( Snitch.class );
    }

    return this.snitch;
  }

  private Thread getSnitchThread() {
    return this.snitchThread;
  }

  private void setSnitchThread( final Thread thread ) {
    this.snitchThread = thread;
  }

  private synchronized Options getOptions() {
    if( this.options == null ) {
      this.options = Services.load( Options.class );
    }

    return this.options;
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  private MainWindow getMainWindow() {
    return this.mainWindow;
  }

  private String getApplicationTitle() {
    return Messages.get( "Main.title" );
  }

  private static Application getApplication() {
    return app;
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
  }
}
