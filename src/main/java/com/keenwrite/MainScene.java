/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.WorkspacePreferences;
import com.keenwrite.ui.actions.ApplicationActions;
import com.keenwrite.ui.actions.ApplicationMenuBar;
import com.keenwrite.ui.listeners.CaretListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.StatusBar;

import static com.keenwrite.Constants.STYLESHEET_SCENE;

/**
 * Responsible for creating the bar scene: menu bar, tool bar, and status bar.
 */
public class MainScene {
  private final Scene mScene;

  public MainScene( final WorkspacePreferences preferences ) {
    final var mainPane = createMainPane( preferences );
    final var actions = createApplicationActions( mainPane );
    final var menuBar = createMenuBar( actions );
    final var appPane = new BorderPane();
    final var statusBar = StatusBarNotifier.getStatusBar();
    final var caretListener = createCaretListener( mainPane );

    statusBar.getRightItems().add( caretListener );

    appPane.setTop( menuBar );
    appPane.setCenter( mainPane );
    appPane.setBottom( statusBar );

    mScene = createScene( appPane );
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

  private MainPane createMainPane( final WorkspacePreferences preferences ) {
    return new MainPane( preferences );
  }

  private ApplicationActions createApplicationActions(
    final MainPane mainPane ) {
    return new ApplicationActions( mainPane );
  }

  private Node createMenuBar( final ApplicationActions actions ) {
    return (new ApplicationMenuBar()).createMenuBar( actions );
  }

  private Scene createScene( final Parent parent ) {
    final var scene = new Scene( parent );
    final var stylesheets = scene.getStylesheets();
    stylesheets.add( STYLESHEET_SCENE );

    return scene;
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
}
