/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.actions;

import com.keenwrite.ExportFormat;
import com.keenwrite.MainPane;
import com.keenwrite.MainScene;
import com.keenwrite.commands.ConcatenateCommand;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.markdown.HyperlinkModel;
import com.keenwrite.editors.markdown.LinkVisitor;
import com.keenwrite.events.CaretMovedEvent;
import com.keenwrite.events.ExportFailedEvent;
import com.keenwrite.io.SysFile;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.PreferencesController;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.search.SearchModel;
import com.keenwrite.typesetting.Typesetter;
import com.keenwrite.ui.controls.SearchBar;
import com.keenwrite.ui.dialogs.*;
import com.keenwrite.ui.explorer.FilePicker;
import com.keenwrite.ui.explorer.FilePickerFactory;
import com.keenwrite.ui.logging.LogView;
import com.vladsch.flexmark.ast.Link;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.keenwrite.Bootstrap.*;
import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.PDF_DEFAULT;
import static com.keenwrite.constants.Constants.USER_DIRECTORY;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG_NODE;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.*;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static com.keenwrite.ui.explorer.FilePickerFactory.SelectionType;
import static com.keenwrite.ui.explorer.FilePickerFactory.SelectionType.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
import static javafx.application.Platform.runLater;
import static javafx.event.Event.fireEvent;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Responsible for abstracting how functionality is mapped to the application.
 * This allows users to customize accelerator keys and will provide pluggable
 * functionality so that different text markup languages can change documents
 * using their respective syntax.
 */
public final class GuiCommands {
  private static final String STYLE_SEARCH = "search";

  /**
   * When an action is executed, this is one of the recipients.
   */
  private final MainPane mMainPane;

  private final MainScene mMainScene;

  private final LogView mLogView;

  /**
   * Tracks finding text in the active document.
   */
  private final SearchModel mSearchModel;

  private boolean mCanTypeset;

  /**
   * A {@link Task} can only be run once, so wrap it in a {@link Service} to
   * allow re-launching the typesetting task repeatedly.
   */
  private Service<Path> mTypesetService;

  /**
   * Prevent a race-condition between checking to see if the typesetting task
   * is running and restarting the task itself.
   */
  private final Object mMutex = new Object();

  public GuiCommands( final MainScene scene, final MainPane pane ) {
    mMainScene = scene;
    mMainPane = pane;
    mLogView = new LogView();
    mSearchModel = new SearchModel();
    mSearchModel.matchOffsetProperty().addListener( ( c, o, n ) -> {
      final var editor = getActiveTextEditor();

      // Clear highlighted areas before highlighting a new region.
      if( o != null ) {
        editor.unstylize( STYLE_SEARCH );
      }

      if( n != null ) {
        editor.moveTo( n.getStart() );
        editor.stylize( n, STYLE_SEARCH );
      }
    } );

    // When the active text editor changes ...
    mMainPane.textEditorProperty().addListener(
      ( c, o, n ) -> {
        // ... update the haystack.
        mSearchModel.search( getActiveTextEditor().getText() );

        // ... update the status bar with the current caret position.
        if( n != null ) {
          final var w = getWorkspace();
          final var recentDoc = w.fileProperty( KEY_UI_RECENT_DOCUMENT );

          // ... preserve the most recent document.
          recentDoc.setValue( n.getFile() );
          CaretMovedEvent.fire( n.getCaret() );
        }
      }
    );
  }

  public void file_new() {
    getMainPane().newTextEditor();
  }

  public void file_open() {
    pickFiles( FILE_OPEN_MULTIPLE ).ifPresent( l -> getMainPane().open( l ) );
  }

  public void file_open_url() {
    pickFile().ifPresent( l -> getMainPane().open( List.of( l ) ) );
  }

  public void file_close() {
    getMainPane().close();
  }

  public void file_close_all() {
    getMainPane().closeAll();
  }

  public void file_save() {
    getMainPane().save();
  }

  public void file_save_as() {
    pickFiles( FILE_SAVE_AS ).ifPresent( l -> getMainPane().saveAs( l ) );
  }

