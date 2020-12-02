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

import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.keenwrite.definition.DefinitionEditor;
import com.keenwrite.editors.DefinitionNameInjector;
import com.keenwrite.editors.markdown.MarkdownEditorPane;
import com.keenwrite.preferences.UserPreferences;
import com.keenwrite.preferences.UserPreferencesView;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.preview.OutputTabPane;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorFactory;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.service.Options;
import com.keenwrite.service.Snitch;
import com.keenwrite.ui.actions.Action;
import com.keenwrite.ui.actions.MenuAction;
import com.keenwrite.ui.actions.SeparatorAction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.prefs.Preferences;

import static com.keenwrite.Bootstrap.APP_TITLE;
import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.ui.actions.ApplicationMenuBar.createMenu;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
import static javafx.application.Platform.runLater;
import static javafx.event.Event.fireEvent;
import static javafx.geometry.Pos.BASELINE_CENTER;
import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @deprecated Use {@link MainView}.
 */
@Deprecated
public class MainWindow implements Observer {

  /**
   * This variable must be declared before all other variables to prevent
   * subsequent initializations from failing due to missing user preferences.
   */
  private static final Options sOptions = Services.load( Options.class );
  private static final Snitch sSnitch = Services.load( Snitch.class );

  private final Scene mScene;
  private final Text mLineNumberText;
  private final TextField mFindTextField;

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<FileEditorController, Processor<String>> mProcessors =
      new HashMap<>();

  private final Map<String, String> mResolvedMap =
      new HashMap<>( DEFAULT_MAP_SIZE );

  private final EventHandler<PreferencesFxEvent> mRPreferencesListener =
      event -> rerender();

  private final ChangeListener<Integer> mCaretPositionListener =
      ( observable, oldPosition, newPosition ) -> processActiveTab();

  private final DefinitionEditor mDefinitionPane = createDefinitionPane();
  private final OutputTabPane mOutputPane = createOutputTabPane();
  private final FileEditorTabPane mFileEditorPane = new FileEditorTabPane(
      mCaretPositionListener );

  /**
   * Listens on the definition pane for double-click events.
   */
  private final DefinitionNameInjector mDefinitionNameInjector
      = new DefinitionNameInjector( mDefinitionPane );

  public MainWindow() {
    mLineNumberText = createLineNumberText();
    mFindTextField = createFindTextField();
    mScene = createScene();

    // Add the close request listener before the window is shown.
    initLayout();
  }

  /**
   * Called after the stage is shown.
   */
  public void init() {
    initFindInput();
    initSnitch();

    initPreferences();
    initVariableNameInjector();
  }

