/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.cmdline.HeadlessApp;
import com.keenwrite.events.HyperlinkOpenEvent;
import com.keenwrite.preferences.Workspace;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;

import java.util.function.BooleanSupplier;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.constants.GraphicsConstants.LOGOS;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.preferences.WorkspaceKeys.*;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
public final class MainApp extends Application {

  private Workspace mWorkspace;
  private MainScene mMainScene;

  /**
   * GUI application entry point. See {@link HeadlessApp} for the entry
   * point to the command-line application.
   *
   * @param args Command-line arguments.
   */
  public static void main( final String[] args ) {
    launch( args );
  }

  /**
   * Creates an instance of {@link KeyEvent} that represents pressing a key.
   *
   * @param code  The key to simulate being pressed down.
   * @param shift Whether shift key modifier shall modify the key code.
   * @return An instance of {@link KeyEvent} that may be used to simulate
   * a key being pressed.
   */
  public static Event keyDown( final KeyCode code, final boolean shift ) {
    return keyEvent( KEY_PRESSED, code, shift );
  }

  /**
   * Creates an instance of {@link KeyEvent} that represents releasing a key.
   *
   * @param code  The key to simulate being released up.
   * @param shift Whether shift key modifier shall modify the key code.
   * @return An instance of {@link KeyEvent} that may be used to simulate
   * a key being released.
   */
  public static Event keyUp( final KeyCode code, final boolean shift ) {
    return keyEvent( KEY_RELEASED, code, shift );
  }

  /**
   * Creates an instance of {@link KeyEvent} that represents a key released
   * event without any modifier keys held.
   *
   * @param code The key code representing a key to simulate releasing.
   * @return An instance of {@link KeyEvent}.
   */
  public static Event keyUp( final KeyCode code ) {
    return keyUp( code, false );
  }

  private static Event keyEvent(
    final EventType<KeyEvent> type, final KeyCode code, final boolean shift ) {
    return new KeyEvent(
      type, "", "", code, shift, false, false, false
    );
  }

  /**
   * JavaFX entry point.
   *
   * @param stage The primary application stage.
   */
  @Override
  public void start( final Stage stage ) {
    // Must be instantiated after the UI is initialized (i.e., not in main)
    // because it interacts with GUI properties.
    mWorkspace = new Workspace();

    initFonts();
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );

    stage.show();
    register( this );
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

    // After the app loses focus, when the user switches back using Alt+Tab,
    // the menu mnemonic is sometimes engaged, swallowing the first letter that
    // the user types---if it is a menu mnemonic. See MainScene::createScene().
    //
    // JavaFX Bug: https://bugs.openjdk.java.net/browse/JDK-8090647
    stage.focusedProperty().addListener( ( c, lost, show ) -> {
      for( final var menu : mMainScene.getMenuBar().getMenus() ) {
        menu.hide();
      }

      for( final var mnemonics : stage.getScene().getMnemonics().values() ) {
        for( final var mnemonic : mnemonics ) {
          mnemonic.getNode().fireEvent( keyUp( ALT ) );
        }
      }
    } );
  }

  private void initIcons( final Stage stage ) {
    stage.getIcons().addAll( LOGOS );
  }

  private void initScene( final Stage stage ) {
    mMainScene = new MainScene( mWorkspace );
    stage.setScene( mMainScene.getScene() );
  }

  /**
   * When a hyperlink website URL is clicked, this method is called to launch
   * the default browser to the event's location.
   *
   * @param event The event called when a hyperlink was clicked.
   */
  @Subscribe
  public void handle( final HyperlinkOpenEvent event ) {
    getHostServices().showDocument( event.getUri().toString() );
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
}
