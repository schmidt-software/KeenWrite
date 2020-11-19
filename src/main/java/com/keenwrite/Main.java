/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite;

import com.keenwrite.service.Options;
import com.keenwrite.service.Snitch;
import com.keenwrite.ui.ApplicationActions;
import com.keenwrite.ui.ApplicationMenuBar;
import com.keenwrite.util.StageState;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;

import java.util.logging.LogManager;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.*;
import static com.keenwrite.preferences.UserPreferences.initPreferences;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
public final class Main extends Application {

  static {
    // Suppress logging to standard output.
    LogManager.getLogManager().reset();

    // Suppress logging to standard error.
    System.err.close();
  }

  private final Options mOptions = Services.load( Options.class );
  private final Snitch mSnitch = Services.load( Snitch.class );

  @SuppressWarnings({"FieldCanBeLocal", "unused", "RedundantSuppression"})
  private StageState mStageState;

  /**
   * Application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    initPreferences();
    initFonts();
    launch( args );
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    initSnitch();
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );

    stage.show();
  }

  /**
   * Stops the snitch service, if its running.
   */
  @Override
  public void stop() {
    getSnitch().stop();
  }

  private void initState( final Stage stage ) {
    mStageState = new StageState( stage, getOptions().getState() );
  }

  private void initStage( final Stage stage ) {
    stage.setTitle( APP_TITLE );

    stage.addEventHandler( KEY_PRESSED, event -> {
      if( F11.equals( event.getCode() ) ) {
        stage.setFullScreen( !stage.isFullScreen() );
      }
    } );
  }

  private void initIcons( final Stage stage ) {
    stage.getIcons().addAll(
        createImage( FILE_LOGO_16 ),
        createImage( FILE_LOGO_32 ),
        createImage( FILE_LOGO_128 ),
        createImage( FILE_LOGO_256 ),
        createImage( FILE_LOGO_512 )
    );
  }

  private void initScene( final Stage stage ) {
    final var appPane = new BorderPane();
    final var mainView = createMainView();
    final var actions = new ApplicationActions( mainView );
    final var menuBar = createMenuBar( actions );
    final var statusBar = getStatusBar();

    appPane.setTop( menuBar );
    appPane.setCenter( mainView );
    appPane.setBottom( statusBar );

    final var scene = new Scene( appPane );
    stage.setScene( scene );
  }

  private Node createMenuBar( final ApplicationActions actions ) {
    final var menuBar = new ApplicationMenuBar( actions );
    return menuBar.createMenuBar();
  }

  private MainView createMainView() {
    return new MainView();
  }

  private StatusBar getStatusBar() {
    return StatusBarNotifier.getStatusBar();
  }

  /**
   * Watch for file system changes.
   */
  private void initSnitch() {
    getSnitch().start();
  }

  private Snitch getSnitch() {
    return mSnitch;
  }

  private Options getOptions() {
    return mOptions;
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
  }
}