  public void file_save_all() {
    getMainPane().saveAll();
  }

  /**
   * Converts the actively edited file in the given file format.
   *
   * @param format The destination file format.
   */
  private void file_export( final ExportFormat format ) {
    file_export( format, false );
  }

  /**
   * Converts one or more files into the given file format. If {@code dir}
   * is set to true, this will first append all files in the same directory
   * as the actively edited file.
   *
   * @param format The destination file format.
   * @param dir    Export all files in the actively edited file's directory.
   */
  private void file_export( final ExportFormat format, final boolean dir ) {
    final var editor = getMainPane().getTextEditor();
    final var exported = getWorkspace().fileProperty( KEY_UI_RECENT_EXPORT );
    final var exportParent = exported.get().toPath().getParent();
    final var editorParent = editor.getPath().getParent();
    final var userHomeParent = USER_DIRECTORY.toPath();
    final var exportPath = exportParent != null
      ? exportParent
      : editorParent != null
      ? editorParent
      : userHomeParent;

    final var filename = format.toExportFilename( editor.getPath() );
    final var selected = PDF_DEFAULT
      .getName()
      .equals( exported.get().getName() );
    final var selection = pickFile(
      selected
        ? filename
        : exported.get(),
      exportPath,
      FILE_EXPORT
    );

    selection.ifPresent( files -> file_export( editor, format, files, dir ) );
  }

  private void file_export(
    final TextEditor editor,
    final ExportFormat format,
    final List<File> files,
    final boolean dir ) {
    editor.save();
    final var main = getMainPane();
    final var exported = getWorkspace().fileProperty( KEY_UI_RECENT_EXPORT );

    final var sourceFile = files.get( 0 );
    final var sourcePath = sourceFile.toPath();
    final var document = dir ? append( editor ) : editor.getText();
    final var context = main.createProcessorContext( sourcePath, format );

    final var service = new Service<Path>() {
      @Override
      protected Task<Path> createTask() {
        final var task = new Task<Path>() {
          @Override
          protected Path call() throws Exception {
            final var chain = createProcessors( context );
            final var export = chain.apply( document );

            // Processors can export binary files. In such cases, processors
            // return null to prevent further processing.
            return export == null
              ? null
              : writeString( sourcePath, export, UTF_8 );
          }
        };

        task.setOnSucceeded(
          e -> {
            // Remember the exported file name for next time.
            exported.setValue( sourceFile );

            final var result = task.getValue();

            // Binary formats must notify users of success independently.
            if( result != null ) {
              clue( "Main.status.export.success", result );
            }
          }
        );

        task.setOnFailed( e -> {
          final var ex = task.getException();
          clue( ex );

          if( ex instanceof TypeNotPresentException ) {
            fireExportFailedEvent();
          }
        } );

        return task;
      }
    };

    mTypesetService = service;
    typeset( service );
  }

  /**
   * @param dir {@code true} means to export all files in the active file
   *            editor's directory; {@code false} means to export only the
   *            actively edited file.
   */
  private void file_export_pdf( final boolean dir ) {
    // Don't re-validate the typesetter installation each time. If the
    // user mucks up the typesetter installation, it'll get caught the
    // next time the application is started. Don't use |= because it
    // won't short-circuit.
    mCanTypeset = mCanTypeset || Typesetter.canRun();

    if( mCanTypeset ) {
      final var workspace = getWorkspace();
      final var theme = workspace.stringProperty(
        KEY_TYPESET_CONTEXT_THEME_SELECTION
      );
      final var chapters = workspace.stringProperty(
        KEY_TYPESET_CONTEXT_CHAPTERS
      );

      final var settings = ExportSettings
        .builder()
        .with( ExportSettings.Mutator::setTheme, theme )
        .with( ExportSettings.Mutator::setChapters, chapters )
        .build();

      final var themes = workspace.getFile(
        KEY_TYPESET_CONTEXT_THEMES_PATH
      );

      // If the typesetter is installed, allow the user to select a theme. If
      // the themes aren't installed, a status message will appear.
      if( ExportDialog.choose( getWindow(), themes, settings, dir ) ) {
        file_export( APPLICATION_PDF, dir );
      }
    }
    else {
      fireExportFailedEvent();
    }
  }

