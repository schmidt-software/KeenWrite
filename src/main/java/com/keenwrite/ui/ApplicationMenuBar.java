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
package com.keenwrite.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.Messages.get;

public class ApplicationMenuBar {

  private static final Action SEPARATOR_ACTION = new SeparatorAction();

  private final ApplicationAction mActions = new ApplicationAction();
  private final Map<String, Action> mMap = new HashMap<>( 64 );

  public ApplicationMenuBar() {
  }

  public Node createMenuBar() {
    //@formatter:off
    putAction( "file.new", e -> mActions.file‿new() );
    putAction( "file.open", e -> mActions.file‿open() );
    putAction( "file.close", e -> mActions.file‿close() );
    putAction( "file.close_all", e -> mActions.file‿close_all() );
    putAction( "file.save", e -> mActions.file‿save() );
    putAction( "file.save_as", e -> mActions.file‿save_as() );
    putAction( "file.save_all", e -> mActions.file‿save_all() );
    putAction( "file.export.html_svg", e -> mActions.file‿export‿html_svg() );
    putAction( "file.export.html_tex", e -> mActions.file‿export‿html_tex() );
    putAction( "file.export.markdown", e -> mActions.file‿export‿markdown() );
    putAction( "file.exit", e -> mActions.file‿exit() );
    putAction( "edit.undo", e -> mActions.edit‿undo() );
    putAction( "edit.redo", e -> mActions.edit‿redo() );
    putAction( "edit.cut", e -> mActions.edit‿cut() );
    putAction( "edit.copy", e -> mActions.edit‿copy() );
    putAction( "edit.paste", e -> mActions.edit‿paste() );
    putAction( "edit.select_all", e -> mActions.edit‿select_all() );
    putAction( "edit.find", e -> mActions.edit‿find() );
    putAction( "edit.find_next", e -> mActions.edit‿find_next() );
    putAction( "edit.preferences", e -> mActions.edit‿preferences() );
    putAction( "format.bold", e -> mActions.format‿bold() );
    putAction( "format.italic", e -> mActions.format‿italic() );
    putAction( "format.superscript", e -> mActions.format‿superscript() );
    putAction( "format.subscript", e -> mActions.format‿subscript() );
    putAction( "format.strikethrough", e -> mActions.format‿strikethrough() );
    putAction( "insert.blockquote", e -> mActions.insert‿blockquote() );
    putAction( "insert.code", e -> mActions.insert‿code() );
    putAction( "insert.fenced_code_block", e -> mActions.insert‿fenced_code_block() );
    putAction( "insert.link", e -> mActions.insert‿link() );
    putAction( "insert.image", e -> mActions.insert‿image() );
    putAction( "insert.heading_1", e -> mActions.insert‿heading_1() );
    putAction( "insert.heading_2", e -> mActions.insert‿heading_2() );
    putAction( "insert.heading_3", e -> mActions.insert‿heading_3() );
    putAction( "insert.unordered_list", e -> mActions.insert‿unordered_list() );
    putAction( "insert.ordered_list", e -> mActions.insert‿ordered_list() );
    putAction( "insert.horizontal_rule", e -> mActions.insert‿horizontal_rule() );
    putAction( "definition.create", e -> mActions.definition‿create() );
    putAction( "definition.insert", e -> mActions.definition‿insert() );
    putAction( "view.refresh", e -> mActions.view‿refresh() );
    putAction( "view.preview", e -> mActions.view‿preview() );
    putAction( "help.about", e -> mActions.help‿about() );
    //@formatter:on

    final var menuFile = ActionUtils.createMenu(
        get( "Main.menu.file" ),
        getAction( "file.new" ),
        getAction( "file.open" ),
        SEPARATOR_ACTION,
        getAction( "file.close" ),
        getAction( "file.close_all" ),
        SEPARATOR_ACTION,
        getAction( "file.save" ),
        getAction( "file.save_as" ),
        getAction( "file.save_all" ),
        SEPARATOR_ACTION,
        // TODO: FIXME Export as HTML+SVG/HTML+TeX
        //actions.get( "file.export" ),
        SEPARATOR_ACTION,
        getAction( "file.exit" )
    );

    final var menuEdit = ActionUtils.createMenu(
        get( "Main.menu.edit" ),
        SEPARATOR_ACTION,
        getAction( "edit.undo" ),
        getAction( "edit.redo" ),
        SEPARATOR_ACTION,
        getAction( "edit.cut" ),
        getAction( "edit.copy" ),
        getAction( "edit.paste" ),
        getAction( "edit.select_all" ),
        SEPARATOR_ACTION,
        getAction( "edit.find" ),
        getAction( "edit.find_next" ),
        SEPARATOR_ACTION,
        getAction( "edit.preferences" )
    );

    final var menuFormat = ActionUtils.createMenu(
        get( "Main.menu.format" ),
        getAction( "format.bold" ),
        getAction( "format.italic" ),
        getAction( "format.superscript" ),
        getAction( "format.subscript" ),
        getAction( "format.strikethrough" )
    );

    final var menuInsert = ActionUtils.createMenu(
        get( "Main.menu.insert" ),
        getAction( "insert.blockquote" ),
        getAction( "insert.code" ),
        getAction( "insert.fenced_code_block" ),
        SEPARATOR_ACTION,
        getAction( "insert.link" ),
        getAction( "insert.image" ),
        SEPARATOR_ACTION,
        getAction( "insert.heading_1" ),
        getAction( "insert.heading_2" ),
        getAction( "insert.heading_3" ),
        SEPARATOR_ACTION,
        getAction( "insert.unordered_list" ),
        getAction( "insert.ordered_list" ),
        getAction( "insert.horizontal_rule" )
    );

    final var menuDefinition = ActionUtils.createMenu(
        get( "Main.menu.definition" ),
        getAction( "definition.create" ),
        getAction( "definition.insert" )
    );

    final var menuView = ActionUtils.createMenu(
        get( "Main.menu.view" ),
        getAction( "view.refresh" ),
        SEPARATOR_ACTION,
        getAction( "view.preview" )
    );

    final var menuHelp = ActionUtils.createMenu(
        get( "Main.menu.help" ),
        getAction( "help.about" ) );

    final var menuBar = new MenuBar(
        menuFile,
        menuEdit,
        menuFormat,
        menuInsert,
        menuDefinition,
        menuView,
        menuHelp );

    final var toolBar = ActionUtils.createToolBar(
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

    return new VBox( menuBar, toolBar );
  }

  private void putAction(
      final String key, final EventHandler<ActionEvent> handler ) {
    final var action = Action
        .builder()
        .setId( key )
        .setAction( handler )
        .build();

    mMap.put( key, action );
  }

  private Action getAction( final String key ) {
    return mMap.get( key );
  }
}
