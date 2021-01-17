/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.actions;

import com.keenwrite.ExportFormat;
import com.keenwrite.MainPane;
import com.keenwrite.MainScene;
import com.keenwrite.StatusNotifier;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.markdown.HyperlinkModel;
import com.keenwrite.editors.markdown.LinkVisitor;
import com.keenwrite.preferences.PreferencesController;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.search.SearchModel;
import com.keenwrite.ui.controls.SearchBar;
import com.keenwrite.ui.dialogs.ImageDialog;
import com.keenwrite.ui.dialogs.LinkDialog;
import com.vladsch.flexmark.ast.Link;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.ICON_DIALOG_NODE;
import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.StatusNotifier.getStatusBar;
import static com.keenwrite.preferences.WorkspaceKeys.KEY_UI_RECENT_DIR;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
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
@SuppressWarnings( "NonAsciiCharacters" )
public final class ApplicationActions {
  private static final String STYLE_SEARCH = "search";

  /**
   * When an action is executed, this is one of the recipients.
   */
  private final MainPane mMainPane;

  private final MainScene mMainScene;

  /**
   * Tracks finding text in the active document.
   */
  private final SearchModel mSearchModel;

  public ApplicationActions( final MainScene scene, final MainPane pane ) {
    mMainScene = scene;
    mMainPane = pane;
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

    // When the active text editor changes, update the haystack.
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
    final var main = getMainPane();
    final var context = main.createProcessorContext( format );
    final var chain = createProcessors( context );
    final var editor = main.getActiveTextEditor();
    final var doc = editor.getText();
    final var export = chain.apply( doc );
    final var filename = format.toExportFilename( editor.getPath() );
    final var chooser = createFileChooser();
    final var file = chooser.exportAs( filename );

    file.ifPresent( ( f ) -> {
      try {
        writeString( f.toPath(), export );
        clue( get( "Main.status.export.success", f.toString() ) );
      } catch( final Exception ex ) {
        clue( ex );
      }
    } );
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
    new PreferencesController( getWorkspace() ).show();
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
    insertObject( createLinkDialog() );
  }

  public void insert‿image() {
    insertObject( createImageDialog() );
  }

  private void insertObject( final Dialog<String> dialog ) {
    final var textArea = getActiveTextEditor().getTextArea();
    dialog.showAndWait().ifPresent( textArea::replaceSelection );
  }

  private Dialog<String> createLinkDialog() {
    return new LinkDialog( getWindow(), createHyperlinkModel() );
  }

  private Dialog<String> createImageDialog() {
    final var path = getActiveTextEditor().getPath();
    final var parentDir = path.getParent();
    return new ImageDialog( getWindow(), parentDir );
  }

  /**
   * Returns one of: selected text, word under cursor, or parsed hyperlink from
   * the Markdown AST.
   *
   * @return An instance containing the link URL and display text.
   */
  private HyperlinkModel createHyperlinkModel() {
    final var context = getMainPane().createProcessorContext();
    final var editor = getActiveTextEditor();
    final var textArea = editor.getTextArea();
    final var selectedText = textArea.getSelectedText();

    // Convert current paragraph to Markdown nodes.
    final var mp = MarkdownProcessor.create( context );
    final var p = textArea.getCurrentParagraph();
    final var paragraph = textArea.getText( p );
    final var node = mp.toNode( paragraph );
    final var visitor = new LinkVisitor( textArea.getCaretColumn() );
    final var link = visitor.process( node );

    if( link != null ) {
      textArea.selectRange( p, link.getStartOffset(), p, link.getEndOffset() );
    }

    return createHyperlinkModel( link, selectedText );
  }

  private HyperlinkModel createHyperlinkModel(
    final Link link, final String selection ) {

    return link == null
      ? new HyperlinkModel( selection, "https://localhost" )
      : new HyperlinkModel( link );
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
    getMainPane().viewRefresh();
  }

  public void view‿preview() {
    getMainPane().viewPreview();
  }

  public void view‿menubar() {
    getMainScene().toggleMenuBar();
  }

  public void view‿toolbar() {
    getMainScene().toggleToolBar();
  }

  public void view‿statusbar() {
    getMainScene().toggleStatusBar();
  }

  public void view‿issues() {
    StatusNotifier.viewIssues();
  }

  public void help‿about() {
    final Alert alert = new Alert( INFORMATION );
    alert.setTitle( get( "Dialog.about.title", APP_TITLE ) );
    alert.setHeaderText( get( "Dialog.about.header", APP_TITLE ) );
    alert.setContentText( get( "Dialog.about.content" ) );
    alert.setGraphic( ICON_DIALOG_NODE );
    alert.initOwner( getWindow() );
    alert.showAndWait();
  }

  private FileChooserCommand createFileChooser() {
    final var dir = getWorkspace().fileProperty( KEY_UI_RECENT_DIR );
    return new FileChooserCommand( getWindow(), dir );
  }

  private TextEditor getActiveTextEditor() {
    return getMainPane().getActiveTextEditor();
  }

  private TextDefinition getActiveTextDefinition() {
    return getMainPane().getActiveTextDefinition();
  }

  private MainScene getMainScene() {
    return mMainScene;
  }

  private MainPane getMainPane() {
    return mMainPane;
  }

  private Workspace getWorkspace() {
    return mMainPane.getWorkspace();
  }

  private Window getWindow() {
    return getMainPane().getWindow();
  }
}
