/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.io.FileModifiedListener;
import com.keenwrite.io.FileWatchService;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.ui.actions.GuiCommands;
import com.keenwrite.ui.listeners.CaretStatus;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.text.MessageFormat;

import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.events.ScrollLockEvent.fireScrollLockEvent;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.KEY_UI_SKIN_CUSTOM;
import static com.keenwrite.preferences.AppKeys.KEY_UI_SKIN_SELECTION;
import static com.keenwrite.preferences.SkinProperty.toFilename;
import static com.keenwrite.ui.actions.ApplicationBars.*;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.KeyCode.SCROLL_LOCK;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

/**
 * Responsible for creating the bar scene: menu bar, toolbar, and status bar.
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
    final var caretStatus = createCaretStatus();

    mMenuBar = setManagedLayout( createMenuBar( actions ) );
    mToolBar = setManagedLayout( createToolBar() );
    mStatusBar = setManagedLayout( createStatusBar() );

    mStatusBar.getRightItems().add( caretStatus );

    final var appPane = new BorderPane();
    appPane.setTop( new VBox( mMenuBar, mToolBar ) );
    appPane.setCenter( mainPane );
    appPane.setBottom( mStatusBar );

    final var fileWatcher = new Thread( mFileWatchService );
    fileWatcher.setDaemon( true );
    fileWatcher.start();

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

  public StatusBar getStatusBar() {return mStatusBar;}

  private void initStylesheets( final Scene scene, final Workspace workspace ) {
    final var internal = workspace.skinProperty( KEY_UI_SKIN_SELECTION );
    final var external = workspace.fileProperty( KEY_UI_SKIN_CUSTOM );
    final var inSkin = internal.get();
    final var exSkin = external.get();
    applyStylesheets( scene, inSkin, exSkin );

    internal.addListener(
      ( c, o, n ) -> {
        if( n != null ) {
          applyStylesheets( scene, n, exSkin );
        }
      }
    );

    external.addListener(
      ( c, o, n ) -> {
        if( o != null ) {
          mFileWatchService.unregister( o );
        }

        if( n != null ) {
          try {
            applyStylesheets( scene, inSkin, n );
          } catch( final Exception ex ) {
            // Changes to the CSS file won't autoload, which is okay.
            clue( ex );
          }
        }
      }
    );

    mFileWatchService.removeListener( mStylesheetFileListener );
    mStylesheetFileListener = event ->
      runLater( () -> applyStylesheets( scene, inSkin, event.getFile() ) );
    mFileWatchService.addListener( mStylesheetFileListener );
  }

  private String getStylesheet( final String filename ) {
    return MessageFormat.format( STYLESHEET_APPLICATION_SKIN, filename );
  }

  /**
   * Clears then re-applies all the internal stylesheets.
   *
   * @param scene    The scene to stylize.
   * @param internal The CSS file name bundled with the application.
   * @param external The (optional) customized CSS file specified by the user.
   */
  private void applyStylesheets(
    final Scene scene, final String internal, final File external ) {
    final var stylesheets = scene.getStylesheets();
    stylesheets.clear();
    stylesheets.add( STYLESHEET_APPLICATION_BASE );
    stylesheets.add( STYLESHEET_MARKDOWN );
    stylesheets.add( getStylesheet( toFilename( internal ) ) );

    try {
      if( external != null && external.canRead() && !external.isDirectory() ) {
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

  private GuiCommands createApplicationActions( final MainPane mainPane ) {
    return new GuiCommands( this, mainPane );
  }

  /**
   * Creates the class responsible for updating the UI with the caret position
   * based on the active text editor.
   *
   * @return The {@link CaretStatus} responsible for updating the
   * {@link StatusBar} whenever the caret changes position.
   */
  private CaretStatus createCaretStatus() {
    return new CaretStatus();
  }

  /**
   * Creates a new scene that is attached to the given {@link Parent}.
   *
   * @param parent The container for the scene.
   * @return A scene to capture user interactions, UI styles, etc.
   */
  private Scene createScene( final Parent parent ) {
    final var scene = new Scene( parent );

    // Update the synchronized scrolling status when user presses scroll lock.
    scene.addEventHandler( KEY_RELEASED, event -> {
      if( event.getCode() == SCROLL_LOCK ) {
        fireScrollLockEvent();
      }
    } );

    return scene;
  }

  /**
   * Binds the visible property of the node to the managed property so that
   * hiding the node also removes the screen real estate that it occupies.
   * This allows the user to hide the menu bar, toolbar, etc.
   *
   * @param node The node to have its real estate bound to visibility.
   * @return The given node for fluent-like convenience.
   */
  private <T extends Node> T setManagedLayout( final T node ) {
    node.managedProperty().bind( node.visibleProperty() );
    return node;
  }
}
