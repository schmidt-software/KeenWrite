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

import com.scrivenvar.definition.*;
import com.scrivenvar.dialogs.RScriptDialog;
import com.scrivenvar.editors.EditorPane;
import com.scrivenvar.editors.VariableNameInjector;
import com.scrivenvar.editors.markdown.MarkdownEditorPane;
import com.scrivenvar.predicates.files.FileTypePredicate;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.Processor;
import com.scrivenvar.processors.ProcessorFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.service.events.Notifier;
import com.scrivenvar.util.Action;
import com.scrivenvar.util.ActionUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.controlsfx.control.StatusBar;
import org.fxmisc.richtext.model.TwoDimensional.Position;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.prefs.Preferences;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.Messages.getLiteral;
import static com.scrivenvar.util.StageState.*;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static javafx.event.Event.fireEvent;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class MainWindow implements Observer {

  private final Options mOptions = Services.load( Options.class );
  private final Snitch mSnitch = Services.load( Snitch.class );
  private final Notifier mNotifier = Services.load( Notifier.class );

  private final Scene mScene;
  private final StatusBar mStatusBar;
  private final Text mLineNumberText;
  private final TextField mFindTextField;

  /**
   * Default definition source is empty.
   */
  private DefinitionSource mDefinitionSource = new EmptyDefinitionSource();
  private DefinitionPane definitionPane;
  private FileEditorTabPane fileEditorPane;
  private HTMLPreviewPane previewPane;

  /**
   * Prevents re-instantiation of processing classes.
   */
  private Map<FileEditorTab, Processor<String>> processors;

  /**
   * Listens on the definition pane for double-click events.
   */
  private VariableNameInjector variableNameInjector;

  public MainWindow() {
    mStatusBar = createStatusBar();
    mLineNumberText = createLineNumberText();
    mFindTextField = createFindTextField();
    mScene = createScene();

    initLayout();
    initFindInput();
    initSnitch();
    initDefinitionListener();
    initTabAddedListener();
    initTabChangedListener();
    initPreferences();
  }

  /**
   * Watch for changes to external files. In particular, this awaits
   * modifications to any XSL files associated with XML files being edited. When
   * an XSL file is modified (external to the application), the snitch's ears
   * perk up and the file is reloaded. This keeps the XSL transformation up to
   * date with what's on the file system.
   */
  private void initSnitch() {
    getSnitch().addObserver( this );
  }

  /**
   * Initialize the find input text field to listen on F3, ENTER, and ESCAPE key
   * presses.
   */
  private void initFindInput() {
    final TextField input = getFindTextField();

    input.setOnKeyPressed( ( KeyEvent event ) -> {
      switch( event.getCode() ) {
        case F3:
        case ENTER:
          findNext();
          break;
        case F:
          if( !event.isControlDown() ) {
            break;
          }
        case ESCAPE:
          getStatusBar().setGraphic( null );
          getActiveFileEditor().getEditorPane().requestFocus();
          break;
      }
    } );

    // Remove when the input field loses focus.
    input.focusedProperty().addListener(
        (
            final ObservableValue<? extends Boolean> focused,
            final Boolean oFocus,
            final Boolean nFocus ) -> {
          if( !nFocus ) {
            getStatusBar().setGraphic( null );
          }
        }
    );
  }

  /**
   * Listen for file editor tab pane to receive an open definition source event.
   */
  private void initDefinitionListener() {
    getFileEditorPane().onOpenDefinitionFileProperty().addListener(
        ( final ObservableValue<? extends Path> file,
          final Path oldPath, final Path newPath ) -> {

          // Indirectly refresh the resolved map.
          setProcessors( null );
          openDefinition( newPath );

          try {
            getSnitch().ignore( oldPath );
            getSnitch().listen( newPath );
          } catch( final IOException ex ) {
            error( ex );
          }

          // Will create new processors and therefore a new resolved map.
          refreshSelectedTab( getActiveFileEditor() );
        }
    );
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
        ( final Change<? extends Tab> change ) -> {
          while( change.next() ) {
            if( change.wasAdded() ) {
              // Multiple tabs can be added simultaneously.
              for( final Tab newTab : change.getAddedSubList() ) {
                final FileEditorTab tab = (FileEditorTab) newTab;

                initTextChangeListener( tab );
                initCaretParagraphListener( tab );
                initKeyboardEventListeners( tab );
//              initSyntaxListener( tab );
              }
            }
          }
        }
    );
  }

  /**
   * Reloads the preferences from the previous session.
   */
  private void initPreferences() {
    restoreDefinitionSource();
    getFileEditorPane().restorePreferences();
    updateDefinitionPane();
  }

  /**
   * Listen for new tab selection events.
   */
  private void initTabChangedListener() {
    final FileEditorTabPane editorPane = getFileEditorPane();

    // Update the preview pane changing tabs.
    editorPane.addTabSelectionListener(
        ( ObservableValue<? extends Tab> tabPane,
          final Tab oldTab, final Tab newTab ) -> {
          updateVariableNameInjector();

          // If there was no old tab, then this is a first time load, which
          // can be ignored.
          if( oldTab != null ) {
            if( newTab == null ) {
              closeRemainingTab();
            }
            else {
              // Update the preview with the edited text.
              refreshSelectedTab( (FileEditorTab) newTab );
            }
          }
        }
    );
  }

  /**
   * Ensure that the keyboard events are received when a new tab is added
   * to the user interface.
   *
   * @param tab The tab that can trigger keyboard events, such as control+space.
   */
  private void initKeyboardEventListeners( final FileEditorTab tab ) {
    final VariableNameInjector vin = getVariableNameInjector();
    vin.initKeyboardEventListeners( tab );
  }

  private void initTextChangeListener( final FileEditorTab tab ) {
    tab.addTextChangeListener(
        ( ObservableValue<? extends String> editor,
          final String oldValue, final String newValue ) ->
            refreshSelectedTab( tab )
    );
  }

  private void initCaretParagraphListener( final FileEditorTab tab ) {
    tab.addCaretParagraphListener(
        ( ObservableValue<? extends Integer> editor,
          final Integer oldValue, final Integer newValue ) ->
            refreshSelectedTab( tab )
    );
  }

  private void updateVariableNameInjector() {
    getVariableNameInjector().setFileEditorTab( getActiveFileEditor() );
  }

  private void setVariableNameInjector( final VariableNameInjector injector ) {
    this.variableNameInjector = injector;
  }

  private synchronized VariableNameInjector getVariableNameInjector() {
    if( this.variableNameInjector == null ) {
      final VariableNameInjector vin = createVariableNameInjector();
      setVariableNameInjector( vin );
    }

    return this.variableNameInjector;
  }

  private VariableNameInjector createVariableNameInjector() {
    final FileEditorTab tab = getActiveFileEditor();
    final DefinitionPane pane = getDefinitionPane();

    return new VariableNameInjector( tab, pane );
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

    // TODO: https://github.com/DaveJarvis/scrivenvar/issues/29
    final Position p = tab.getCaretOffset();
    getLineNumberText().setText(
        get( STATUS_BAR_LINE,
             p.getMajor() + 1,
             p.getMinor() + 1,
             tab.getCaretPosition() + 1
        )
    );

    Processor<String> processor = getProcessors().get( tab );

    if( processor == null ) {
      processor = createProcessor( tab );
      getProcessors().put( tab, processor );
    }

    try {
      getNotifier().clear();
      processor.processChain( tab.getEditorText() );
    } catch( final Exception ex ) {
      error( ex );
    }
  }

  /**
   * Used to find text in the active file editor window.
   */
  private void find() {
    final TextField input = getFindTextField();
    getStatusBar().setGraphic( input );
    input.requestFocus();
  }

  public void findNext() {
    getActiveFileEditor().searchNext( getFindTextField().getText() );
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
    return getDefinitionSource().asTreeView();
  }

  /**
   * Called when a definition source is opened.
   *
   * @param path Path to the definition source that was opened.
   */
  private void openDefinition( final Path path ) {
    try {
      final DefinitionSource ds = createDefinitionSource( path.toString() );
      setDefinitionSource( ds );
      storeDefinitionSource();
      updateDefinitionPane();
    } catch( final Exception e ) {
      error( e );
    }
  }

  private void updateDefinitionPane() {
    getDefinitionPane().setRoot( getTreeView() );
  }

  private void restoreDefinitionSource() {
    final Preferences preferences = getPreferences();
    final String source = preferences.get( PERSIST_DEFINITION_SOURCE, "" );

    setDefinitionSource( createDefinitionSource( source ) );
  }

  private void storeDefinitionSource() {
    final Preferences preferences = getPreferences();
    final DefinitionSource ds = getDefinitionSource();

    preferences.put( PERSIST_DEFINITION_SOURCE, ds.toString() );
  }

  /**
   * Called when the last open tab is closed to clear the preview pane.
   */
  private void closeRemainingTab() {
    getPreviewPane().clear();
  }

  /**
   * Called when an exception occurs that warrants the user's attention.
   *
   * @param e The exception with a message that the user should know about.
   */
  private void error( final Exception e ) {
    getNotifier().notify( e );
  }

  //---- File actions -------------------------------------------------------

  /**
   * Called when an observable instance has changed. This is called by both the
   * snitch service and the notify service. The snitch service can be called for
   * different file types, including definition sources.
   *
   * @param observable The observed instance.
   * @param value      The noteworthy item.
   */
  @Override
  public void update( final Observable observable, final Object value ) {
    if( value != null ) {
      if( observable instanceof Snitch && value instanceof Path ) {
        final Path path = (Path) value;
        final FileTypePredicate predicate
            = new FileTypePredicate( GLOB_DEFINITION_EXTENSIONS );

        // Reload definitions.
        if( predicate.test( path.toFile() ) ) {
          updateDefinitionSource( path );
        }

        updateSelectedTab();
      }
      else if( observable instanceof Notifier && value instanceof String ) {
        updateStatusBar( (String) value );
      }
    }
  }

  /**
   * Updates the status bar to show the given message.
   *
   * @param s The message to show in the status bar.
   */
  private void updateStatusBar( final String s ) {
    Platform.runLater(
        () -> {
          final int index = s.indexOf( '\n' );
          final String message = s.substring(
              0, index > 0 ? index : s.length() );

          getStatusBar().setText( message );
        }
    );
  }

  /**
   * Called when a file has been modified.
   */
  private void updateSelectedTab() {
    Platform.runLater(
        () -> {
          // Brute-force XSLT file reload by re-instantiating all processors.
          resetProcessors();
          refreshSelectedTab( getActiveFileEditor() );
        }
    );
  }

  /**
   * Reloads the definition source from the given path.
   *
   * @param path The path containing new definition information.
   */
  private void updateDefinitionSource( final Path path ) {
    Platform.runLater( () -> openDefinition( path ) );
  }

  /**
   * After resetting the processors, they will refresh anew to be up-to-date
   * with the files (text and definition) currently loaded into the editor.
   */
  private void resetProcessors() {
    getProcessors().clear();
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

  private void fileSaveAs() {
    final FileEditorTab editor = getActiveFileEditor();
    getFileEditorPane().saveEditorAs( editor );
    getProcessors().remove( editor );

    try {
      refreshSelectedTab( editor );
    } catch( final Exception ex ) {
      getNotifier().notify( ex );
    }
  }

  private void fileSaveAll() {
    getFileEditorPane().saveAllEditors();
  }

  private void fileExit() {
    final Window window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
  }

  //---- R menu actions
  private void rScript() {
    final String script = getPreferences().get( PERSIST_R_STARTUP, "" );
    final RScriptDialog dialog = new RScriptDialog(
        getWindow(), "Dialog.r.script.title", script );
    final Optional<String> result = dialog.showAndWait();

    result.ifPresent( this::putStartupScript );
  }

  private void rDirectory() {
    final TextInputDialog dialog = new TextInputDialog(
        getPreferences().get( PERSIST_R_DIRECTORY, USER_DIRECTORY )
    );

    dialog.setTitle( get( "Dialog.r.directory.title" ) );
    dialog.setHeaderText( getLiteral( "Dialog.r.directory.header" ) );
    dialog.setContentText( "Directory" );

    final Optional<String> result = dialog.showAndWait();

    result.ifPresent( this::putStartupDirectory );
  }

  /**
   * Stores the R startup script into the user preferences.
   */
  private void putStartupScript( final String script ) {
    putPreference( PERSIST_R_STARTUP, script );
  }

  /**
   * Stores the R bootstrap script directory into the user preferences.
   */
  private void putStartupDirectory( final String directory ) {
    putPreference( PERSIST_R_DIRECTORY, directory );
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

  protected Scene getScene() {
    return mScene;
  }

  public Window getWindow() {
    return getScene().getWindow();
  }

  private MarkdownEditorPane getActiveEditor() {
    final EditorPane pane = getActiveFileEditor().getEditorPane();

    return pane instanceof MarkdownEditorPane
        ? (MarkdownEditorPane) pane
        : null;
  }

  private FileEditorTab getActiveFileEditor() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------
  private void setProcessors(
      final Map<FileEditorTab, Processor<String>> map ) {
    this.processors = map;
  }

  private Map<FileEditorTab, Processor<String>> getProcessors() {
    if( this.processors == null ) {
      setProcessors( new HashMap<>() );
    }

    return this.processors;
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
    assert definitionSource != null;
    mDefinitionSource = definitionSource;
  }

  private DefinitionSource getDefinitionSource() {
    return mDefinitionSource;
  }

  private DefinitionPane getDefinitionPane() {
    if( this.definitionPane == null ) {
      this.definitionPane = createDefinitionPane();
    }

    return this.definitionPane;
  }

  private Options getOptions() {
    return mOptions;
  }

  private Snitch getSnitch() {
    return mSnitch;
  }

  private Notifier getNotifier() {
    return mNotifier;
  }

  private Text getLineNumberText() {
    return mLineNumberText;
  }

  private StatusBar getStatusBar() {
    return mStatusBar;
  }

  private TextField getFindTextField() {
    return this.mFindTextField;
  }

  //---- Member creators ----------------------------------------------------

  /**
   * Factory to create processors that are suited to different file types.
   *
   * @param tab The tab that is subjected to processing.
   * @return A processor suited to the file type specified by the tab's path.
   */
  private Processor<String> createProcessor( final FileEditorTab tab ) {
    return createProcessorFactory().createProcessor( tab );
  }

  private ProcessorFactory createProcessorFactory() {
    return new ProcessorFactory( getPreviewPane(), getResolvedMap() );
  }

  private DefinitionSource createDefinitionSource( final String path ) {
    DefinitionSource ds;

    try {
      ds = createDefinitionFactory().createDefinitionSource( path );

      if( ds instanceof FileDefinitionSource ) {
        try {
          getNotifier().notify( ds.getError() );
          getSnitch().listen( ((FileDefinitionSource) ds).getPath() );
        } catch( final Exception ex ) {
          error( ex );
        }
      }
    } catch( final Exception ex ) {
      ds = new EmptyDefinitionSource();
      error( ex );
    }

    return ds;
  }

  private TextField createFindTextField() {
    return new TextField();
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

  private StatusBar createStatusBar() {
    return new StatusBar();
  }

  private Scene createScene() {
    final SplitPane splitPane = new SplitPane(
        getDefinitionPane().getNode(),
        getFileEditorPane().getNode(),
        getPreviewPane().getNode() );

    splitPane.setDividerPositions(
        getFloat( K_PANE_SPLIT_DEFINITION, .10f ),
        getFloat( K_PANE_SPLIT_EDITOR, .45f ),
        getFloat( K_PANE_SPLIT_PREVIEW, .45f ) );

    // See: http://broadlyapplicable.blogspot
    // .ca/2015/03/javafx-capture-restorePreferences-splitpane.html
    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1024, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setBottom( getStatusBar() );
    borderPane.setCenter( splitPane );

    final VBox box = new VBox();
    box.setAlignment( Pos.BASELINE_CENTER );
    box.getChildren().add( getLineNumberText() );
    getStatusBar().getRightItems().add( box );

    return new Scene( borderPane );
  }

  private Text createLineNumberText() {
    return new Text( get( STATUS_BAR_LINE, 1, 1, 1 ) );
  }

  private Node createMenuBar() {
    final BooleanBinding activeFileEditorIsNull =
        getFileEditorPane().activeFileEditorProperty()
                           .isNull();

    // File actions
    final Action fileNewAction = new Action( get( "Main.menu.file.new" ),
                                             "Shortcut+N", FILE_ALT,
                                             e -> fileNew() );
    final Action fileOpenAction = new Action( get( "Main.menu.file.open" ),
                                              "Shortcut+O", FOLDER_OPEN_ALT,
                                              e -> fileOpen() );
    final Action fileCloseAction = new Action( get( "Main.menu.file.close" ),
                                               "Shortcut+W", null,
                                               e -> fileClose(),
                                               activeFileEditorIsNull );
    final Action fileCloseAllAction = new Action( get(
        "Main.menu.file.close_all" ), null, null, e -> fileCloseAll(),
                                                  activeFileEditorIsNull );
    final Action fileSaveAction = new Action( get( "Main.menu.file.save" ),
                                              "Shortcut+S", FLOPPY_ALT,
                                              e -> fileSave(),
                                              createActiveBooleanProperty(
                                                  FileEditorTab::modifiedProperty )
                                                  .not() );
    final Action fileSaveAsAction = new Action( Messages.get(
        "Main.menu.file.save_as" ), null, null, e -> fileSaveAs(),
                                                activeFileEditorIsNull );
    final Action fileSaveAllAction = new Action(
        get( "Main.menu.file.save_all" ), "Shortcut+Shift+S", null,
        e -> fileSaveAll(),
        Bindings.not( getFileEditorPane().anyFileEditorModifiedProperty() ) );
    final Action fileExitAction = new Action( get( "Main.menu.file.exit" ),
                                              null,
                                              null,
                                              e -> fileExit() );

    // Edit actions
    final Action editUndoAction = new Action( get( "Main.menu.edit.undo" ),
                                              "Shortcut+Z", UNDO,
                                              e -> getActiveEditor().undo(),
                                              createActiveBooleanProperty(
                                                  FileEditorTab::canUndoProperty )
                                                  .not() );
    final Action editRedoAction = new Action( get( "Main.menu.edit.redo" ),
                                              "Shortcut+Y", REPEAT,
                                              e -> getActiveEditor().redo(),
                                              createActiveBooleanProperty(
                                                  FileEditorTab::canRedoProperty )
                                                  .not() );
    final Action editFindAction = new Action( Messages.get(
        "Main.menu.edit.find" ), "Ctrl+F", SEARCH,
                                              e -> find(),
                                              activeFileEditorIsNull );
    final Action editFindNextAction = new Action( Messages.get(
        "Main.menu.edit.find.next" ), "F3", null,
                                                  e -> findNext(),
                                                  activeFileEditorIsNull );

    // Insert actions
    final Action insertBoldAction = new Action( get( "Main.menu.insert.bold" ),
                                                "Shortcut+B", BOLD,
                                                e -> getActiveEditor().surroundSelection(
                                                    "**", "**" ),
                                                activeFileEditorIsNull );
    final Action insertItalicAction = new Action(
        get( "Main.menu.insert.italic" ), "Shortcut+I", ITALIC,
        e -> getActiveEditor().surroundSelection( "*", "*" ),
        activeFileEditorIsNull );
    final Action insertSuperscriptAction = new Action( get(
        "Main.menu.insert.superscript" ), "Shortcut+[", SUPERSCRIPT,
                                                       e -> getActiveEditor().surroundSelection(
                                                           "^", "^" ),
                                                       activeFileEditorIsNull );
    final Action insertSubscriptAction = new Action( get(
        "Main.menu.insert.subscript" ), "Shortcut+]", SUBSCRIPT,
                                                     e -> getActiveEditor().surroundSelection(
                                                         "~", "~" ),
                                                     activeFileEditorIsNull );
    final Action insertStrikethroughAction = new Action( get(
        "Main.menu.insert.strikethrough" ), "Shortcut+T", STRIKETHROUGH,
                                                         e -> getActiveEditor().surroundSelection(
                                                             "~~", "~~" ),
                                                         activeFileEditorIsNull );
    final Action insertBlockquoteAction = new Action( get(
        "Main.menu.insert.blockquote" ),
                                                      "Ctrl+Q",
                                                      QUOTE_LEFT,
                                                      // not Shortcut+Q
                                                      // because of conflict
                                                      // on Mac
                                                      e -> getActiveEditor().surroundSelection(
                                                          "\n\n> ", "" ),
                                                      activeFileEditorIsNull );
    final Action insertCodeAction = new Action( get( "Main.menu.insert.code" ),
                                                "Shortcut+K", CODE,
                                                e -> getActiveEditor().surroundSelection(
                                                    "`", "`" ),
                                                activeFileEditorIsNull );
    final Action insertFencedCodeBlockAction = new Action( get(
        "Main.menu.insert.fenced_code_block" ),
                                                           "Shortcut+Shift+K",
                                                           FILE_CODE_ALT,
                                                           e -> getActiveEditor()
                                                               .surroundSelection(
                                                                   "\n\n```\n",
                                                                   "\n```\n\n",
                                                                   get(
                                                                       "Main.menu.insert.fenced_code_block.prompt" ) ),
                                                           activeFileEditorIsNull );

    final Action insertLinkAction = new Action( get( "Main.menu.insert.link" ),
                                                "Shortcut+L", LINK,
                                                e -> getActiveEditor().insertLink(),
                                                activeFileEditorIsNull );
    final Action insertImageAction = new Action( get( "Main.menu.insert" +
                                                          ".image" ),
                                                 "Shortcut+G", PICTURE_ALT,
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
                                     e -> getActiveEditor().surroundSelection(
                                         markup, "", prompt ),
                                     activeFileEditorIsNull );
    }

    final Action insertUnorderedListAction = new Action(
        get( "Main.menu.insert.unordered_list" ), "Shortcut+U", LIST_UL,
        e -> getActiveEditor().surroundSelection( "\n\n* ", "" ),
        activeFileEditorIsNull );
    final Action insertOrderedListAction = new Action(
        get( "Main.menu.insert.ordered_list" ), "Shortcut+Shift+O", LIST_OL,
        e -> getActiveEditor().surroundSelection( "\n\n1. ", "" ),
        activeFileEditorIsNull );
    final Action insertHorizontalRuleAction = new Action(
        get( "Main.menu.insert.horizontal_rule" ), "Shortcut+H", null,
        e -> getActiveEditor().surroundSelection( "\n\n---\n\n", "" ),
        activeFileEditorIsNull );

    // R actions
    final Action mRScriptAction = new Action(
        get( "Main.menu.r.script" ), null, null, e -> rScript() );

    final Action mRDirectoryAction = new Action(
        get( "Main.menu.r.directory" ), null, null, e -> rDirectory() );

    // Help actions
    final Action helpAboutAction = new Action(
        get( "Main.menu.help.about" ), null, null, e -> helpAbout() );

    //---- MenuBar ----
    final Menu fileMenu = ActionUtils.createMenu(
        get( "Main.menu.file" ),
        fileNewAction,
        fileOpenAction,
        null,
        fileCloseAction,
        fileCloseAllAction,
        null,
        fileSaveAction,
        fileSaveAsAction,
        fileSaveAllAction,
        null,
        fileExitAction );

    final Menu editMenu = ActionUtils.createMenu(
        get( "Main.menu.edit" ),
        editUndoAction,
        editRedoAction,
        editFindAction,
        editFindNextAction );

    final Menu insertMenu = ActionUtils.createMenu(
        get( "Main.menu.insert" ),
        insertBoldAction,
        insertItalicAction,
        insertSuperscriptAction,
        insertSubscriptAction,
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

    final Menu rMenu = ActionUtils.createMenu(
        get( "Main.menu.r" ),
        mRScriptAction,
        mRDirectoryAction );

    final Menu helpMenu = ActionUtils.createMenu(
        get( "Main.menu.help" ),
        helpAboutAction );

    final MenuBar menuBar = new MenuBar(
        fileMenu,
        editMenu,
        insertMenu,
        rMenu,
        helpMenu );

    //---- ToolBar ----
    final ToolBar toolBar = ActionUtils.createToolBar(
        fileNewAction,
        fileOpenAction,
        fileSaveAction,
        null,
        editUndoAction,
        editRedoAction,
        null,
        insertBoldAction,
        insertItalicAction,
        insertSuperscriptAction,
        insertSubscriptAction,
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
        ( observable, oldFileEditor, newFileEditor ) -> {
          b.unbind();

          if( newFileEditor != null ) {
            b.bind( func.apply( newFileEditor ) );
          }
          else {
            b.set( false );
          }
        }
    );

    return b;
  }

  private void initLayout() {
    final Scene appScene = getScene();

    appScene.getStylesheets().add( STYLESHEET_SCENE );

    // TODO: Apply an XML syntax highlighting for XML files.
//    appScene.getStylesheets().add( STYLESHEET_XML );
    appScene.windowProperty().addListener(
        ( observable, oldWindow, newWindow ) ->
            newWindow.setOnCloseRequest(
                e -> {
                  if( !getFileEditorPane().closeAllEditors() ) {
                    e.consume();
                  }
                }
            )
    );
  }

  private void putPreference( final String key, final String value ) {
    try {
      getPreferences().put( key, value );
    } catch( final Exception ex ) {
      getNotifier().notify( ex );
    }
  }
}
