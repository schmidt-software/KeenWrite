/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
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
import org.jetbrains.annotations.NotNull;

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
   * @param actions The {@link GuiCommands} that map user interface
   *                selections to executable code.
   * @return An instance of {@link MenuBar} that contains the menu.
   */
  public static MenuBar createMenuBar( final GuiCommands actions ) {
    final var SEPARATOR = new SeparatorAction();

    return new MenuBar(
      createMenuFile( actions, SEPARATOR ),
      createMenuEdit( actions, SEPARATOR ),
      createMenuFormat( actions ),
      createMenuInsert( actions, SEPARATOR ),
      createMenuVariable( actions, SEPARATOR ),
      createMenuView( actions, SEPARATOR ),
      createMenuHelp( actions )
    );
  }

  @NotNull
  private static Menu createMenuFile(
    final GuiCommands actions, final SeparatorAction SEPARATOR ) {
    // @formatter:off
    return createMenu(
      get( "Main.menu.file" ),
      addAction( "file.new", _ -> actions.file_new() ),
      addAction( "file.open", _ -> actions.file_open() ),
      addAction( "file.open_url", _ -> actions.file_open_url() ),
      SEPARATOR,
      addAction( "file.close", _ -> actions.file_close() ),
      addAction( "file.close_all", _ -> actions.file_close_all() ),
      SEPARATOR,
      addAction( "file.save", _ -> actions.file_save() ),
      addAction( "file.save_as", _ -> actions.file_save_as() ),
      addAction( "file.save_all", _ -> actions.file_save_all() ),
      SEPARATOR,
      addAction( "file.export", _ -> { } )
        .addSubActions(
          addAction( "file.export.pdf", _ -> actions.file_export_pdf() ),
          addAction( "file.export.pdf.dir", _ -> actions.file_export_pdf_dir() ),
          addAction( "file.export.pdf.repeat", _ -> actions.file_export_repeat() ),
          addAction( "file.export.html.dir", _ -> actions.file_export_html_dir() ),
          addAction( "file.export.html_svg", _ -> actions.file_export_html_svg() ),
          addAction( "file.export.html_tex", _ -> actions.file_export_html_tex() ),
          addAction( "file.export.xhtml_tex", _ -> actions.file_export_xhtml_tex() )
        ),
      SEPARATOR,
      addAction( "file.exit", _ -> actions.file_exit() )
    );
    // @formatter:on
  }

  @NotNull
  private static Menu createMenuEdit(
    final GuiCommands actions, final SeparatorAction SEPARATOR ) {
    return createMenu(
      get( "Main.menu.edit" ),
      SEPARATOR,
      addAction( "edit.undo", _ -> actions.edit_undo() ),
      addAction( "edit.redo", _ -> actions.edit_redo() ),
      SEPARATOR,
      addAction( "edit.cut", _ -> actions.edit_cut() ),
      addAction( "edit.copy", _ -> actions.edit_copy() ),
      addAction( "edit.paste", _ -> actions.edit_paste() ),
      addAction( "edit.select_all", _ -> actions.edit_select_all() ),
      SEPARATOR,
      addAction( "edit.find", _ -> actions.edit_find() ),
      addAction( "edit.find_next", _ -> actions.edit_find_next() ),
      addAction( "edit.find_prev", _ -> actions.edit_find_prev() ),
      SEPARATOR,
      addAction( "edit.preferences", _ -> actions.edit_preferences() )
    );
  }

  @NotNull
  private static Menu createMenuFormat( final GuiCommands actions ) {
    return createMenu(
      get( "Main.menu.format" ),
      addAction( "format.bold", _ -> actions.format_bold() ),
      addAction( "format.italic", _ -> actions.format_italic() ),
      addAction( "format.monospace", _ -> actions.format_monospace() ),
      addAction( "format.superscript", _ -> actions.format_superscript() ),
      addAction( "format.subscript", _ -> actions.format_subscript() ),
      addAction( "format.strikethrough", _ -> actions.format_strikethrough() )
    );
  }

  @NotNull
  private static Menu createMenuInsert(
    final GuiCommands actions,
    final SeparatorAction SEPARATOR ) {
    // @formatter:off
    return createMenu(
      get( "Main.menu.insert" ),
      addAction( "insert.blockquote", _ -> actions.insert_blockquote() ),
      addAction( "insert.code", _ -> actions.insert_code() ),
      addAction( "insert.fenced_code_block", _ -> actions.insert_fenced_code_block() ),
      SEPARATOR,
      addAction( "insert.link", _ -> actions.insert_link() ),
      addAction( "insert.image", _ -> actions.insert_image() ),
      SEPARATOR,
      addAction( "insert.heading_1", _ -> actions.insert_heading_1() ),
      addAction( "insert.heading_2", _ -> actions.insert_heading_2() ),
      addAction( "insert.heading_3", _ -> actions.insert_heading_3() ),
      SEPARATOR,
      addAction( "insert.unordered_list", _ -> actions.insert_unordered_list() ),
      addAction( "insert.ordered_list", _ -> actions.insert_ordered_list() ),
      addAction( "insert.horizontal_rule", _ -> actions.insert_horizontal_rule() )
    );
    // @formatter:on
  }

  @NotNull
  private static Menu createMenuVariable(
    final GuiCommands actions, final SeparatorAction SEPARATOR ) {
    return createMenu(
      get( "Main.menu.definition" ),
      addAction( "definition.insert", _ -> actions.definition_autoinsert() ),
      SEPARATOR,
      addAction( "definition.create", _ -> actions.definition_create() ),
      addAction( "definition.rename", _ -> actions.definition_rename() ),
      addAction( "definition.delete", _ -> actions.definition_delete() )
    );
  }

  @NotNull
  private static Menu createMenuView(
    final GuiCommands actions, final SeparatorAction SEPARATOR ) {
    return createMenu(
      get( "Main.menu.view" ),
      addAction( "view.refresh", _ -> actions.view_refresh() ),
      SEPARATOR,
      addAction( "view.preview", _ -> actions.view_preview() ),
      addAction( "view.outline", _ -> actions.view_outline() ),
      addAction( "view.statistics", _ -> actions.view_statistics() ),
      addAction( "view.files", _ -> actions.view_files() ),
      SEPARATOR,
      addAction( "view.menubar", _ -> actions.view_menubar() ),
      addAction( "view.toolbar", _ -> actions.view_toolbar() ),
      addAction( "view.statusbar", _ -> actions.view_statusbar() ),
      SEPARATOR,
      addAction( "view.log", _ -> actions.view_log() )
    );
  }

  @NotNull
  private static Menu createMenuHelp( final GuiCommands actions ) {
    return createMenu(
      get( "Main.menu.help" ),
      addAction( "help.about", _ -> actions.help_about() )
    );
  }

  public static Node createToolBar() {
    final var SEPARATOR = new SeparatorAction();

    return createToolBar(
      getAction( "file.new" ),
      getAction( "file.open" ),
      getAction( "file.save" ),
      SEPARATOR,
      getAction( "file.export.pdf" ),
      SEPARATOR,
      getAction( "edit.undo" ),
      getAction( "edit.redo" ),
      getAction( "edit.cut" ),
      getAction( "edit.copy" ),
      getAction( "edit.paste" ),
      SEPARATOR,
      getAction( "format.bold" ),
      getAction( "format.italic" ),
      getAction( "format.superscript" ),
      getAction( "format.subscript" ),
      getAction( "insert.blockquote" ),
      getAction( "insert.code" ),
      getAction( "insert.fenced_code_block" ),
      SEPARATOR,
      getAction( "insert.link" ),
      getAction( "insert.image" ),
      SEPARATOR,
      getAction( "insert.heading_1" ),
      SEPARATOR,
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
