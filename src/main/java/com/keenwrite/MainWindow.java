/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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

import com.keenwrite.definition.DefinitionEditor;
import com.keenwrite.editors.DefinitionNameInjector;
import com.keenwrite.editors.markdown.MarkdownEditorPane;
import com.keenwrite.preferences.UserPreferencesView;
import com.keenwrite.ui.actions.Action;
import com.keenwrite.ui.actions.MenuAction;
import com.keenwrite.ui.actions.SeparatorAction;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.controlsfx.control.StatusBar;

import static com.keenwrite.Messages.get;
import static com.keenwrite.ui.actions.ApplicationMenuBar.createMenu;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @deprecated Use {@link MainPane}.
 */
@Deprecated
public class MainWindow {

  private final TextField mFindTextField;
  private final DefinitionEditor mDefinitionPane = new DefinitionEditor( null );
  private final FileEditorTabPane mFileEditorPane = new FileEditorTabPane();

  /**
   * Listens on the definition pane for double-click events.
   */
  private final DefinitionNameInjector mDefinitionNameInjector
      = new DefinitionNameInjector( mDefinitionPane );

  public MainWindow() {
    mFindTextField = createFindTextField();
  }

  /**
   * Called after the stage is shown.
   */
  public void init() {
    initFindInput();
    initVariableNameInjector();
  }

  /**
   * Initialize the find input text field to listen on F3, ENTER, and
   * ESCAPE key presses.
   */
  private void initFindInput() {
    final TextField input = getFindTextField();

    input.setOnKeyPressed( ( KeyEvent event ) -> {
      switch( event.getCode() ) {
        case F3:
        case ENTER:
          editFindNext();
          break;
        case F:
          if( !event.isControlDown() ) {
            break;
          }
        case ESCAPE:
          getStatusBar().setGraphic( null );
          getActiveFileEditorTab().getEditorPane().requestFocus();
          break;
      }
    } );

    // Remove when the input field loses focus.
    input.focusedProperty().addListener(
        ( focused, oldFocus, newFocus ) -> {
          if( !newFocus ) {
            getStatusBar().setGraphic( null );
          }
        }
    );
  }

  private void initVariableNameInjector() {
    updateVariableNameInjector( getActiveFileEditorTab() );
  }

  private void updateVariableNameInjector( final FileEditorController tab ) {
    getDefinitionNameInjector().addListener( tab );
  }

  //---- Edit actions -------------------------------------------------------

  /**
   * Used to find text in the active file editor window.
   */
  private void editFind() {
    final TextField input = getFindTextField();
    getStatusBar().setGraphic( input );
    input.requestFocus();
  }

  public void editFindNext() {
    getActiveFileEditorTab().searchNext( getFindTextField().getText() );
  }

  public void editPreferences() {
    getUserPreferencesView().show();
  }

  //---- Member creators ----------------------------------------------------

  private TextField createFindTextField() {
    return new TextField();
  }

  private Node createMenuBar() {
    // Edit actions
    final Action editFindAction = Action
        .builder()
        .setText( "Main.menu.edit.find" )
        .setAccelerator( "Ctrl+F" )
        .setIcon( SEARCH )
        .setHandler( e -> editFind() )
        .build();
    final Action editFindNextAction = Action
        .builder()
        .setText( "Main.menu.edit.find_next" )
        .setAccelerator( "F3" )
        .setHandler( e -> editFindNext() )
        .build();
    final Action editPreferencesAction = Action
        .builder()
        .setText( "Main.menu.edit.preferences" )
        .setAccelerator( "Ctrl+Alt+S" )
        .setHandler( e -> editPreferences() )
        .build();

    // Insert actions
    final Action insertLinkAction = Action
        .builder()
        .setText( "Main.menu.insert.link" )
        .setAccelerator( "Shortcut+L" )
        .setIcon( LINK )
        .setHandler( e -> getActiveEditorPane().insertLink() )
        .build();
    final Action insertImageAction = Action
        .builder()
        .setText( "Main.menu.insert.image" )
        .setAccelerator( "Shortcut+G" )
        .setIcon( PICTURE_ALT )
        .setHandler( e -> getActiveEditorPane().insertImage() )
        .build();

    final MenuAction SEPARATOR_ACTION = new SeparatorAction();

    //---- MenuBar ----

    // Edit Menu
    final var editMenu = createMenu(
        get( "Main.menu.edit" ),
        editFindAction,
        editFindNextAction,
        SEPARATOR_ACTION,
        editPreferencesAction );

    // Insert Menu
    final var insertMenu = createMenu(
        get( "Main.menu.insert" ),
        insertLinkAction,
        insertImageAction
    );

    //---- MenuBar ----
    final var menuBar = new MenuBar(
        editMenu,
        insertMenu );

    return new VBox( menuBar );
  }

  //---- Convenience accessors ----------------------------------------------

  private MarkdownEditorPane getActiveEditorPane() {
    return getActiveFileEditorTab().getEditorPane();
  }

  private FileEditorController getActiveFileEditorTab() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------

  private FileEditorTabPane getFileEditorPane() {
    return mFileEditorPane;
  }

  private DefinitionEditor getDefinitionPane() {
    return mDefinitionPane;
  }

  private StatusBar getStatusBar() {
    return StatusBarNotifier.getStatusBar();
  }

  private TextField getFindTextField() {
    return mFindTextField;
  }

  private DefinitionNameInjector getDefinitionNameInjector() {
    return mDefinitionNameInjector;
  }

  //---- Persistence accessors ----------------------------------------------

  private UserPreferencesView getUserPreferencesView() {
    return UserPreferencesView.getInstance();
  }
}
