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

import static com.scrivenvar.Constants.LOGO_32;
import static com.scrivenvar.Messages.get;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.editor.EditorPane;
import com.scrivenvar.editor.MarkdownEditorPane;
import com.scrivenvar.editor.VariableNameInjector;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.HTMLPreviewProcessor;
import com.scrivenvar.processors.MarkdownCaretInsertionProcessor;
import com.scrivenvar.processors.MarkdownCaretReplacementProcessor;
import com.scrivenvar.processors.MarkdownProcessor;
import com.scrivenvar.processors.Processor;
import com.scrivenvar.processors.TextChangeProcessor;
import com.scrivenvar.processors.VariableProcessor;
import com.scrivenvar.service.Options;
import com.scrivenvar.util.Action;
import com.scrivenvar.util.ActionUtils;
import static com.scrivenvar.util.StageState.K_PANE_SPLIT_DEFINITION;
import static com.scrivenvar.util.StageState.K_PANE_SPLIT_EDITOR;
import com.scrivenvar.yaml.YamlParser;
import com.scrivenvar.yaml.YamlTreeAdapter;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.BOLD;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.CODE;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.FILE_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.FILE_CODE_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.FLOPPY_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.FOLDER_OPEN_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.HEADER;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.ITALIC;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LINK;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LIST_OL;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.LIST_UL;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.PICTURE_ALT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.QUOTE_LEFT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.REPEAT;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.STRIKETHROUGH;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.UNDO;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.prefs.Preferences;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
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
import org.fxmisc.richtext.StyleClassedTextArea;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.get;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class MainWindow {

  private final Options options = Services.load( Options.class );

  private Scene scene;

  private TreeView<String> treeView;
  private FileEditorTabPane fileEditorPane;
  private DefinitionPane definitionPane;

  private VariableNameInjector variableNameInjector;

  private YamlTreeAdapter yamlTreeAdapter;
  private YamlParser yamlParser;

  private MenuBar menuBar;

  public MainWindow() {
    initLayout();
    initVariableNameInjector();
  }

  private void initLayout() {
    final SplitPane splitPane = new SplitPane(
      getDefinitionPane().getNode(),
      getFileEditorPane().getNode() );

    splitPane.setDividerPositions(
      getFloat( K_PANE_SPLIT_DEFINITION, .05f ),
      getFloat( K_PANE_SPLIT_EDITOR, .95f ) );

    // See: http://broadlyapplicable.blogspot.ca/2015/03/javafx-capture-restorePreferences-splitpane.html
    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1024, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setCenter( splitPane );
    
    final Scene appScene = new Scene( borderPane );
    setScene( appScene );
    appScene.getStylesheets().add( Constants.STYLESHEET_PREVIEW );
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
          } );
      } );
  }

  private void initVariableNameInjector() {
    setVariableNameInjector( new VariableNameInjector(
      getFileEditorPane(),
      getDefinitionPane() )
    );
  }

  private Window getWindow() {
    return getScene().getWindow();
  }

  public Scene getScene() {
    return this.scene;
  }

  private void setScene( Scene scene ) {
    this.scene = scene;
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
      } );

    return b;
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
    Event.fireEvent( window,
      new WindowEvent( window, WindowEvent.WINDOW_CLOSE_REQUEST ) );
  }

  //---- Help actions -------------------------------------------------------
  private void helpAbout() {
    Alert alert = new Alert( AlertType.INFORMATION );
    alert.setTitle( Messages.get( "Dialog.about.title" ) );
    alert.setHeaderText( Messages.get( "Dialog.about.header" ) );
    alert.setContentText( Messages.get( "Dialog.about.content" ) );
    alert.setGraphic( new ImageView( new Image( LOGO_32 ) ) );
    alert.initOwner( getWindow() );

    alert.showAndWait();
  }

  private FileEditorTabPane getFileEditorPane() {
    if( this.fileEditorPane == null ) {
      this.fileEditorPane = createFileEditorPane();
    }

    return this.fileEditorPane;
  }

  private FileEditorTabPane createFileEditorPane() {
    // Create an editor pane to hold file editor tabs.
    final FileEditorTabPane editorPane = new FileEditorTabPane();

    // Make sure the text processor kicks off when new files are opened.
    final ObservableList<Tab> tabs = editorPane.getTabs();

    tabs.addListener( (Change<? extends Tab> change) -> {
      while( change.next() ) {
        if( change.wasAdded() ) {
          // Multiple tabs can be added simultaneously.
          for( final Tab tab : change.getAddedSubList() ) {
            addListener( (FileEditorTab)tab );
          }
        }
      }
    } );

    // After the processors are in place, restorePreferences the previously closed
    // tabs. Adding them will trigger the change event, above.
    editorPane.restorePreferences();

    return editorPane;
  }

  private MarkdownEditorPane getActiveEditor() {
    return (MarkdownEditorPane)(getActiveFileEditor().getEditorPane());
  }

  private FileEditorTab getActiveFileEditor() {
    return getFileEditorPane().getActiveFileEditor();
  }

  /**
   * Listens for changes to tabs and their text editors.
   *
   * @see https://github.com/DaveJarvis/scrivenvar/issues/17
   * @see https://github.com/DaveJarvis/scrivenvar/issues/18
   *
   * @param tab The file editor tab that contains a text editor.
   */
  private void addListener( FileEditorTab tab ) {
    final HTMLPreviewPane previewPane = tab.getPreviewPane();
    final EditorPane editorPanel = tab.getEditorPane();
    final StyleClassedTextArea editor = editorPanel.getEditor();

    // TODO: Use a factory based on the filename extension. The default
    // extension will be for a markdown file (e.g., on file new).
    final Processor<String> hpp = new HTMLPreviewProcessor( previewPane );
    final Processor<String> mcrp = new MarkdownCaretReplacementProcessor( hpp );
    final Processor<String> mp = new MarkdownProcessor( mcrp );
    final Processor<String> mcip = new MarkdownCaretInsertionProcessor( mp, editor );
    final Processor<String> vnp = new VariableProcessor( mcip, getResolvedMap() );
    final TextChangeProcessor tp = new TextChangeProcessor( vnp );

    editorPanel.addChangeListener( tp );
    editorPanel.addCaretParagraphListener(
      (final ObservableValue<? extends Integer> observable,
        final Integer oldValue, final Integer newValue) -> {
        
        // Kick off the processing chain at the variable processor when the
        // cursor changes paragraphs. This might cause some slight duplication
        // when the Enter key is pressed.
        vnp.processChain( editor.getText() );
      } );
  }

  protected DefinitionPane createDefinitionPane() {
    return new DefinitionPane( getTreeView() );
  }

  private DefinitionPane getDefinitionPane() {
    if( this.definitionPane == null ) {
      this.definitionPane = createDefinitionPane();
    }

    return this.definitionPane;
  }

  public MenuBar getMenuBar() {
    return menuBar;
  }

  public void setMenuBar( MenuBar menuBar ) {
    this.menuBar = menuBar;
  }

  public VariableNameInjector getVariableNameInjector() {
    return this.variableNameInjector;
  }

  public void setVariableNameInjector( VariableNameInjector variableNameInjector ) {
    this.variableNameInjector = variableNameInjector;
  }

  private float getFloat( final String key, final float defaultValue ) {
    return getPreferences().getFloat( key, defaultValue );
  }

  private Preferences getPreferences() {
    return getOptions().getState();
  }

  private Options getOptions() {
    return this.options;
  }

  private synchronized TreeView<String> getTreeView() throws RuntimeException {
    if( this.treeView == null ) {
      try {
        this.treeView = createTreeView();
      } catch( IOException ex ) {

        // TODO: Pop an error message.
        throw new RuntimeException( ex );
      }
    }

    return this.treeView;
  }

  private InputStream asStream( final String resource ) {
    return getClass().getResourceAsStream( resource );
  }

  private TreeView<String> createTreeView() throws IOException {
    // TODO: Associate variable file with path to current file.
    return getYamlTreeAdapter().adapt(
      asStream( "/com/scrivenvar/variables.yaml" ),
      get( "Pane.defintion.node.root.title" )
    );
  }

  private Map<String, String> getResolvedMap() {
    return getYamlParser().createResolvedMap();
  }

  private YamlTreeAdapter getYamlTreeAdapter() {
    if( this.yamlTreeAdapter == null ) {
      setYamlTreeAdapter( new YamlTreeAdapter( getYamlParser() ) );
    }

    return this.yamlTreeAdapter;
  }

  private void setYamlTreeAdapter( final YamlTreeAdapter yamlTreeAdapter ) {
    this.yamlTreeAdapter = yamlTreeAdapter;
  }

  private YamlParser getYamlParser() {
    if( this.yamlParser == null ) {
      setYamlParser( new YamlParser() );
    }

    return this.yamlParser;
  }

  private void setYamlParser( final YamlParser yamlParser ) {
    this.yamlParser = yamlParser;
  }

  private Node createMenuBar() {
    final BooleanBinding activeFileEditorIsNull = getFileEditorPane().activeFileEditorProperty().isNull();

    // File actions
    Action fileNewAction = new Action( Messages.get( "Main.menu.file.new" ), "Shortcut+N", FILE_ALT, e -> fileNew() );
    Action fileOpenAction = new Action( Messages.get( "Main.menu.file.open" ), "Shortcut+O", FOLDER_OPEN_ALT, e -> fileOpen() );
    Action fileCloseAction = new Action( Messages.get( "Main.menu.file.close" ), "Shortcut+W", null, e -> fileClose(), activeFileEditorIsNull );
    Action fileCloseAllAction = new Action( Messages.get( "Main.menu.file.close_all" ), null, null, e -> fileCloseAll(), activeFileEditorIsNull );
    Action fileSaveAction = new Action( Messages.get( "Main.menu.file.save" ), "Shortcut+S", FLOPPY_ALT, e -> fileSave(),
      createActiveBooleanProperty( FileEditorTab::modifiedProperty ).not() );
    Action fileSaveAllAction = new Action( Messages.get( "Main.menu.file.save_all" ), "Shortcut+Shift+S", null, e -> fileSaveAll(),
      Bindings.not( getFileEditorPane().anyFileEditorModifiedProperty() ) );
    Action fileExitAction = new Action( Messages.get( "Main.menu.file.exit" ), null, null, e -> fileExit() );

    // Edit actions
    Action editUndoAction = new Action( Messages.get( "Main.menu.edit.undo" ), "Shortcut+Z", UNDO,
      e -> getActiveEditor().undo(),
      createActiveBooleanProperty( FileEditorTab::canUndoProperty ).not() );
    Action editRedoAction = new Action( Messages.get( "Main.menu.edit.redo" ), "Shortcut+Y", REPEAT,
      e -> getActiveEditor().redo(),
      createActiveBooleanProperty( FileEditorTab::canRedoProperty ).not() );

    // Insert actions
    Action insertBoldAction = new Action( Messages.get( "Main.menu.insert.bold" ), "Shortcut+B", BOLD,
      e -> getActiveEditor().surroundSelection( "**", "**" ),
      activeFileEditorIsNull );
    Action insertItalicAction = new Action( Messages.get( "Main.menu.insert.italic" ), "Shortcut+I", ITALIC,
      e -> getActiveEditor().surroundSelection( "*", "*" ),
      activeFileEditorIsNull );
    Action insertStrikethroughAction = new Action( Messages.get( "Main.menu.insert.strikethrough" ), "Shortcut+T", STRIKETHROUGH,
      e -> getActiveEditor().surroundSelection( "~~", "~~" ),
      activeFileEditorIsNull );
    Action insertBlockquoteAction = new Action( Messages.get( "Main.menu.insert.blockquote" ), "Ctrl+Q", QUOTE_LEFT, // not Shortcut+Q because of conflict on Mac
      e -> getActiveEditor().surroundSelection( "\n\n> ", "" ),
      activeFileEditorIsNull );
    Action insertCodeAction = new Action( Messages.get( "Main.menu.insert.code" ), "Shortcut+K", CODE,
      e -> getActiveEditor().surroundSelection( "`", "`" ),
      activeFileEditorIsNull );
    Action insertFencedCodeBlockAction = new Action( Messages.get( "Main.menu.insert.fenced_code_block" ), "Shortcut+Shift+K", FILE_CODE_ALT,
      e -> getActiveEditor().surroundSelection( "\n\n```\n", "\n```\n\n", Messages.get( "Main.menu.insert.fenced_code_block.prompt" ) ),
      activeFileEditorIsNull );

    Action insertLinkAction = new Action( Messages.get( "Main.menu.insert.link" ), "Shortcut+L", LINK,
      e -> getActiveEditor().insertLink(),
      activeFileEditorIsNull );
    Action insertImageAction = new Action( Messages.get( "Main.menu.insert.image" ), "Shortcut+G", PICTURE_ALT,
      e -> getActiveEditor().insertImage(),
      activeFileEditorIsNull );

    final Action[] headers = new Action[ 6 ];

    // Insert header actions (H1 ... H6)
    for( int i = 1; i <= 6; i++ ) {
      final String hashes = new String( new char[ i ] ).replace( "\0", "#" );
      final String markup = String.format( "\n\n%s ", hashes );
      final String text = Messages.get( "Main.menu.insert.header_" + i );
      final String accelerator = "Shortcut+" + i;
      final String prompt = Messages.get( "Main.menu.insert.header_" + i + ".prompt" );

      headers[ i - 1 ] = new Action( text, accelerator, HEADER,
        e -> getActiveEditor().surroundSelection( markup, "", prompt ),
        activeFileEditorIsNull );
    }

    Action insertUnorderedListAction = new Action( Messages.get( "Main.menu.insert.unordered_list" ), "Shortcut+U", LIST_UL,
      e -> getActiveEditor().surroundSelection( "\n\n* ", "" ),
      activeFileEditorIsNull );
    Action insertOrderedListAction = new Action( Messages.get( "Main.menu.insert.ordered_list" ), "Shortcut+Shift+O", LIST_OL,
      e -> getActiveEditor().surroundSelection( "\n\n1. ", "" ),
      activeFileEditorIsNull );
    Action insertHorizontalRuleAction = new Action( Messages.get( "Main.menu.insert.horizontal_rule" ), "Shortcut+H", null,
      e -> getActiveEditor().surroundSelection( "\n\n---\n\n", "" ),
      activeFileEditorIsNull );

    // Help actions
    Action helpAboutAction = new Action( Messages.get( "Main.menu.help.about" ), null, null, e -> helpAbout() );

    //---- MenuBar ----
    Menu fileMenu = ActionUtils.createMenu( Messages.get( "Main.menu.file" ),
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

    Menu editMenu = ActionUtils.createMenu( Messages.get( "Main.menu.edit" ),
      editUndoAction,
      editRedoAction );

    Menu insertMenu = ActionUtils.createMenu( Messages.get( "Main.menu.insert" ),
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

    Menu helpMenu = ActionUtils.createMenu( Messages.get( "Main.menu.help" ),
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
}
