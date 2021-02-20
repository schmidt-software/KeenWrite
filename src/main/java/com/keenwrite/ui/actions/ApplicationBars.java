/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.ui.controls.EventedStatusBar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import org.controlsfx.control.StatusBar;

import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.Messages.get;

/**
 * Responsible for wiring all application actions to menus, toolbar buttons,
 * and keyboard shortcuts.
 */
public final class ApplicationBars {

  private static final Map<String, Action> sMap = new HashMap<>( 64 );

  /**
   * Empty constructor.
   */
  public ApplicationBars() {
  }

  /**
   * Creates the main application affordances.
   *
   * @param actions The {@link ApplicationActions} that map user interface
   *                selections to executable code.
   * @return An instance of {@link MenuBar} that contains the menu.
   */
  public static MenuBar createMenuBar( final ApplicationActions actions ) {
    final var SEPARATOR_ACTION = new SeparatorAction();

    //@formatter:off
    return new MenuBar(
    createMenu(
      get( "Main.menu.file" ),
      addAction( "file.new", e -> actions.file‿new() ),
      addAction( "file.open", e -> actions.file‿open() ),
      SEPARATOR_ACTION,
      addAction( "file.close", e -> actions.file‿close() ),
      addAction( "file.close_all", e -> actions.file‿close_all() ),
      SEPARATOR_ACTION,
      addAction( "file.save", e -> actions.file‿save() ),
      addAction( "file.save_as", e -> actions.file‿save_as() ),
      addAction( "file.save_all", e -> actions.file‿save_all() ),
      SEPARATOR_ACTION,
      addAction( "file.export", e -> {} )
        .addSubActions(
          addAction( "file.export.html_svg", e -> actions.file‿export‿html_svg() ),
          addAction( "file.export.html_tex", e -> actions.file‿export‿html_tex() ),
          addAction( "file.export.markdown", e -> actions.file‿export‿markdown() )
        ),
      SEPARATOR_ACTION,
      addAction( "file.exit", e -> actions.file‿exit() )
    ),
    createMenu(
      get( "Main.menu.edit" ),
      SEPARATOR_ACTION,
      addAction( "edit.undo", e -> actions.edit‿undo() ),
      addAction( "edit.redo", e -> actions.edit‿redo() ),
      SEPARATOR_ACTION,
      addAction( "edit.cut", e -> actions.edit‿cut() ),
      addAction( "edit.copy", e -> actions.edit‿copy() ),
      addAction( "edit.paste", e -> actions.edit‿paste() ),
      addAction( "edit.select_all", e -> actions.edit‿select_all() ),
      SEPARATOR_ACTION,
      addAction( "edit.find", e -> actions.edit‿find() ),
      addAction( "edit.find_next", e -> actions.edit‿find_next() ),
      addAction( "edit.find_prev", e -> actions.edit‿find_prev() ),
      SEPARATOR_ACTION,
      addAction( "edit.preferences", e -> actions.edit‿preferences() )
    ),
    createMenu(
      get( "Main.menu.format" ),
      addAction( "format.bold", e -> actions.format‿bold() ),
      addAction( "format.italic", e -> actions.format‿italic() ),
      addAction( "format.superscript", e -> actions.format‿superscript() ),
      addAction( "format.subscript", e -> actions.format‿subscript() ),
      addAction( "format.strikethrough", e -> actions.format‿strikethrough() )
    ),
    createMenu(
      get( "Main.menu.insert" ),
      addAction( "insert.blockquote", e -> actions.insert‿blockquote() ),
      addAction( "insert.code", e -> actions.insert‿code() ),
      addAction( "insert.fenced_code_block", e -> actions.insert‿fenced_code_block() ),
      SEPARATOR_ACTION,
      addAction( "insert.link", e -> actions.insert‿link() ),
      addAction( "insert.image", e -> actions.insert‿image() ),
      SEPARATOR_ACTION,
      addAction( "insert.heading_1", e -> actions.insert‿heading_1() ),
      addAction( "insert.heading_2", e -> actions.insert‿heading_2() ),
      addAction( "insert.heading_3", e -> actions.insert‿heading_3() ),
      SEPARATOR_ACTION,
      addAction( "insert.unordered_list", e -> actions.insert‿unordered_list() ),
      addAction( "insert.ordered_list", e -> actions.insert‿ordered_list() ),
      addAction( "insert.horizontal_rule", e -> actions.insert‿horizontal_rule() )
    ),
    createMenu(
      get( "Main.menu.definition" ),
      addAction( "definition.insert", e -> actions.definition‿autoinsert() ),
      SEPARATOR_ACTION,
      addAction( "definition.create", e -> actions.definition‿create() ),
      addAction( "definition.rename", e -> actions.definition‿rename() ),
      addAction( "definition.delete", e -> actions.definition‿delete() )
    ),
    createMenu(
      get( "Main.menu.view" ),
      addAction( "view.refresh", e -> actions.view‿refresh() ),
      SEPARATOR_ACTION,
      addAction( "view.preview", e -> actions.view‿preview() ),
      addAction( "view.outline", e -> actions.view‿outline() ),
      addAction( "view.statistics", e-> actions.view‿statistics() ),
      SEPARATOR_ACTION,
      addAction( "view.menubar", e -> actions.view‿menubar() ),
      addAction( "view.toolbar", e -> actions.view‿toolbar() ),
      addAction( "view.statusbar", e -> actions.view‿statusbar() ),
      SEPARATOR_ACTION,
      addAction( "view.issues", e -> actions.view‿issues() )
    ),
    createMenu(
      get( "Main.menu.help" ),
      addAction( "help.about", e -> actions.help‿about() )
    ) );
    //@formatter:on
  }

