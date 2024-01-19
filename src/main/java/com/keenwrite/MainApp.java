/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.cmdline.HeadlessApp;
import com.keenwrite.events.HyperlinkOpenEvent;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.MathRenderer;
import com.keenwrite.spelling.impl.Lexicon;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.greenrobot.eventbus.Subscribe;

import java.io.PrintStream;
import java.util.function.BooleanSupplier;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.constants.GraphicsConstants.LOGOS;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.preferences.AppKeys.*;
import static com.keenwrite.util.FontLoader.initFonts;
import static javafx.scene.input.KeyCode.F11;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;
import static javafx.stage.WindowEvent.WINDOW_SHOWN;

/**
 * Application entry point. The application allows users to edit plain text
 * files in a markup notation and see a real-time preview of the formatted
 * output.
 */
public final class MainApp extends Application {

  private Workspace mWorkspace;

  /**
   * TODO: Delete this after JavaFX/GTK 3 no longer barfs useless warnings.
   */
  @SuppressWarnings( "SameParameterValue" )
  private static void stderrRedirect( final PrintStream stream ) {
    System.setErr( stream );
  }

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
   * Creates an instance of {@link KeyEvent} that represents a key released
   * event without any modifier keys held.
   *
   * @param code The key code representing a key to simulate releasing.
   * @return An instance of {@link KeyEvent}.
   */
  public static Event keyDown( final KeyCode code ) {
    return keyDown( code, false );
  }

  /**
   * Creates an instance of {@link KeyEvent} that represents releasing a key.
   *
   * @param code  The key to simulate being released up.
   * @param shift Whether shift key modifier shall modify the key code.
   * @return An instance of {@link KeyEvent} that may be used to simulate
   * a key being released.
   */
  @SuppressWarnings( "unused" )
  public static Event keyUp( final KeyCode code, final boolean shift ) {
    return keyEvent( KEY_RELEASED, code, shift );
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

    // The locale was already loaded when the workspace was created. This
    // ensures that when the locale preference changes, a new spellchecker
    // instance will be loaded and applied.
    final var property = mWorkspace.localeProperty( KEY_LANGUAGE_LOCALE );
    property.addListener( ( _, _, _ ) -> readLexicon() );

    initFonts();
    initState( stage );
    initStage( stage );
    initIcons( stage );
    initScene( stage );

    MathRenderer.bindSize( mWorkspace.doubleProperty( KEY_UI_FONT_MATH_SIZE ) );

    // Load the lexicon and check all the documents after all files are open.
    stage.addEventFilter( WINDOW_SHOWN, _ -> readLexicon() );
    stage.show();

    stderrRedirect( System.out );

    register( this );
  }

  private void initState( final Stage stage ) {
    final var enable = createBoundsEnabledSupplier( stage );

    stage.setX( mWorkspace.getDouble( KEY_UI_WINDOW_X ) );
    stage.setY( mWorkspace.getDouble( KEY_UI_WINDOW_Y ) );
    stage.setWidth( mWorkspace.getDouble( KEY_UI_WINDOW_W ) );
    stage.setHeight( mWorkspace.getDouble( KEY_UI_WINDOW_H ) );
    stage.setMaximized( mWorkspace.getBoolean( KEY_UI_WINDOW_MAX ) );
    stage.setFullScreen( mWorkspace.getBoolean( KEY_UI_WINDOW_FULL ) );

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
    final var mainScene = new MainScene( mWorkspace );
    stage.setScene( mainScene.getScene() );
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
   * This will load the lexicon for the user's preferred locale and fire
   * an event when the all entries in the lexicon have been loaded.
   */
  private void readLexicon() {
    Lexicon.read( mWorkspace.getLocale() );
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
