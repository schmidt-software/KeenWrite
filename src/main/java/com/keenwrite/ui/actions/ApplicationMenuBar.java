/* Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.ui.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.Messages.get;

/**
 * Responsible for wiring all application actions to menus, toolbar buttons,
 * and keyboard shortcuts.
 */
public class ApplicationMenuBar {

  private final ApplicationActions mActions;
  private final Map<String, Action> mMap = new HashMap<>( 64 );

  /**
   * Empty constructor.
   */
  public ApplicationMenuBar( final ApplicationActions actions ) {
    mActions = actions;
  }

  /**
   * Creates the main application affordances.
   *
   * @return An instance of {@link Node} that contains the menu and toolbar.
   */
  public Node createMenuBar() {
    final var SEPARATOR_ACTION = new SeparatorAction();

    //@formatter:off
    final var menuBar = new MenuBar(
    createMenu(
      get( "Main.menu.file" ),
      addAction( "file.new", e -> mActions.file‿new() ),
      addAction( "file.open", e -> mActions.file‿open() ),
      SEPARATOR_ACTION,
      addAction( "file.close", e -> mActions.file‿close() ),
      addAction( "file.close_all", e -> mActions.file‿close_all() ),
      SEPARATOR_ACTION,
      addAction( "file.save", e -> mActions.file‿save() ),
      addAction( "file.save_as", e -> mActions.file‿save_as() ),
      addAction( "file.save_all", e -> mActions.file‿save_all() ),
      SEPARATOR_ACTION,
      addAction( "file.export", e -> {} )
        .addSubActions(
          addAction( "file.export.html_svg", e -> mActions.file‿export‿html_svg() ),
          addAction( "file.export.html_tex", e -> mActions.file‿export‿html_tex() ),
          addAction( "file.export.markdown", e -> mActions.file‿export‿markdown() )
        ),
      SEPARATOR_ACTION,
      addAction( "file.exit", e -> mActions.file‿exit() )
    ),
    createMenu(
      get( "Main.menu.edit" ),
      SEPARATOR_ACTION,
      addAction( "edit.undo", e -> mActions.edit‿undo() ),
      addAction( "edit.redo", e -> mActions.edit‿redo() ),
      SEPARATOR_ACTION,
      addAction( "edit.cut", e -> mActions.edit‿cut() ),
      addAction( "edit.copy", e -> mActions.edit‿copy() ),
      addAction( "edit.paste", e -> mActions.edit‿paste() ),
      addAction( "edit.select_all", e -> mActions.edit‿select_all() ),
      SEPARATOR_ACTION,
      addAction( "edit.find", e -> mActions.edit‿find() ),
      addAction( "edit.find_next", e -> mActions.edit‿find_next() ),
      SEPARATOR_ACTION,
      addAction( "edit.preferences", e -> mActions.edit‿preferences() )
    ),
    createMenu(
      get( "Main.menu.format" ),
      addAction( "format.bold", e -> mActions.format‿bold() ),
      addAction( "format.italic", e -> mActions.format‿italic() ),
      addAction( "format.superscript", e -> mActions.format‿superscript() ),
      addAction( "format.subscript", e -> mActions.format‿subscript() ),
      addAction( "format.strikethrough", e -> mActions.format‿strikethrough() )
    ),
    createMenu(
      get( "Main.menu.insert" ),
      addAction( "insert.blockquote", e -> mActions.insert‿blockquote() ),
      addAction( "insert.code", e -> mActions.insert‿code() ),
      addAction( "insert.fenced_code_block", e -> mActions.insert‿fenced_code_block() ),
      SEPARATOR_ACTION,
      addAction( "insert.link", e -> mActions.insert‿link() ),
      addAction( "insert.image", e -> mActions.insert‿image() ),
      SEPARATOR_ACTION,
      addAction( "insert.heading_1", e -> mActions.insert‿heading_1() ),
      addAction( "insert.heading_2", e -> mActions.insert‿heading_2() ),
      addAction( "insert.heading_3", e -> mActions.insert‿heading_3() ),
      SEPARATOR_ACTION,
      addAction( "insert.unordered_list", e -> mActions.insert‿unordered_list() ),
      addAction( "insert.ordered_list", e -> mActions.insert‿ordered_list() ),
      addAction( "insert.horizontal_rule", e -> mActions.insert‿horizontal_rule() )
    ),
    createMenu(
      get( "Main.menu.definition" ),
      addAction( "definition.create", e -> mActions.definition‿create() ),
      addAction( "definition.insert", e -> mActions.definition‿insert() )
    ),
    createMenu(
      get( "Main.menu.view" ),
      addAction( "view.refresh", e -> mActions.view‿refresh() ),
      SEPARATOR_ACTION,
      addAction( "view.preview", e -> mActions.view‿preview() )
    ),
    createMenu(
      get( "Main.menu.help" ),
      addAction( "help.about", e -> mActions.help‿about() )
    )
    //@formatter:on
    );

    //@formatter:off
    final var toolBar = createToolBar(
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
      getAction( "insert.ordered_list" ) );
    //@formatter:on

    return new VBox( menuBar, toolBar );
  }

  /**
   * Adds a new action to the list of actions.
   *
   * @param key     The name of the action to register in {@link #mMap}.
   * @param handler Performs the action upon request.
   * @return The newly registered action.
   */
  private Action addAction(
      final String key, final EventHandler<ActionEvent> handler ) {
    assert key != null;
    assert handler != null;

    final var action = Action
        .builder()
        .setId( key )
        .setHandler( handler )
        .build();

    mMap.put( key, action );

    return action;
  }

  private Action getAction( final String key ) {
    return mMap.get( key );
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
