/*
 * Copyright 2016 Karl Tauber and White Magic Software, Ltd.
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
package com.scrivenvar;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;
import com.scrivenvar.definition.DefinitionFactory;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.definition.DefinitionSource;
import com.scrivenvar.definition.EmptyDefinitionSource;
import com.scrivenvar.editors.EditorPane;
import com.scrivenvar.editors.VariableNameInjector;
import com.scrivenvar.editors.markdown.MarkdownEditorPane;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.Processor;
import com.scrivenvar.processors.ProcessorFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.util.Action;
import com.scrivenvar.util.ActionUtils;
import static com.scrivenvar.util.StageState.*;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import static javafx.event.Event.fireEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javafx.scene.input.KeyCode.ESCAPE;
import javafx.scene.input.KeyEvent;
import static javafx.scene.input.KeyEvent.CHAR_UNDEFINED;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class MainWindow implements Observer {

  private final Options options = Services.load( Options.class );
  private final Snitch snitch = Services.load( Snitch.class );

  private Scene scene;
  private MenuBar menuBar;

  private DefinitionSource definitionSource;
  private DefinitionPane definitionPane;
  private FileEditorTabPane fileEditorPane;
  private HTMLPreviewPane previewPane;

  /**
   * Prevent re-instantiation processing classes.
   */
  private Map<FileEditorTab, Processor<String>> processors;
  private ProcessorFactory processorFactory;
  

  public MainWindow() {
    initLayout();
    initOpenDefinitionListener();
    initTabAddedListener();
    initTabChangedListener();
    initPreferences();
    initWatchDog();
  }

  /**
   * Listen for file editor tab pane to receive an open definition source event.
   */
  private void initOpenDefinitionListener() {
    getFileEditorPane().onOpenDefinitionFileProperty().addListener(
      (ObservableValue<? extends Path> definitionFile,
        final Path oldPath, final Path newPath) -> {
        openDefinition( newPath );
        refreshSelectedTab( getActiveFileEditor() );
      } );
  }

  /**
   * When tabs are added, hook the various change listeners onto the new tab so
   * that the preview pane refreshes as necessary.
   */
  private void initTabAddedListener() {
    final FileEditorTabPane editorPane = getFileEditorPane();

    // Make sure the text processor kicks off when new files are opened.
    final ObservableList<Tab> tabs = editorPane.getTabs();

    // Update the preview pane on tab changes.
    tabs.addListener(
      (final Change<? extends Tab> change) -> {
        while( change.next() ) {
          if( change.wasAdded() ) {
            // Multiple tabs can be added simultaneously.
            for( final Tab newTab : change.getAddedSubList() ) {
              final FileEditorTab tab = (FileEditorTab)newTab;

              initTextChangeListener( tab );
              initCaretParagraphListener( tab );
              initVariableNameInjector( tab );
            }
          }
        }
      }
    );
  }

  /**
   * Reloads the preferences from the previous load.
   */
  private void initPreferences() {
    getFileEditorPane().restorePreferences();
    restoreDefinitionSource();
  }

  /**
   * Listen for new tab selection events.
   */
  private void initTabChangedListener() {
    final FileEditorTabPane editorPane = getFileEditorPane();

    // Update the preview pane changing tabs.
    editorPane.addTabSelectionListener(
      (ObservableValue<? extends Tab> tabPane,
        final Tab oldTab, final Tab newTab) -> {

        // If there was no old tab, then this is a first time load, which
        // can be ignored.
        if( oldTab != null ) {
          if( newTab == null ) {
            closeRemainingTab();
          } else {
            // Update the preview with the edited text.
            refreshSelectedTab( (FileEditorTab)newTab );
          }
        }
      }
    );
  }

  private void initTextChangeListener( final FileEditorTab tab ) {
    tab.addTextChangeListener(
      (ObservableValue<? extends String> editor,
        final String oldValue, final String newValue) -> {
        refreshSelectedTab( tab );
      }
    );
  }

  private void initCaretParagraphListener( final FileEditorTab tab ) {
    tab.addCaretParagraphListener(
      (ObservableValue<? extends Integer> editor,
        final Integer oldValue, final Integer newValue) -> {
        refreshSelectedTab( tab );
      }
    );
  }

  private void initVariableNameInjector( final FileEditorTab tab ) {
    VariableNameInjector.listen( tab, getDefinitionPane() );
  }

  private void initWatchDog() {
    getSnitch().addObserver( this );
  }

  /**
   * Called whenever the preview pane becomes out of sync with the file editor
   * tab. This can be called when the text changes, the caret paragraph changes,
   * or the file tab changes.
   *
   * @param tab The file editor tab that has been changed in some fashion.
   */
  private void refreshSelectedTab( final FileEditorTab tab ) {
    getPreviewPane().setPath( tab.getPath() );

    Processor<String> processor = getProcessors().get( tab );

    if( processor == null ) {
      processor = createProcessor( tab );
      getProcessors().put( tab, processor );
    }

    processor.processChain( tab.getEditorText() );
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return getDefinitionSource().getResolvedMap();
  }

  /**
   * Returns the root node for the hierarchical definition source.
   *
   * @return Data to display in the definition pane.
   */
  private TreeView<String> getTreeView() {
    try {
      return getDefinitionSource().asTreeView();
    } catch( Exception e ) {
      alert( e );
    }

    return new TreeView<>();
  }

  private void openDefinition( final Path path ) {
    openDefinition( path.toString() );
  }

  private void openDefinition( final String path ) {
    try {
      final DefinitionSource ds = createDefinitionSource( path );
      setDefinitionSource( ds );
      storeDefinitionSource();

      getDefinitionPane().setRoot( ds.asTreeView() );
    } catch( Exception e ) {
      alert( e );
    }
  }

  private void restoreDefinitionSource() {
    final Preferences preferences = getPreferences();
    final String source = preferences.get( PREFS_DEFINITION_SOURCE, null );

    if( source != null ) {
      openDefinition( source );
    }
  }

  private void storeDefinitionSource() {
    final Preferences preferences = getPreferences();
    final DefinitionSource ds = getDefinitionSource();

    preferences.put( PREFS_DEFINITION_SOURCE, ds.toString() );
  }

  /**
   * Called when the last open tab is closed. This clears out the preview pane
   * and the definition pane.
   */
  private void closeRemainingTab() {
    getPreviewPane().clear();
    getDefinitionPane().clear();
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param e The exception with a message that the user should know about.
   */
  private void alert( final Exception e ) {
    // TODO: Update the status bar.
  }

  //---- File actions -------------------------------------------------------
  /**
   * Called when a file has been modified.
   *
   * @param snitch The watchdog file monitoring instance.
   * @param file The file that was modified.
   */
  @Override
  public void update( final Observable snitch, final Object file ) {
    if( file instanceof Path ) {
      update( (Path)file );
    }
  }

  /**
   * Called when a file has been modified.
   *
   * @param file Path to the modified file.
   */
  private void update( final Path file ) {
    // Avoid throwing IllegalStateException by running from a non-JavaFX thread.
    Platform.runLater(
      () -> {
        // Brute-force XSLT file reload by re-instantiating all processors.
        getProcessors().clear();
        refreshSelectedTab( getActiveFileEditor() );
      }
    );
  }

  //---- File actions -------------------------------------------------------
  private void fileNew() {
    getFileEditorPane().newEditor();
  }

  private void fileOpen() {
    getFileEditorPane().openFileDialog();
  }

  private void fileClose() {
    getFileEditorPane().closeEditor( getActiveFileEditor(), true );
  }

  private void fileCloseAll() {
    getFileEditorPane().closeAllEditors();
  }

  private void fileSave() {
    getFileEditorPane().saveEditor( getActiveFileEditor() );
  }

  private void fileSaveAll() {
    getFileEditorPane().saveAllEditors();
  }

  private void fileExit() {
    final Window window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
  }

  //---- Help actions -------------------------------------------------------
  private void helpAbout() {
    Alert alert = new Alert( AlertType.INFORMATION );
    alert.setTitle( get( "Dialog.about.title" ) );
    alert.setHeaderText( get( "Dialog.about.header" ) );
    alert.setContentText( get( "Dialog.about.content" ) );
    alert.setGraphic( new ImageView( new Image( FILE_LOGO_32 ) ) );
    alert.initOwner( getWindow() );

    alert.showAndWait();
  }

  //---- Convenience accessors ----------------------------------------------
  private float getFloat( final String key, final float defaultValue ) {
    return getPreferences().getFloat( key, defaultValue );
  }

  private Preferences getPreferences() {
    return getOptions().getState();
  }

  private Window getWindow() {
    return getScene().getWindow();
  }

  private MarkdownEditorPane getActiveEditor() {
    final EditorPane pane = getActiveFileEditor().getEditorPane();

    return pane instanceof MarkdownEditorPane ? (MarkdownEditorPane)pane : null;
  }

  private FileEditorTab getActiveFileEditor() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------
  public Scene getScene() {
    return this.scene;
  }

  private void setScene( Scene scene ) {
    this.scene = scene;
  }

  private Map<FileEditorTab, Processor<String>> getProcessors() {
    if( this.processors == null ) {
      this.processors = new HashMap<>();
    }

    return this.processors;
  }
  
  private ProcessorFactory getProcessorFactory() {
    if( this.processorFactory == null ) {
      this.processorFactory = createProcessorFactory();
    }

    return this.processorFactory;
  }

  private FileEditorTabPane getFileEditorPane() {
    if( this.fileEditorPane == null ) {
      this.fileEditorPane = createFileEditorPane();
    }

    return this.fileEditorPane;
  }

  private HTMLPreviewPane getPreviewPane() {
    if( this.previewPane == null ) {
      this.previewPane = createPreviewPane();
    }

    return this.previewPane;
  }

  private void setDefinitionSource( final DefinitionSource definitionSource ) {
    this.definitionSource = definitionSource;
  }

  private DefinitionSource getDefinitionSource() {
    if( this.definitionSource == null ) {
      this.definitionSource = new EmptyDefinitionSource();
    }

    return this.definitionSource;
  }

  private DefinitionPane getDefinitionPane() {
    if( this.definitionPane == null ) {
      this.definitionPane = createDefinitionPane();
    }

    return this.definitionPane;
  }

  private Options getOptions() {
    return this.options;
  }

  private Snitch getSnitch() {
    return this.snitch;
  }

  public MenuBar getMenuBar() {
    return this.menuBar;
  }

  public void setMenuBar( MenuBar menuBar ) {
    this.menuBar = menuBar;
  }

  //---- Member creators ----------------------------------------------------
  /**
   * Factory to create processors that are suited to different file types.
   *
   * @param tab The tab that is subjected to processing.
   *
   * @return A processor suited to the file type specified by the tab's path.
   */
  private Processor<String> createProcessor( final FileEditorTab tab ) {
    return getProcessorFactory().createProcessor( tab );
  }

  private ProcessorFactory createProcessorFactory() {
    return new ProcessorFactory( getPreviewPane(), getResolvedMap() );
  }

  private DefinitionSource createDefinitionSource( final String path )
    throws MalformedURLException {
    return createDefinitionFactory().createDefinitionSource( path );
  }

  /**
   * Create an editor pane to hold file editor tabs.
   *
   * @return A new instance, never null.
   */
  private FileEditorTabPane createFileEditorPane() {
    return new FileEditorTabPane();
  }

  private HTMLPreviewPane createPreviewPane() {
    return new HTMLPreviewPane();
  }

  private DefinitionPane createDefinitionPane() {
    return new DefinitionPane( getTreeView() );
  }

  private DefinitionFactory createDefinitionFactory() {
    return new DefinitionFactory();
  }

  private Node createMenuBar() {
    final BooleanBinding activeFileEditorIsNull = getFileEditorPane().activeFileEditorProperty().isNull();

    // File actions
    Action fileNewAction = new Action( get( "Main.menu.file.new" ), "Shortcut+N", FILE_ALT, e -> fileNew() );
    Action fileOpenAction = new Action( get( "Main.menu.file.open" ), "Shortcut+O", FOLDER_OPEN_ALT, e -> fileOpen() );
    Action fileCloseAction = new Action( get( "Main.menu.file.close" ), "Shortcut+W", null, e -> fileClose(), activeFileEditorIsNull );
    Action fileCloseAllAction = new Action( get( "Main.menu.file.close_all" ), null, null, e -> fileCloseAll(), activeFileEditorIsNull );
    Action fileSaveAction = new Action( get( "Main.menu.file.save" ), "Shortcut+S", FLOPPY_ALT, e -> fileSave(),
      createActiveBooleanProperty( FileEditorTab::modifiedProperty ).not() );
    Action fileSaveAllAction = new Action( get( "Main.menu.file.save_all" ), "Shortcut+Shift+S", null, e -> fileSaveAll(),
      Bindings.not( getFileEditorPane().anyFileEditorModifiedProperty() ) );
    Action fileExitAction = new Action( get( "Main.menu.file.exit" ), null, null, e -> fileExit() );

    // Edit actions
    Action editUndoAction = new Action( get( "Main.menu.edit.undo" ), "Shortcut+Z", UNDO,
      e -> getActiveEditor().undo(),
      createActiveBooleanProperty( FileEditorTab::canUndoProperty ).not() );
    Action editRedoAction = new Action( get( "Main.menu.edit.redo" ), "Shortcut+Y", REPEAT,
      e -> getActiveEditor().redo(),
      createActiveBooleanProperty( FileEditorTab::canRedoProperty ).not() );

    // Insert actions
    Action insertBoldAction = new Action( get( "Main.menu.insert.bold" ), "Shortcut+B", BOLD,
      e -> getActiveEditor().surroundSelection( "**", "**" ),
      activeFileEditorIsNull );
    Action insertItalicAction = new Action( get( "Main.menu.insert.italic" ), "Shortcut+I", ITALIC,
      e -> getActiveEditor().surroundSelection( "*", "*" ),
      activeFileEditorIsNull );
    Action insertStrikethroughAction = new Action( get( "Main.menu.insert.strikethrough" ), "Shortcut+T", STRIKETHROUGH,
      e -> getActiveEditor().surroundSelection( "~~", "~~" ),
      activeFileEditorIsNull );
    Action insertBlockquoteAction = new Action( get( "Main.menu.insert.blockquote" ), "Ctrl+Q", QUOTE_LEFT, // not Shortcut+Q because of conflict on Mac
      e -> getActiveEditor().surroundSelection( "\n\n> ", "" ),
      activeFileEditorIsNull );
    Action insertCodeAction = new Action( get( "Main.menu.insert.code" ), "Shortcut+K", CODE,
      e -> getActiveEditor().surroundSelection( "`", "`" ),
      activeFileEditorIsNull );
    Action insertFencedCodeBlockAction = new Action( get( "Main.menu.insert.fenced_code_block" ), "Shortcut+Shift+K", FILE_CODE_ALT,
      e -> getActiveEditor().surroundSelection( "\n\n```\n", "\n```\n\n", get( "Main.menu.insert.fenced_code_block.prompt" ) ),
      activeFileEditorIsNull );

    Action insertLinkAction = new Action( get( "Main.menu.insert.link" ), "Shortcut+L", LINK,
      e -> getActiveEditor().insertLink(),
      activeFileEditorIsNull );
    Action insertImageAction = new Action( get( "Main.menu.insert.image" ), "Shortcut+G", PICTURE_ALT,
      e -> getActiveEditor().insertImage(),
      activeFileEditorIsNull );

    final Action[] headers = new Action[ 6 ];

    // Insert header actions (H1 ... H6)
    for( int i = 1; i <= 6; i++ ) {
      final String hashes = new String( new char[ i ] ).replace( "\0", "#" );
      final String markup = String.format( "%n%n%s ", hashes );
      final String text = get( "Main.menu.insert.header_" + i );
      final String accelerator = "Shortcut+" + i;
      final String prompt = get( "Main.menu.insert.header_" + i + ".prompt" );

      headers[ i - 1 ] = new Action( text, accelerator, HEADER,
        e -> getActiveEditor().surroundSelection( markup, "", prompt ),
        activeFileEditorIsNull );
    }

    Action insertUnorderedListAction = new Action( get( "Main.menu.insert.unordered_list" ), "Shortcut+U", LIST_UL,
      e -> getActiveEditor().surroundSelection( "\n\n* ", "" ),
      activeFileEditorIsNull );
    Action insertOrderedListAction = new Action( get( "Main.menu.insert.ordered_list" ), "Shortcut+Shift+O", LIST_OL,
      e -> getActiveEditor().surroundSelection( "\n\n1. ", "" ),
      activeFileEditorIsNull );
    Action insertHorizontalRuleAction = new Action( get( "Main.menu.insert.horizontal_rule" ), "Shortcut+H", null,
      e -> getActiveEditor().surroundSelection( "\n\n---\n\n", "" ),
      activeFileEditorIsNull );

    // Help actions
    Action helpAboutAction = new Action( get( "Main.menu.help.about" ), null, null, e -> helpAbout() );

    //---- MenuBar ----
    Menu fileMenu = ActionUtils.createMenu( get( "Main.menu.file" ),
      fileNewAction,
      fileOpenAction,
      null,
      fileCloseAction,
      fileCloseAllAction,
      null,
      fileSaveAction,
      fileSaveAllAction,
      null,
      fileExitAction );

    Menu editMenu = ActionUtils.createMenu( get( "Main.menu.edit" ),
      editUndoAction,
      editRedoAction );

    Menu insertMenu = ActionUtils.createMenu( get( "Main.menu.insert" ),
      insertBoldAction,
      insertItalicAction,
      insertStrikethroughAction,
      insertBlockquoteAction,
      insertCodeAction,
      insertFencedCodeBlockAction,
      null,
      insertLinkAction,
      insertImageAction,
      null,
      headers[ 0 ],
      headers[ 1 ],
      headers[ 2 ],
      headers[ 3 ],
      headers[ 4 ],
      headers[ 5 ],
      null,
      insertUnorderedListAction,
      insertOrderedListAction,
      insertHorizontalRuleAction );

    Menu helpMenu = ActionUtils.createMenu( get( "Main.menu.help" ),
      helpAboutAction );

    menuBar = new MenuBar( fileMenu, editMenu, insertMenu, helpMenu );

    //---- ToolBar ----
    ToolBar toolBar = ActionUtils.createToolBar(
      fileNewAction,
      fileOpenAction,
      fileSaveAction,
      null,
      editUndoAction,
      editRedoAction,
      null,
      insertBoldAction,
      insertItalicAction,
      insertBlockquoteAction,
      insertCodeAction,
      insertFencedCodeBlockAction,
      null,
      insertLinkAction,
      insertImageAction,
      null,
      headers[ 0 ],
      null,
      insertUnorderedListAction,
      insertOrderedListAction );

    return new VBox( menuBar, toolBar );
  }

  /**
   * Creates a boolean property that is bound to another boolean value of the
   * active editor.
   */
  private BooleanProperty createActiveBooleanProperty(
    final Function<FileEditorTab, ObservableBooleanValue> func ) {

    final BooleanProperty b = new SimpleBooleanProperty();
    final FileEditorTab tab = getActiveFileEditor();

    if( tab != null ) {
      b.bind( func.apply( tab ) );
    }

    getFileEditorPane().activeFileEditorProperty().addListener(
      (observable, oldFileEditor, newFileEditor) -> {
        b.unbind();

        if( newFileEditor != null ) {
          b.bind( func.apply( newFileEditor ) );
        } else {
          b.set( false );
        }
      }
    );

    return b;
  }

  private void initLayout() {
    final SplitPane splitPane = new SplitPane(
      getDefinitionPane().getNode(),
      getFileEditorPane().getNode(),
      getPreviewPane().getNode() );

    splitPane.setDividerPositions(
      getFloat( K_PANE_SPLIT_DEFINITION, .10f ),
      getFloat( K_PANE_SPLIT_EDITOR, .45f ),
      getFloat( K_PANE_SPLIT_PREVIEW, .45f ) );

    // See: http://broadlyapplicable.blogspot.ca/2015/03/javafx-capture-restorePreferences-splitpane.html
    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1024, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setCenter( splitPane );

    final Scene appScene = new Scene( borderPane );
    setScene( appScene );
    appScene.getStylesheets().add( STYLESHEET_SCENE );
    appScene.windowProperty().addListener(
      (observable, oldWindow, newWindow) -> {
        newWindow.setOnCloseRequest( e -> {
          if( !getFileEditorPane().closeAllEditors() ) {
            e.consume();
          }
        } );

        // Workaround JavaFX bug: deselect menubar if window loses focus.
        newWindow.focusedProperty().addListener(
          (obs, oldFocused, newFocused) -> {
            if( !newFocused ) {
              // Send an ESC key event to the menubar
              this.menuBar.fireEvent(
                new KeyEvent(
                  KEY_PRESSED, CHAR_UNDEFINED, "", ESCAPE,
                  false, false, false, false ) );
            }
          }
        );
      }
    );
  }
}
