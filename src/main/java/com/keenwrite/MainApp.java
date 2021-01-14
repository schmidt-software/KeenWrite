/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.service.Snitch;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.function.BooleanSupplier;
import java.util.logging.LogManager;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.LOGOS;
import static com.keenwrite.preferences.Workspace.*;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
@SuppressWarnings( {"FieldCanBeLocal", "unused", "RedundantSuppression"} )
public final class MainApp extends Application {

  private final Snitch mSnitch = Services.load( Snitch.class );

  private Workspace mWorkspace;

  /**
   * Application entry point.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    disableLogging();
    launch( args );
  }

  /**
   * Suppress logging to standard output and standard error.
   */
  private static void disableLogging() {
    LogManager.getLogManager().reset();
    System.err.close();
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    // Must be instantiated after the UI is initialized (i.e., not in main).
    mWorkspace = new Workspace();

    initFonts();
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );
    initSnitch();

    stage.show();
  }

  private void initState( final Stage stage ) {
    final var enable = createBoundsEnabledSupplier( stage );

    stage.setX( mWorkspace.toDouble( KEY_UI_WINDOW_X ) );
    stage.setY( mWorkspace.toDouble( KEY_UI_WINDOW_Y ) );
    stage.setWidth( mWorkspace.toDouble( KEY_UI_WINDOW_W ) );
    stage.setHeight( mWorkspace.toDouble( KEY_UI_WINDOW_H ) );
    stage.setMaximized( mWorkspace.toBoolean( KEY_UI_WINDOW_MAX ) );
    stage.setFullScreen( mWorkspace.toBoolean( KEY_UI_WINDOW_FULL ) );

    mWorkspace.listen( KEY_UI_WINDOW_X, stage.xProperty(), enable );
    mWorkspace.listen( KEY_UI_WINDOW_Y, stage.yProperty(), enable );
    mWorkspace.listen( KEY_UI_WINDOW_W, stage.widthProperty(), enable );
    mWorkspace.listen( KEY_UI_WINDOW_H, stage.heightProperty(), enable );
    mWorkspace.listen( KEY_UI_WINDOW_MAX, stage.maximizedProperty() );
    mWorkspace.listen( KEY_UI_WINDOW_FULL, stage.fullScreenProperty() );
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

  /**
   * When the window is maximized, full screen, or iconified, prevent updating
   * the window bounds. This is used so that if the user exits the application
   * when full screen (or maximized), restarting the application will recall
   * the previous bounds, allowing for continuity of expected behaviour.
   *
   * @param stage The window to check for "normal" status.
   * @return {@code false} when the bounds must not be changed, ergo persisted.
   */
  private BooleanSupplier createBoundsEnabledSupplier( final Stage stage ) {
    return () ->
      !(stage.isMaximized() || stage.isFullScreen() || stage.isIconified());
  }

  private Snitch getSnitch() {
    return mSnitch;
  }
}
