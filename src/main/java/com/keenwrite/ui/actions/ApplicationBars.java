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
      addAction( "file.new", e -> actions.file_new() ),
      addAction( "file.open", e -> actions.file_open() ),
      SEPARATOR_ACTION,
      addAction( "file.close", e -> actions.file_close() ),
      addAction( "file.close_all", e -> actions.file_close_all() ),
      SEPARATOR_ACTION,
      addAction( "file.save", e -> actions.file_save() ),
      addAction( "file.save_as", e -> actions.file_save_as() ),
      addAction( "file.save_all", e -> actions.file_save_all() ),
      SEPARATOR_ACTION,
      addAction( "file.export", e -> {} )
        .addSubActions(
          addAction( "file.export.pdf", e -> actions.file_export_pdf() ),
          addAction( "file.export.pdf.dir", e -> actions.file_export_pdf_dir() ),
          addAction( "file.export.html_svg", e -> actions.file_export_html_svg() ),
          addAction( "file.export.html_tex", e -> actions.file_export_html_tex() ),
          addAction( "file.export.xhtml_tex", e -> actions.file_export_xhtml_tex() ),
          addAction( "file.export.markdown", e -> actions.file_export_markdown() )
        ),
      SEPARATOR_ACTION,
      addAction( "file.exit", e -> actions.file_exit() )
    ),
    createMenu(
      get( "Main.menu.edit" ),
      SEPARATOR_ACTION,
      addAction( "edit.undo", e -> actions.edit_undo() ),
      addAction( "edit.redo", e -> actions.edit_redo() ),
      SEPARATOR_ACTION,
      addAction( "edit.cut", e -> actions.edit_cut() ),
      addAction( "edit.copy", e -> actions.edit_copy() ),
      addAction( "edit.paste", e -> actions.edit_paste() ),
      addAction( "edit.select_all", e -> actions.edit_select_all() ),
      SEPARATOR_ACTION,
      addAction( "edit.find", e -> actions.edit_find() ),
      addAction( "edit.find_next", e -> actions.edit_find_next() ),
      addAction( "edit.find_prev", e -> actions.edit_find_prev() ),
      SEPARATOR_ACTION,
      addAction( "edit.preferences", e -> actions.edit_preferences() )
    ),
    createMenu(
      get( "Main.menu.format" ),
      addAction( "format.bold", e -> actions.format_bold() ),
      addAction( "format.italic", e -> actions.format_italic() ),
      addAction( "format.monospace", e -> actions.format_monospace() ),
      addAction( "format.superscript", e -> actions.format_superscript() ),
      addAction( "format.subscript", e -> actions.format_subscript() ),
      addAction( "format.strikethrough", e -> actions.format_strikethrough() )
    ),
    createMenu(
      get( "Main.menu.insert" ),
      addAction( "insert.blockquote", e -> actions.insert_blockquote() ),
      addAction( "insert.code", e -> actions.insert_code() ),
      addAction( "insert.fenced_code_block", e -> actions.insert_fenced_code_block() ),
      SEPARATOR_ACTION,
      addAction( "insert.link", e -> actions.insert_link() ),
      addAction( "insert.image", e -> actions.insert_image() ),
      SEPARATOR_ACTION,
      addAction( "insert.heading_1", e -> actions.insert_heading_1() ),
      addAction( "insert.heading_2", e -> actions.insert_heading_2() ),
      addAction( "insert.heading_3", e -> actions.insert_heading_3() ),
      SEPARATOR_ACTION,
      addAction( "insert.unordered_list", e -> actions.insert_unordered_list() ),
      addAction( "insert.ordered_list", e -> actions.insert_ordered_list() ),
      addAction( "insert.horizontal_rule", e -> actions.insert_horizontal_rule() )
    ),
    createMenu(
      get( "Main.menu.definition" ),
      addAction( "definition.insert", e -> actions.definition_autoinsert() ),
      SEPARATOR_ACTION,
      addAction( "definition.create", e -> actions.definition_create() ),
      addAction( "definition.rename", e -> actions.definition_rename() ),
      addAction( "definition.delete", e -> actions.definition_delete() )
    ),
    createMenu(
      get( "Main.menu.view" ),
      addAction( "view.refresh", e -> actions.view_refresh() ),
      SEPARATOR_ACTION,
      addAction( "view.preview", e -> actions.view_preview() ),
      addAction( "view.outline", e -> actions.view_outline() ),
      addAction( "view.statistics", e-> actions.view_statistics() ),
      addAction( "view.files", e-> actions.view_files() ),
      SEPARATOR_ACTION,
      addAction( "view.menubar", e -> actions.view_menubar() ),
      addAction( "view.toolbar", e -> actions.view_toolbar() ),
      addAction( "view.statusbar", e -> actions.view_statusbar() ),
      SEPARATOR_ACTION,
      addAction( "view.log", e -> actions.view_log() )
    ),
    createMenu(
      get( "Main.menu.help" ),
      addAction( "help.about", e -> actions.help_about() )
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
      getAction( "file.export.pdf" ),
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
