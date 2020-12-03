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

import com.keenwrite.ExportFormat;
import com.keenwrite.MainView;
import com.keenwrite.editors.TextEditor;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.Messages.get;
import static javafx.event.Event.fireEvent;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Responsible for abstracting how functionality is mapped to the application.
 * This allows users to customize accelerator keys and will provide pluggable
 * functionality so that different text markup languages can change documents
 * using their respective syntax.
 */
@SuppressWarnings("NonAsciiCharacters")
public class ApplicationActions {
  /**
   * When an action is executed, this is one of the recipients.
   */
  private final MainView mMainView;

  public ApplicationActions( final MainView mainView ) {
    mMainView = mainView;
  }

  public void file‿new() {
    getMainView().newTextEditor();
  }

  public void file‿open() {
    getMainView().open( createFileChooser().openFiles() );
  }

  public void file‿close() {
  }

  public void file‿close_all() {
  }

  public void file‿save() {
    getMainView().save();
  }

  public void file‿save_as() {
    final var file = createFileChooser().saveAs();

    if( file != null ) {
      getMainView().saveAs( file );
    }
  }

  public void file‿save_all() {
    getMainView().saveAll();
  }

  public void file‿export‿html_svg() {
    file‿export( HTML_TEX_SVG );
  }

  public void file‿export‿html_tex() {
    file‿export( HTML_TEX_DELIMITED );
  }

  public void file‿export‿markdown() {
    file‿export( MARKDOWN_PLAIN );
  }

  private void file‿export( final ExportFormat format ) {
  }

  public void file‿exit() {
    final var window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
  }

  public void edit‿undo() {
    getActiveTextEditor().undo();
  }

  public void edit‿redo() {
    getActiveTextEditor().redo();
  }

  public void edit‿cut() {
    getActiveTextEditor().cut();
  }

  public void edit‿copy() {
    getActiveTextEditor().copy();
  }

  public void edit‿paste() {
    getActiveTextEditor().paste();
  }

  public void edit‿select_all() {
    getActiveTextEditor().selectAll();
  }

  public void edit‿find() {
  }

  public void edit‿find_next() {
  }

  public void edit‿preferences() {
  }

  public void format‿bold() {
    getActiveTextEditor().bold();
  }

  public void format‿italic() {
    getActiveTextEditor().italic();
  }

  public void format‿superscript() {
    getActiveTextEditor().superscript();
  }

  public void format‿subscript() {
    getActiveTextEditor().subscript();
  }

  public void format‿strikethrough() {
    getActiveTextEditor().strikethrough();
  }

  public void insert‿blockquote() {
    getActiveTextEditor().blockquote();
  }

  public void insert‿code() {
    getActiveTextEditor().code();
  }

  public void insert‿fenced_code_block() {
    getActiveTextEditor().fencedCodeBlock();
  }

  public void insert‿link() {
  }

  public void insert‿image() {
  }

  public void insert‿heading_1() {
    insert‿heading( 1 );
  }

  public void insert‿heading_2() {
    insert‿heading( 2 );
  }

  public void insert‿heading_3() {
    insert‿heading( 3 );
  }

  private void insert‿heading( final int level ) {
    getActiveTextEditor().heading( level );
  }

  public void insert‿unordered_list() {
    getActiveTextEditor().unorderedList();
  }

  public void insert‿ordered_list() {
    getActiveTextEditor().orderedList();
  }

  public void insert‿horizontal_rule() {
    getActiveTextEditor().horizontalRule();
  }

  public void definition‿create() {
  }

  public void definition‿insert() {
  }

  public void view‿refresh() {
  }

  public void view‿preview() {
  }

  public void help‿about() {
    final Alert alert = new Alert( INFORMATION );
    alert.setTitle( get( "Dialog.about.title", APP_TITLE ) );
    alert.setHeaderText( get( "Dialog.about.header", APP_TITLE ) );
    alert.setContentText( get( "Dialog.about.content" ) );
    alert.setGraphic( new ImageView( ICON_DIALOG ) );
    alert.initOwner( getWindow() );

    alert.showAndWait();
  }

  private FileChooserCommand createFileChooser() {
    return new FileChooserCommand( getWindow() );
  }

  private MainView getMainView() {
    return mMainView;
  }

  private TextEditor getActiveTextEditor() {
    return getMainView().getActiveTextEditor();
  }

  private Window getWindow() {
    return getMainView().getWindow();
  }
}
