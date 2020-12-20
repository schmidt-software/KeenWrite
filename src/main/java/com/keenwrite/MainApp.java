/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.preferences.WorkspacePreferences;
import com.keenwrite.service.Snitch;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.LOGOS;
import static com.keenwrite.preferences.WorkspacePreferences.*;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import static javafx.stage.WindowEvent.WINDOW_HIDING;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused", "RedundantSuppression"})
public final class MainApp extends Application {

  private final Snitch mSnitch = Services.load( Snitch.class );

  private Workspace mWorkspace;
  private WorkspacePreferences mPreferences;

  /**
   * Application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    initLogging();
    launch( args );
  }

  /**
   * Suppress logging to standard output and standard error.
   */
  private static void initLogging() {
    //LogManager.getLogManager().reset();
    //System.err.close();
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    // These must be instantiated after the UI is initialized.
    mPreferences = new WorkspacePreferences();
    mWorkspace = new Workspace( mPreferences );

    initFonts();
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
    save();
    getSnitch().stop();
    Platform.exit();
    System.exit( 0 );
  }

  /**
   * Saves the current application state configuration and user preferences.
   */
  private void save() {
    mWorkspace.save();
  }

  private void initState( final Stage stage ) {
    stage.setX( mPreferences.getDouble( KEY_UI_WINDOW_X ) );
    stage.setY( mPreferences.getDouble( KEY_UI_WINDOW_Y ) );
    stage.setWidth( mPreferences.getDouble( KEY_UI_WINDOW_W ) );
    stage.setHeight( mPreferences.getDouble( KEY_UI_WINDOW_H ) );
    stage.setMaximized( mPreferences.getBoolean( KEY_UI_WINDOW_MAX ) );
    stage.setFullScreen( mPreferences.getBoolean( KEY_UI_WINDOW_FULL ) );

    mPreferences.bind( KEY_UI_WINDOW_X, stage.xProperty() );
    mPreferences.bind( KEY_UI_WINDOW_Y, stage.yProperty() );
    mPreferences.bind( KEY_UI_WINDOW_W, stage.widthProperty() );
    mPreferences.bind( KEY_UI_WINDOW_H, stage.heightProperty() );
    mPreferences.bind( KEY_UI_WINDOW_MAX, stage.maximizedProperty() );
    mPreferences.bind( KEY_UI_WINDOW_FULL, stage.fullScreenProperty() );
  }

  private void initStage( final Stage stage ) {
    stage.setTitle( APP_TITLE );
    stage.addEventHandler( WINDOW_HIDING, event -> save() );
    stage.addEventHandler( WINDOW_CLOSE_REQUEST, event -> stop() );
    stage.addEventHandler( KEY_PRESSED, event -> {
      if( F11.equals( event.getCode() ) ) {
        stage.setFullScreen( !stage.isFullScreen() );
      }
    } );
  }

  private void initIcons( final Stage stage ) {
    stage.getIcons().addAll( LOGOS );
  }

  private void initScene( final Stage stage ) {
    stage.setScene( (new MainScene( mWorkspace )).getScene() );
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

  private Workspace getWorkspace() {
    return mWorkspace;
  }
}