  private void initLayout() {
    final var scene = getScene();
    final var stylesheets = scene.getStylesheets();

    stylesheets.add( STYLESHEET_SCENE );
    scene.windowProperty().addListener(
        ( __, oldWindow, newWindow ) ->
            newWindow.setOnCloseRequest(
                e -> {
                  if( !getFileEditorPane().closeAllEditors() ) {
                    e.consume();
                  }
                }
            )
    );
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

  /**
   * Watch for changes to external files. In particular, this awaits
   * modifications to any XSL files associated with XML files being edited.
   * When
   * an XSL file is modified (external to the application), the snitch's ears
   * perk up and the file is reloaded. This keeps the XSL transformation up to
   * date with what's on the file system.
   */
  private void initSnitch() {
    sSnitch.addObserver( this );
  }

  /**
   * Re-instantiates all processors then re-renders the active tab. This
   * will refresh the resolved map, force R to re-initialize, and brute-force
   * XSLT file reloads.
   */
  private void rerender() {
    runLater(
        () -> {
          resetProcessors();
          processActiveTab();
        }
    );
  }

  /**
   * Reloads the preferences from the previous session.
   */
  private void initPreferences() {
    getFileEditorPane().initPreferences();
    getUserPreferencesView().addSaveEventHandler( mRPreferencesListener );
  }

  private void initVariableNameInjector() {
    updateVariableNameInjector( getActiveFileEditorTab() );
  }

  private void scrollToCaret() {
    getHtmlPreview().scrollTo( CARET_ID );
  }

  private void updateVariableNameInjector( final FileEditorController tab ) {
    getDefinitionNameInjector().addListener( tab );
  }

  /**
   * Called to update the status bar's caret position when a new tab is added
   * or the active tab is switched.
   *
   * @param tab The active tab containing a caret position to show.
   */
  private void updateCaretStatus( final FileEditorController tab ) {
    getLineNumberText().setText( tab.getCaretPosition().toString() );
  }

  /**
   * Called whenever the preview pane becomes out of sync with the file editor
   * tab. This can be called when the text changes, the caret paragraph
   * changes, or the file tab changes.
   *
   * @param tab The file editor tab that has been changed in some fashion.
   */
  private void process( final FileEditorController tab ) {
    if( tab != null ) {
      getHtmlPreview().setBaseUri( tab.getPath() );

      final Processor<String> processor = getProcessors().computeIfAbsent(
          tab, p -> createProcessors( tab )
      );

      try {
        updateCaretStatus( tab );
        processor.apply( tab.getEditorText() );
        scrollToCaret();
      } catch( final Exception ex ) {
        clue( ex );
      }
    }
  }

  private void processActiveTab() {
    process( getActiveFileEditorTab() );
  }

  //---- File actions -------------------------------------------------------

  /**
   * Called when an {@link Observable} instance has changed. This is called
   * by both the {@link Snitch} service and the notify service. The @link
   * Snitch} service can be called for different file types.
   *
   * @param observable The observed instance.
   * @param value      The noteworthy item.
   */
  @Override
  public void update( final Observable observable, final Object value ) {
    if( value instanceof Path && observable instanceof Snitch ) {
      updateSelectedTab();
    }
  }

  /**
   * Called when a file has been modified.
   */
  private void updateSelectedTab() {
    rerender();
  }

  /**
   * After resetting the processors, they will refresh anew to be up-to-date
   * with the files (text and definition) currently loaded into the editor.
   */
  private void resetProcessors() {
    getProcessors().clear();
  }

  //---- File actions -------------------------------------------------------

  private void fileClose() {
    // TODO: FIXME REFACTOR TABS
//    getFileEditorPane().closeEditor( getActiveFileEditorTab(), true );
  }

  /**
   * TODO: Upon closing, first remove the tab change listeners. (There's no
   * need to re-render each tab when all are being closed.)
   */
  private void fileCloseAll() {
    getFileEditorPane().closeAllEditors();
  }

  /**
   * Exports the contents of the current tab according to the given
   * {@link ExportFormat}.
   *
   * @param format Configures the {@link MarkdownProcessor} when exporting.
   */
  private void fileExport( final ExportFormat format ) {
    final var tab = getActiveFileEditorTab();
    final var context = createProcessorContext( tab, format );
    final var chain = ProcessorFactory.createProcessors( context );
    final var doc = tab.getEditorText();
    final var export = chain.apply( doc );

    final var filename = format.toExportFilename( tab.getPath().toFile() );
    final var dir = getPreferences().get( "lastDirectory", null );
    final var lastDir = new File( dir == null ? "." : dir );

    final FileChooser chooser = new FileChooser();
    chooser.setTitle( get( "Dialog.file.choose.export.title" ) );
    chooser.setInitialFileName( filename.getName() );
    chooser.setInitialDirectory( lastDir );

    final File file = chooser.showSaveDialog( getWindow() );

    if( file != null ) {
      try {
        writeString( file.toPath(), export, UTF_8 );
        final var m = get( "Main.status.export.success", file.toString() );
        clue( m );
      } catch( final IOException e ) {
        clue( e );
      }
    }
  }

  private void fileExit() {
    final Window window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
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

  //---- Insert actions -----------------------------------------------------

  /**
   * Delegates to the active editor to handle wrapping the current text
   * selection with leading and trailing strings.
   *
   * @param leading  The string to put before the selection.
   * @param trailing The string to put after the selection.
   */
  private void insertMarkdown(
      final String leading, final String trailing ) {
    getActiveEditorPane().surroundSelection( leading, trailing );
  }

  private void insertMarkdown(
      final String leading, final String trailing, final String hint ) {
    getActiveEditorPane().surroundSelection( leading, trailing, hint );
  }

  //---- Help actions -------------------------------------------------------

  private void helpAbout() {
    final Alert alert = new Alert( INFORMATION );
    alert.setTitle( get( "Dialog.about.title", APP_TITLE ) );
    alert.setHeaderText( get( "Dialog.about.header", APP_TITLE ) );
    alert.setContentText( get( "Dialog.about.content" ) );
    alert.setGraphic( new ImageView( ICON_DIALOG ) );
    alert.initOwner( getWindow() );

    alert.showAndWait();
  }

  //---- Member creators ----------------------------------------------------

  /**
   * Creates processors suited to parsing and rendering different file types.
   *
   * @param tab The tab that is subjected to processing.
   * @return A processor suited to the file type specified by the tab's path.
   */
  private Processor<String> createProcessors( final FileEditorController tab ) {
    final var context = createProcessorContext( tab );
    return ProcessorFactory.createProcessors( context );
  }

  private ProcessorContext createProcessorContext(
      final FileEditorController tab, final ExportFormat format ) {
    final var preview = getHtmlPreview();
    final var map = getResolvedMap();
    return new ProcessorContext( preview, map, tab, format );
  }

  private ProcessorContext createProcessorContext(
      final FileEditorController tab ) {
    return createProcessorContext( tab, NONE );
  }

  private DefinitionEditor createDefinitionPane() {
    return null;//new DefinitionEditor();
  }

  private OutputTabPane createOutputTabPane() {
    return new OutputTabPane();
  }

  private TextField createFindTextField() {
    return new TextField();
  }

  private Scene createScene() {
    final var splitPane = new SplitPane(
        getDefinitionPane(),
        getFileEditorPane(),
        getOutputPane() );

    getDefinitionPane().prefHeightProperty()
                       .bind( splitPane.heightProperty() );

    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1280, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setBottom( getStatusBar() );
    borderPane.setCenter( splitPane );

    final VBox statusBar = new VBox();
    statusBar.setAlignment( BASELINE_CENTER );
    statusBar.getChildren().add( getLineNumberText() );
    getStatusBar().getRightItems().add( statusBar );

    // Force preview pane refresh on Windows.
//    if( IS_OS_WINDOWS ) {
//      splitPane.getDividers().get( 1 ).positionProperty().addListener(
//          ( l, oValue, nValue ) -> runLater(
//              () -> getHtmlPreview().repaintScrollPane()
//          )
//      );
//    }

    return new Scene( borderPane );
  }

  private Text createLineNumberText() {
    return new Text( get( STATUS_BAR_LINE, 1, 1, 1 ) );
  }

  private Node createMenuBar() {
    final Action fileCloseAction = Action
        .builder()
        .setText( "Main.menu.file.close" )
        .setAccelerator( "Shortcut+W" )
        .setHandler( e -> fileClose() )
        .build();
    final Action fileCloseAllAction = Action
        .builder()
        .setText( "Main.menu.file.close_all" )
        .setHandler( e -> fileCloseAll() )
        .build();
    final Action fileExportAction = Action
        .builder()
        .setText( "Main.menu.file.export" )
        .build();
    final Action fileExportHtmlSvgAction = Action
        .builder()
        .setText( "Main.menu.file.export.html_svg" )
        .setHandler( e -> fileExport( HTML_TEX_SVG ) )
        .build();
    final Action fileExportHtmlTexAction = Action
        .builder()
        .setText( "Main.menu.file.export.html_tex" )
        .setHandler( e -> fileExport( HTML_TEX_DELIMITED ) )
        .build();
    final Action fileExportMarkdownAction = Action
        .builder()
        .setText( "Main.menu.file.export.markdown" )
        .setHandler( e -> fileExport( MARKDOWN_PLAIN ) )
        .build();
    fileExportAction.addSubActions(
        fileExportHtmlSvgAction,
        fileExportHtmlTexAction,
        fileExportMarkdownAction );

    final Action fileExitAction = Action
        .builder()
        .setText( "Main.menu.file.exit" )
        .setHandler( e -> fileExit() )
        .build();

    // Edit actions
    final Action editUndoAction = Action
        .builder()
        .setText( "Main.menu.edit.undo" )
        .setAccelerator( "Shortcut+Z" )
        .setIcon( UNDO )
        .setHandler( e -> getActiveEditorPane().undo() )
        .build();
    final Action editRedoAction = Action
        .builder()
        .setText( "Main.menu.edit.redo" )
        .setAccelerator( "Shortcut+Y" )
        .setIcon( REPEAT )
        .setHandler( e -> getActiveEditorPane().redo() )
        .build();

    final Action editCutAction = Action
        .builder()
        .setText( "Main.menu.edit.cut" )
        .setAccelerator( "Shortcut+X" )
        .setIcon( CUT )
        .setHandler( e -> getActiveEditorPane().cut() )
        .build();
    final Action editCopyAction = Action
        .builder()
        .setText( "Main.menu.edit.copy" )
        .setAccelerator( "Shortcut+C" )
        .setIcon( COPY )
        .setHandler( e -> getActiveEditorPane().copy() )
        .build();
    final Action editPasteAction = Action
        .builder()
        .setText( "Main.menu.edit.paste" )
        .setAccelerator( "Shortcut+V" )
        .setIcon( PASTE )
        .setHandler( e -> getActiveEditorPane().paste() )
        .build();
    final Action editSelectAllAction = Action
        .builder()
        .setText( "Main.menu.edit.selectAll" )
        .setAccelerator( "Shortcut+A" )
        .setHandler( e -> getActiveEditorPane().selectAll() )
        .build();

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

    // Format actions
    final Action formatBoldAction = Action
        .builder()
        .setText( "Main.menu.format.bold" )
        .setAccelerator( "Shortcut+B" )
        .setIcon( BOLD )
        .setHandler( e -> insertMarkdown( "**", "**" ) )
        .build();
    final Action formatItalicAction = Action
        .builder()
        .setText( "Main.menu.format.italic" )
        .setAccelerator( "Shortcut+I" )
        .setIcon( ITALIC )
        .setHandler( e -> insertMarkdown( "*", "*" ) )
        .build();
    final Action formatSuperscriptAction = Action
        .builder()
        .setText( "Main.menu.format.superscript" )
        .setAccelerator( "Shortcut+[" )
        .setIcon( SUPERSCRIPT )
        .setHandler( e -> insertMarkdown( "^", "^" ) )
        .build();
    final Action formatSubscriptAction = Action
        .builder()
        .setText( "Main.menu.format.subscript" )
        .setAccelerator( "Shortcut+]" )
        .setIcon( SUBSCRIPT )
        .setHandler( e -> insertMarkdown( "~", "~" ) )
        .build();
    final Action formatStrikethroughAction = Action
        .builder()
        .setText( "Main.menu.format.strikethrough" )
        .setAccelerator( "Shortcut+T" )
        .setIcon( STRIKETHROUGH )
        .setHandler( e -> insertMarkdown( "~~", "~~" ) )
        .build();

    // Insert actions
    final Action insertBlockquoteAction = Action
        .builder()
        .setText( "Main.menu.insert.blockquote" )
        .setAccelerator( "Ctrl+Q" )
        .setIcon( QUOTE_LEFT )
        .setHandler( e -> insertMarkdown( "\n\n> ", "" ) )
        .build();
    final Action insertCodeAction = Action
        .builder()
        .setText( "Main.menu.insert.code" )
        .setAccelerator( "Shortcut+K" )
        .setIcon( CODE )
        .setHandler( e -> insertMarkdown( "`", "`" ) )
        .build();
    final Action insertFencedCodeBlockAction = Action
        .builder()
        .setText( "Main.menu.insert.fenced_code_block" )
        .setAccelerator( "Shortcut+Shift+K" )
        .setIcon( FILE_CODE_ALT )
        .setHandler( e -> insertMarkdown(
            "\n\n```\n",
            "\n```\n\n",
            get( "Main.menu.insert.fenced_code_block.prompt" ) ) )
        .build();
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

    // Number of heading actions (H1 ... H3)
    final int HEADINGS = 3;
    final Action[] headings = new Action[ HEADINGS ];

    for( int i = 1; i <= HEADINGS; i++ ) {
      final String hashes = new String( new char[ i ] ).replace( "\0", "#" );
      final String markup = String.format( "%n%n%s ", hashes );
      final String text = "Main.menu.insert.heading_" + i;
      final String accelerator = "Shortcut+" + i;
      final String prompt = text + ".prompt";

      headings[ i - 1 ] = Action
          .builder()
          .setText( text )
          .setAccelerator( accelerator )
          .setIcon( HEADER )
          .setHandler( e -> insertMarkdown( markup, "", get( prompt ) ) )
          .build();
    }

    final Action insertUnorderedListAction = Action
        .builder()
        .setText( "Main.menu.insert.unordered_list" )
        .setAccelerator( "Shortcut+U" )
        .setIcon( LIST_UL )
        .setHandler( e -> insertMarkdown( "\n\n* ", "" ) )
        .build();
    final Action insertOrderedListAction = Action
        .builder()
        .setText( "Main.menu.insert.ordered_list" )
        .setAccelerator( "Shortcut+Shift+O" )
        .setIcon( LIST_OL )
        .setHandler( e -> insertMarkdown(
            "\n\n1. ", "" ) )
        .build();
    final Action insertHorizontalRuleAction = Action
        .builder()
        .setText( "Main.menu.insert.horizontal_rule" )
        .setAccelerator( "Shortcut+H" )
        .setHandler( e -> insertMarkdown(
            "\n\n---\n\n", "" ) )
        .build();

    // Definition actions
    final Action definitionCreateAction = Action
        .builder()
        .setText( "Main.menu.definition.create" )
        .setIcon( TREE )
        .setHandler( e -> getDefinitionPane().addItem() )
        .build();
    final Action definitionInsertAction = Action
        .builder()
        .setText( "Main.menu.definition.insert" )
        .setAccelerator( "Ctrl+Space" )
        .setIcon( STAR )
        .setHandler( e -> definitionInsert() )
        .build();

    // Help actions
    final Action helpAboutAction = Action
        .builder()
        .setText( "Main.menu.help.about" )
        .setHandler( e -> helpAbout() )
        .build();

    final MenuAction SEPARATOR_ACTION = new SeparatorAction();

    //---- MenuBar ----

    // File Menu
    final var fileMenu = createMenu(
        get( "Main.menu.file" ),
        SEPARATOR_ACTION,
        fileCloseAction,
        fileCloseAllAction,
        SEPARATOR_ACTION,
        fileExportAction,
        SEPARATOR_ACTION,
        fileExitAction );

    // Edit Menu
    final var editMenu = createMenu(
        get( "Main.menu.edit" ),
        SEPARATOR_ACTION,
        editUndoAction,
        editRedoAction,
        SEPARATOR_ACTION,
        editCutAction,
        editCopyAction,
        editPasteAction,
        editSelectAllAction,
        SEPARATOR_ACTION,
        editFindAction,
        editFindNextAction,
        SEPARATOR_ACTION,
        editPreferencesAction );

    // Format Menu
    final var formatMenu = createMenu(
        get( "Main.menu.format" ),
        formatBoldAction,
        formatItalicAction,
        formatSuperscriptAction,
        formatSubscriptAction,
        formatStrikethroughAction
    );

    // Insert Menu
    final var insertMenu = createMenu(
        get( "Main.menu.insert" ),
        insertBlockquoteAction,
        insertCodeAction,
        insertFencedCodeBlockAction,
        SEPARATOR_ACTION,
        insertLinkAction,
        insertImageAction,
        SEPARATOR_ACTION,
        headings[ 0 ],
        headings[ 1 ],
        headings[ 2 ],
        SEPARATOR_ACTION,
        insertUnorderedListAction,
        insertOrderedListAction,
        insertHorizontalRuleAction
    );

    // Definition Menu
    final var definitionMenu = createMenu(
        get( "Main.menu.definition" ),
        definitionCreateAction,
        definitionInsertAction );

    // Help Menu
    final var helpMenu = createMenu(
        get( "Main.menu.help" ),
        helpAboutAction );

    //---- MenuBar ----
    final var menuBar = new MenuBar(
        fileMenu,
        editMenu,
        formatMenu,
        insertMenu,
        definitionMenu,
        helpMenu );

    return new VBox( menuBar );
  }

  /**
   * Performs the autoinsert function on the active file editor.
   */
  private void definitionInsert() {
    getDefinitionNameInjector().autoinsert();
  }

  /**
   * Creates a boolean property that is bound to another boolean value of the
   * active editor.
   */
  private BooleanProperty createActiveBooleanProperty(
      final Function<FileEditorController, ObservableBooleanValue> func ) {

    final BooleanProperty b = new SimpleBooleanProperty();
    final FileEditorController tab = getActiveFileEditorTab();

    if( tab != null ) {
      b.bind( func.apply( tab ) );
    }

    getFileEditorPane().activeFileEditorProperty().addListener(
        ( __, oldFileEditor, newFileEditor ) -> {
          b.unbind();

          if( newFileEditor == null ) {
            b.set( false );
          }
          else {
            b.bind( func.apply( newFileEditor ) );
          }
        }
    );

    return b;
  }

  //---- Convenience accessors ----------------------------------------------

  private Preferences getPreferences() {
    return sOptions.getState();
  }

  private float getFloat( final String key, final float defaultValue ) {
    return getPreferences().getFloat( key, defaultValue );
  }

  public Window getWindow() {
    return getScene().getWindow();
  }

  private MarkdownEditorPane getActiveEditorPane() {
    return getActiveFileEditorTab().getEditorPane();
  }

  private FileEditorController getActiveFileEditorTab() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------

  protected Scene getScene() {
    return mScene;
  }

  private Map<FileEditorController, Processor<String>> getProcessors() {
    return mProcessors;
  }

  private FileEditorTabPane getFileEditorPane() {
    return mFileEditorPane;
  }

  private OutputTabPane getOutputPane() {
    return mOutputPane;
  }

  private HtmlPreview getHtmlPreview() {
    return getOutputPane().getHtmlPreview();
  }

  private DefinitionEditor getDefinitionPane() {
    return mDefinitionPane;
  }

  private Text getLineNumberText() {
    return mLineNumberText;
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

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return mResolvedMap;
  }

  //---- Persistence accessors ----------------------------------------------

  private UserPreferences getUserPreferences() {
    return UserPreferences.getInstance();
  }

  private UserPreferencesView getUserPreferencesView() {
    return UserPreferencesView.getInstance();
  }

  private Path getDefinitionPath() {
    return getUserPreferences().getDefinitionPath();
  }

}
