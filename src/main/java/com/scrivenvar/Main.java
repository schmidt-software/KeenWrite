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

import com.scrivenvar.preferences.FilePreferencesFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.util.StageState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.logging.LogManager;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;

/**
 * Application entry point. The application allows users to edit Markdown
 * files and see a real-time preview of the edits.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class Main extends Application {

  // Suppress logging errors to standard output.
  static {
    LogManager.getLogManager().reset();
  }

  private final Options mOptions = Services.load( Options.class );
  private final Snitch mSnitch = Services.load( Snitch.class );
  private final Thread mSnitchThread = new Thread( getSnitch() );
  private final MainWindow mMainWindow = new MainWindow();

  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private StageState mStageState;

  /**
   * Application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    initPreferences();
    launch( args );
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    initState( stage );
    initStage( stage );
    initSnitch();

    stage.show();
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

  private void initState( final Stage stage ) {
    mStageState = new StageState( stage, getOptions().getState() );
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

  /**
   * Watch for file system changes.
   */
  private void initSnitch() {
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
    thread.interrupt();
    thread.join();
  }

  private Snitch getSnitch() {
    return mSnitch;
  }

  private Thread getSnitchThread() {
    return mSnitchThread;
  }

  private Options getOptions() {
    return mOptions;
  }

  private MainWindow getMainWindow() {
    return mMainWindow;
  }

  private Scene getScene() {
    return getMainWindow().getScene();
  }

  private String getApplicationTitle() {
    return get( "Main.title" );
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
  }
}
