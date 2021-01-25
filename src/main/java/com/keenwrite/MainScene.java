/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.FileModifiedListener;
import com.keenwrite.io.FileWatchService;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.ui.actions.ApplicationActions;
import com.keenwrite.ui.listeners.CaretListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;

import java.io.File;

import static com.keenwrite.Constants.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.StatusNotifier.getStatusBar;
import static com.keenwrite.preferences.ThemeProperty.toFilename;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_THEME_CUSTOM;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_THEME_SELECTION;
import static com.keenwrite.ui.actions.ApplicationBars.createMenuBar;
import static com.keenwrite.ui.actions.ApplicationBars.createToolBar;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.KeyCode.ALT;
import static javafx.scene.input.KeyCode.ALT_GRAPH;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * Responsible for creating the bar scene: menu bar, tool bar, and status bar.
 */
public final class MainScene {
  private final Scene mScene;
  private final MenuBar mMenuBar;
  private final Node mToolBar;
  private final StatusBar mStatusBar;
  private final FileWatchService mFileWatchService = new FileWatchService();
  private FileModifiedListener mStylesheetFileListener = event -> {};

  public MainScene( final Workspace workspace ) {
    final var mainPane = createMainPane( workspace );
    final var actions = createApplicationActions( mainPane );
    final var caretListener = createCaretListener( mainPane );
    mMenuBar = setManagedLayout( createMenuBar( actions ) );
    mToolBar = setManagedLayout( createToolBar() );
    mStatusBar = setManagedLayout( getStatusBar() );

    mStatusBar.getRightItems().add( caretListener );

    final var appPane = new BorderPane();
    appPane.setTop( new VBox( mMenuBar, mToolBar ) );
    appPane.setCenter( mainPane );
    appPane.setBottom( mStatusBar );

    final var watchThread = new Thread( mFileWatchService );
    watchThread.setDaemon( true );
    watchThread.start();

    mScene = createScene( appPane );
    initStylesheets( mScene, workspace );
  }

  /**
   * Called by the {@link MainApp} to get a handle on the {@link Scene}
   * created by an instance of {@link MainScene}.
   *
   * @return The {@link Scene} created at construction time.
   */
  public Scene getScene() {
    return mScene;
  }

  public void toggleMenuBar() {
    final var node = mMenuBar;
    node.setVisible( !node.isVisible() );
  }

  public void toggleToolBar() {
    final var node = mToolBar;
    node.setVisible( !node.isVisible() );
  }

  public void toggleStatusBar() {
    final var node = mStatusBar;
    node.setVisible( !node.isVisible() );
  }

  MenuBar getMenuBar() {
    return mMenuBar;
  }

  private void initStylesheets( final Scene scene, final Workspace workspace ) {
    final var internal = workspace.themeProperty( KEY_UI_THEME_SELECTION );
    final var external = workspace.fileProperty( KEY_UI_THEME_CUSTOM );
    final var inTheme = internal.get();
    final var exTheme = external.get();
    applyStylesheets( scene, inTheme, exTheme );

    internal.addListener(
      ( c, o, n ) -> applyStylesheets( scene, inTheme, exTheme )
    );

    external.addListener(
      ( c, o, n ) -> {
        if( o != null ) {
          mFileWatchService.unregister( o );
        }

        if( n != null ) {
          try {
            applyStylesheets( scene, inTheme, n );
          } catch( final Exception ex ) {
            // Changes to the CSS file won't autoload, which is okay.
            clue( ex );
          }
        }
      }
    );

    mFileWatchService.removeListener( mStylesheetFileListener );
    mStylesheetFileListener = event ->
      runLater( () -> applyStylesheets( scene, inTheme, event.getFile() ) );
    mFileWatchService.addListener( mStylesheetFileListener );
  }

  private String getStylesheet( final String filename ) {
    return get( STYLESHEET_APPLICATION_THEME, filename );
  }

  /**
   * Clears then re-applies all the internal stylesheets.
   *
   * @param scene    The scene to stylize.
   * @param internal The CSS file name bundled with the application.
   */
  private void applyStylesheets(
    final Scene scene, final String internal, final File external ) {
    final var stylesheets = scene.getStylesheets();
    stylesheets.clear();
    stylesheets.add( STYLESHEET_APPLICATION_BASE );
    stylesheets.add( STYLESHEET_MARKDOWN );
    stylesheets.add( getStylesheet( toFilename( internal ) ) );

    try {
      if( external.canRead() && !external.isDirectory() ) {
        stylesheets.add( external.toURI().toURL().toString() );

        mFileWatchService.register( external );
      }
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  private MainPane createMainPane( final Workspace workspace ) {
    return new MainPane( workspace );
  }

  private ApplicationActions createApplicationActions(
    final MainPane mainPane ) {
    return new ApplicationActions( this, mainPane );
  }

  /**
   * Creates the class responsible for updating the UI with the caret position
   * based on the active text editor.
   *
   * @return The {@link CaretListener} responsible for updating the
   * {@link StatusBar} whenever the caret changes position.
   */
  private CaretListener createCaretListener( final MainPane mainPane ) {
    return new CaretListener( mainPane.activeTextEditorProperty() );
  }

  /**
   * Creates a new scene that is attached to the given {@link Parent}.
   *
   * @param parent The container for the scene.
   * @return A scene to capture user interactions, UI styles, etc.
   */
  private Scene createScene( final Parent parent ) {
    final var scene = new Scene( parent );

    // After the app loses focus, when the user switches back using Alt+Tab,
    // the menu is sometimes engaged. See MainApp::initStage().
    //
    // JavaFX Bug: https://bugs.openjdk.java.net/browse/JDK-8090647
    scene.addEventHandler( KEY_PRESSED, event -> {
      // Only consume lone ALT key press events. If the modifier is used in
      // combination with another key, don't consume the event. First check
      // if ALT is down before getting the key code as a micro-optimization.
      if( event.isAltDown() ) {
        if( event.getCode() == ALT || event.getCode() == ALT_GRAPH ) {
          event.consume();
        }
      }
    } );

    return scene;
  }

  /**
   * Binds the visible property of the node to the managed property so that
   * hiding the node also removes the screen real estate that it occupies.
   * This allows the user to hide the menu bar, tool bar, etc.
   *
   * @param node The node to have its real estate bound to visibility.
   * @return The given node.
   */
  private <T extends Node> T setManagedLayout( final T node ) {
    node.managedProperty().bind( node.visibleProperty() );
    return node;
  }
}
