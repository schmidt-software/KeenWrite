/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.preferences.Workspace;
import com.keenwrite.ui.actions.ApplicationActions;
import com.keenwrite.ui.listeners.CaretListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;

import java.io.File;

import static com.keenwrite.Constants.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusNotifier.getStatusBar;
import static com.keenwrite.preferences.ThemeProperty.toFilename;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_THEME_CUSTOM;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_THEME_SELECTION;
import static com.keenwrite.ui.actions.ApplicationBars.createMenuBar;
import static com.keenwrite.ui.actions.ApplicationBars.createToolBar;

/**
 * Responsible for creating the bar scene: menu bar, tool bar, and status bar.
 */
public final class MainScene {
  private final Scene mScene;
  private final Node mMenuBar;
  private final Node mToolBar;
  private final StatusBar mStatusBar;

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

  private void initStylesheets( final Scene scene, final Workspace workspace ) {
    final var internal = workspace.themeProperty( KEY_UI_THEME_SELECTION );
    applyStylesheets( scene, internal.get() );

    internal.addListener( ( c, o, n ) -> {
      applyStylesheets( scene, internal.get() );
    } );

    final var external = workspace.fileProperty( KEY_UI_THEME_CUSTOM );
    external.addListener( ( c, o, n ) -> {
      applyStylesheets( scene, internal.get(), external.get() );
    } );
  }

  private String getStylesheet( final String filename ) {
    return get( STYLESHEET_APPLICATION_THEME, filename );
  }

  private void applyStylesheets( final Scene scene, final String filename ) {
    final var stylesheets = scene.getStylesheets();
    stylesheets.clear();
    stylesheets.add( STYLESHEET_APPLICATION_BASE );
    stylesheets.add( STYLESHEET_MARKDOWN );
    stylesheets.add( getStylesheet( toFilename( filename ) ) );
  }

  private void applyStylesheets( final Scene scene, final String internal,
                                 final File external ) {

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

  private Scene createScene( final Parent parent ) {
    return new Scene( parent );
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