  public static Node createToolBar() {
    final var SEPARATOR_ACTION = new SeparatorAction();

    return createToolBar(
      getAction( "file.new" ),
      getAction( "file.open" ),
      getAction( "file.save" ),
      SEPARATOR_ACTION,
      getAction( "edit.undo" ),
      getAction( "edit.redo" ),
      getAction( "edit.cut" ),
      getAction( "edit.copy" ),
      getAction( "edit.paste" ),
      SEPARATOR_ACTION,
      getAction( "format.bold" ),
      getAction( "format.italic" ),
      getAction( "format.superscript" ),
      getAction( "format.subscript" ),
      getAction( "insert.blockquote" ),
      getAction( "insert.code" ),
      getAction( "insert.fenced_code_block" ),
      SEPARATOR_ACTION,
      getAction( "insert.link" ),
      getAction( "insert.image" ),
      SEPARATOR_ACTION,
      getAction( "insert.heading_1" ),
      SEPARATOR_ACTION,
      getAction( "insert.unordered_list" ),
      getAction( "insert.ordered_list" )
    );
  }

  public static StatusBar createStatusBar() {
    return new EventedStatusBar();
  }

  /**
   * Adds a new action to the list of actions.
   *
   * @param key     The name of the action to register in {@link #sMap}.
   * @param handler Performs the action upon request.
   * @return The newly registered action.
   */
  private static Action addAction(
    final String key, final EventHandler<ActionEvent> handler ) {
    assert key != null;
    assert handler != null;

    final var action = Action
      .builder()
      .setId( key )
      .setHandler( handler )
      .build();

    sMap.put( key, action );

    return action;
  }

  private static Action getAction( final String key ) {
    return sMap.get( key );
  }

  public static Menu createMenu(
    final String text, final MenuAction... actions ) {
    return new Menu( text, null, createMenuItems( actions ) );
  }

  public static MenuItem[] createMenuItems( final MenuAction... actions ) {
    final var menuItems = new MenuItem[ actions.length ];

    for( var i = 0; i < actions.length; i++ ) {
      menuItems[ i ] = actions[ i ].createMenuItem();
    }

    return menuItems;
  }

  private static ToolBar createToolBar( final MenuAction... actions ) {
    return new ToolBar( createToolBarButtons( actions ) );
  }

  private static Node[] createToolBarButtons( final MenuAction... actions ) {
    final var len = actions.length;
    final var nodes = new Node[ len ];

    for( var i = 0; i < len; i++ ) {
      nodes[ i ] = actions[ i ].createToolBarNode();
    }

    return nodes;
  }
}
