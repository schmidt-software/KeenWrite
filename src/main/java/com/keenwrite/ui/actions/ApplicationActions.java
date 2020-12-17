/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.ExportFormat;
import com.keenwrite.MainPane;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.io.File;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.search.SearchModel;
import com.keenwrite.ui.controls.SearchBar;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.ICON_DIALOG;
import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.StatusBarNotifier.getStatusBar;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
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
  private static final String STYLE_SEARCH = "search";

  /**
   * When an action is executed, this is one of the recipients.
   */
  private final MainPane mMainPane;

  /**
   * Tracks finding text in the active document.
   */
  private final SearchModel mSearchModel;

  public ApplicationActions( final MainPane mainPane ) {
    mMainPane = mainPane;
    mSearchModel = new SearchModel();
    mSearchModel.matchOffsetProperty().addListener( ( c, o, n ) -> {
      final var editor = getActiveTextEditor();

      // Clear highlighted areas before adding highlighting to a new region.
      if( o != null ) {
        editor.unstylize( STYLE_SEARCH );
      }

      if( n != null ) {
        editor.moveTo( n.getStart() );
        editor.stylize( n, STYLE_SEARCH );
      }
    } );

    mMainPane.activeTextEditorProperty().addListener(
        ( c, o, n ) -> mSearchModel.search( getActiveTextEditor().getText() )
    );
  }

  public void file‿new() {
    getMainPane().newTextEditor();
  }

  public void file‿open() {
    getMainPane().open( createFileChooser().openFiles() );
  }

  public void file‿close() {
    getMainPane().close();
  }

  public void file‿close_all() {
    getMainPane().closeAll();
  }

  public void file‿save() {
    getMainPane().save();
  }

  public void file‿save_as() {
    final var file = createFileChooser().saveAs();
    file.ifPresent( ( f ) -> getMainPane().saveAs( f ) );
  }

  public void file‿save_all() {
    getMainPane().saveAll();
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
    final var editor = getActiveTextEditor();
    final var context = createProcessorContext( editor );
    final var chain = createProcessors( context );
    final var doc = editor.getText();
    final var export = chain.apply( doc );
    final var filename = format.toExportFilename( editor.getPath() );
    final var chooser = new FileChooserCommand( getWindow() );
    final var file = chooser.exportAs( new File( filename ) );

    file.ifPresent( ( f ) -> {
      try {
        writeString( f.toPath(), export, UTF_8 );
        final var m = get( "Main.status.export.success", f.toString() );
        clue( m );
      } catch( final Exception e ) {
        clue( e );
      }
    } );
  }

  private ProcessorContext createProcessorContext( final TextEditor editor ) {
    return getMainPane().createProcessorContext( editor );
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
    final var nodes = getStatusBar().getLeftItems();

    if( nodes.isEmpty() ) {
      final var searchBar = new SearchBar();

      searchBar.matchIndexProperty().bind( mSearchModel.matchIndexProperty() );
      searchBar.matchCountProperty().bind( mSearchModel.matchCountProperty() );

      searchBar.setOnCancelAction( ( event ) -> {
        final var editor = getActiveTextEditor();
        nodes.remove( searchBar );
        editor.unstylize( STYLE_SEARCH );
        editor.getNode().requestFocus();
      } );

      searchBar.addInputListener( ( c, o, n ) -> {
        if( n != null && !n.isEmpty() ) {
          mSearchModel.search( n, getActiveTextEditor().getText() );
        }
      } );

      searchBar.setOnNextAction( ( event ) -> edit‿find_next() );
      searchBar.setOnPrevAction( ( event ) -> edit‿find_prev() );

      nodes.add( searchBar );
      searchBar.requestFocus();
    }
    else {
      nodes.clear();
    }
  }

  public void edit‿find_next() {
    mSearchModel.advance();
  }

  public void edit‿find_prev() {
    mSearchModel.retreat();
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
    createMarkdownDialog().insertLink(getActiveTextEditor().getTextArea());
  }

  public void insert‿image() {
    createMarkdownDialog().insertImage(getActiveTextEditor().getTextArea());
  }

  private MarkdownCommands createMarkdownDialog() {
    return new MarkdownCommands( getWindow(), getActiveTextEditor().getPath() );
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
    getActiveTextDefinition().createDefinition();
  }

  public void definition‿rename() {
    getActiveTextDefinition().renameDefinition();
  }

  public void definition‿delete() {
    getActiveTextDefinition().deleteDefinitions();
  }

  public void definition‿autoinsert() {
    getMainPane().autoinsert();
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

  private MainPane getMainPane() {
    return mMainPane;
  }

  private TextEditor getActiveTextEditor() {
    return getMainPane().getActiveTextEditor();
  }

  private TextDefinition getActiveTextDefinition() {
    return getMainPane().getActiveTextDefinition();
  }

  private Window getWindow() {
    return getMainPane().getWindow();
  }
}
