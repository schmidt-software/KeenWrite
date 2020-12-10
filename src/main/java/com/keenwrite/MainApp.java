/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.service.Options;
import com.keenwrite.service.Snitch;
import com.keenwrite.util.StageState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.*;
import static com.keenwrite.preferences.UserPreferences.initPreferences;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
public final class MainApp extends Application {

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
    initLogging();
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
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );
    initSnitch();

    stage.show();
  }

  /**
   * Saves the workspace then terminates the application.
   */
  @Override
  public void stop() {
    Workspace.getInstance().save();
    getSnitch().stop();
    Platform.exit();
    System.exit( 0 );
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

    stage.addEventHandler( WINDOW_CLOSE_REQUEST, event -> stop() );
  }

  private static void initLogging() {
    // Suppress logging to standard output.
    //LogManager.getLogManager().reset();

    // Suppress logging to standard error.
    //System.err.close();
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
    stage.setScene( (new MainScene()).getScene() );
  }

  private Image createImage( final String filename ) {
    return new Image( filename );
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
}