  public void file_export_pdf() {
    file_export_pdf( false );
  }

  public void file_export_pdf_dir() {
    file_export_pdf( true );
  }

  public void file_export_html_dir() {
    file_export( XHTML_TEX, true );
  }

  public void file_export_repeat() {
    typeset( mTypesetService );
  }

  public void file_export_html_svg() {
    file_export( HTML_TEX_SVG );
  }

  public void file_export_html_tex() {
    file_export( HTML_TEX_DELIMITED );
  }

  public void file_export_xhtml_tex() {
    file_export( XHTML_TEX );
  }

  private void fireExportFailedEvent() {
    runLater( ExportFailedEvent::fire );
  }

  public void file_exit() {
    final var window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
  }

  public void edit_undo() {
    getActiveTextEditor().undo();
  }

  public void edit_redo() {
    getActiveTextEditor().redo();
  }

  public void edit_cut() {
    getActiveTextEditor().cut();
  }

  public void edit_copy() {
    getActiveTextEditor().copy();
  }

  public void edit_paste() {
    getActiveTextEditor().paste();
  }

  public void edit_select_all() {
    getActiveTextEditor().selectAll();
  }

  public void edit_find() {
    final var nodes = getMainScene().getStatusBar().getLeftItems();

    if( nodes.isEmpty() ) {
      final var searchBar = new SearchBar();

      searchBar.matchIndexProperty().bind( mSearchModel.matchIndexProperty() );
      searchBar.matchCountProperty().bind( mSearchModel.matchCountProperty() );

      searchBar.setOnCancelAction( event -> {
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

      searchBar.setOnNextAction( event -> edit_find_next() );
      searchBar.setOnPrevAction( event -> edit_find_prev() );

      nodes.add( searchBar );
      searchBar.requestFocus();
    }
  }

  public void edit_find_next() {
    mSearchModel.advance();
  }

  public void edit_find_prev() {
    mSearchModel.retreat();
  }

  public void edit_preferences() {
    try {
      new PreferencesController( getWorkspace() ).show();
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  public void format_bold() {
    getActiveTextEditor().bold();
  }

  public void format_italic() {
    getActiveTextEditor().italic();
  }

  public void format_monospace() {
    getActiveTextEditor().monospace();
  }

  public void format_superscript() {
    getActiveTextEditor().superscript();
  }

  public void format_subscript() {
    getActiveTextEditor().subscript();
  }

  public void format_strikethrough() {
    getActiveTextEditor().strikethrough();
  }

  public void insert_blockquote() {
    getActiveTextEditor().blockquote();
  }

  public void insert_code() {
    getActiveTextEditor().code();
  }

  public void insert_fenced_code_block() {
    getActiveTextEditor().fencedCodeBlock();
  }

  public void insert_link() {
    insertObject( createLinkDialog() );
  }

  public void insert_image() {
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
   * the Markdown AST. When a user opts to insert a hyperlink, this will populate
   * the insert hyperlink dialog with data from the document, thereby allowing a
   * user to edit an existing link.
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
      ? new HyperlinkModel( selection )
      : new HyperlinkModel( link );
  }

  public void insert_heading_1() {
    insert_heading( 1 );
  }

  public void insert_heading_2() {
    insert_heading( 2 );
  }

  public void insert_heading_3() {
    insert_heading( 3 );
  }

  private void insert_heading( final int level ) {
    getActiveTextEditor().heading( level );
  }

  public void insert_unordered_list() {
    getActiveTextEditor().unorderedList();
  }

  public void insert_ordered_list() {
    getActiveTextEditor().orderedList();
  }

  public void insert_horizontal_rule() {
    getActiveTextEditor().horizontalRule();
  }

  public void definition_create() {
    getActiveTextDefinition().createDefinition();
  }

  public void definition_rename() {
    getActiveTextDefinition().renameDefinition();
  }

  public void definition_delete() {
    getActiveTextDefinition().deleteDefinitions();
  }

  public void definition_autoinsert() {
    getMainPane().autoinsert();
  }

  public void view_refresh() {
    getMainPane().viewRefresh();
  }

  public void view_preview() {
    getMainPane().viewPreview();
  }

  public void view_outline() {
    getMainPane().viewOutline();
  }

  public void view_files() {getMainPane().viewFiles();}

  public void view_statistics() {
    getMainPane().viewStatistics();
  }

  public void view_menubar() {
    getMainScene().toggleMenuBar();
  }

  public void view_toolbar() {
    getMainScene().toggleToolBar();
  }

  public void view_statusbar() {
    getMainScene().toggleStatusBar();
  }

  public void view_log() {
    mLogView.view();
  }

  public void help_about() {
    final var alert = new Alert( INFORMATION );
    final var prefix = "Dialog.about.";
    alert.setTitle( get( prefix + "title", APP_TITLE ) );
    alert.setHeaderText( get( prefix + "header", APP_TITLE ) );
    alert.setContentText( get( prefix + "content", APP_YEAR, APP_VERSION ) );
    alert.setGraphic( ICON_DIALOG_NODE );
    alert.initOwner( getWindow() );
    alert.showAndWait();
  }

  private <T> void typeset( final Service<T> service ) {
    synchronized( mMutex ) {
      if( service != null && !service.isRunning() ) {
        service.reset();
        service.start();
      }
    }
  }

  /**
   * Concatenates all the files in the same directory as the given file into
   * a string. The extension is determined by the given file name pattern; the
   * order files are concatenated is based on their numeric sort order (this
   * avoids lexicographic sorting).
   * <p>
   * If the parent path to the file being edited in the text editor cannot
   * be found then this will return the editor's text, without iterating through
   * the parent directory. (Should never happen, but who knows?)
   * </p>
   * <p>
   * New lines are automatically appended to separate each file.
   * </p>
   *
   * @param editor The text editor containing
   * @return All files in the same directory as the file being edited
   * concatenated into a single string.
   */
  private String append( final TextEditor editor ) {
    final var pattern = editor.getPath();
    final var parent = pattern.getParent();

    // Short-circuit because nothing else can be done.
    if( parent == null ) {
      clue( "Main.status.export.concat.parent", pattern );
      return editor.getText();
    }

    final var filename = SysFile.getFileName( pattern );
    final var extension = getExtension( filename );

    if( extension.isBlank() ) {
      clue( "Main.status.export.concat.extension", filename );
      return editor.getText();
    }

    try {
      final var command = new ConcatenateCommand(
        parent, extension, getString( KEY_TYPESET_CONTEXT_CHAPTERS ) );
      return command.call();
    } catch( final Throwable t ) {
      clue( t );
      return editor.getText();
    }
  }

  private Optional<File> pickFile() {
    return new OpenUrlDialog( getWindow() ).showAndWait();
  }

  private Optional<List<File>> pickFiles( final SelectionType type ) {
    return createPicker( type ).choose();
  }

  @SuppressWarnings( "SameParameterValue" )
  private Optional<List<File>> pickFile(
    final File file,
    final Path directory,
    final SelectionType type ) {
    final var picker = createPicker( type );
    picker.setInitialFilename( file );
    picker.setInitialDirectory( directory );
    return picker.choose();
  }

  private FilePicker createPicker( final SelectionType type ) {
    final var factory = new FilePickerFactory( getWorkspace() );
    return factory.createModal( getWindow(), type );
  }

  private TextEditor getActiveTextEditor() {
    return getMainPane().getTextEditor();
  }

  private TextDefinition getActiveTextDefinition() {
    return getMainPane().getTextDefinition();
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

  @SuppressWarnings( "SameParameterValue" )
  private String getString( final Key key ) {
    return getWorkspace().getString( key );
  }

  private Window getWindow() {
    return getMainPane().getWindow();
  }
}
