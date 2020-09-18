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
package com.scrivenvar;

import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.scrivenvar.definition.DefinitionFactory;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.definition.DefinitionSource;
import com.scrivenvar.definition.MapInterpolator;
import com.scrivenvar.definition.yaml.YamlDefinitionSource;
import com.scrivenvar.editors.DefinitionNameInjector;
import com.scrivenvar.editors.EditorPane;
import com.scrivenvar.editors.markdown.MarkdownEditorPane;
import com.scrivenvar.preferences.UserPreferences;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.HtmlPreviewProcessor;
import com.scrivenvar.processors.Processor;
import com.scrivenvar.processors.ProcessorFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.spelling.api.SpellCheckListener;
import com.scrivenvar.spelling.api.SpellChecker;
import com.scrivenvar.spelling.impl.PermissiveSpeller;
import com.scrivenvar.spelling.impl.SymSpellSpeller;
import com.scrivenvar.util.Action;
import com.scrivenvar.util.ActionBuilder;
import com.scrivenvar.util.ActionUtils;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.VisitHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.StatusBar;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.value.Val;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.StatusBarNotifier.alert;
import static com.scrivenvar.util.StageState.*;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static javafx.application.Platform.runLater;
import static javafx.event.Event.fireEvent;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

/**
 * Main window containing a tab pane in the center for file editors.
 */
public class MainWindow implements Observer {
  /**
   * The {@code OPTIONS} variable must be declared before all other variables
   * to prevent subsequent initializations from failing due to missing user
   * preferences.
   */
  private static final Options sOptions = Services.load( Options.class );
  private static final Snitch SNITCH = Services.load( Snitch.class );

  private final Scene mScene;
  private final StatusBar mStatusBar;
  private final Text mLineNumberText;
  private final TextField mFindTextField;
  private final SpellChecker mSpellChecker;

  private final Object mMutex = new Object();

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<FileEditorTab, Processor<String>> mProcessors =
      new HashMap<>();

  private final Map<String, String> mResolvedMap =
      new HashMap<>( DEFAULT_MAP_SIZE );

  private final EventHandler<PreferencesFxEvent> mRPreferencesListener =
      event -> rerender();

  /**
   * Called when the definition data is changed.
   */
  private final EventHandler<TreeItem.TreeModificationEvent<Event>>
      mTreeHandler = event -> {
    exportDefinitions( getDefinitionPath() );
    interpolateResolvedMap();
    rerender();
  };

  /**
   * Called to inject the selected item when the user presses ENTER in the
   * definition pane.
   */
  private final EventHandler<? super KeyEvent> mDefinitionKeyHandler =
      event -> {
        if( event.getCode() == ENTER ) {
          getDefinitionNameInjector().injectSelectedItem();
        }
      };

  private final ChangeListener<Integer> mCaretPositionListener =
      ( observable, oldPosition, newPosition ) -> {
        final FileEditorTab tab = getActiveFileEditorTab();
        final EditorPane pane = tab.getEditorPane();
        final StyleClassedTextArea editor = pane.getEditor();

        getLineNumberText().setText(
            get( STATUS_BAR_LINE,
                 editor.getCurrentParagraph() + 1,
                 editor.getParagraphs().size(),
                 editor.getCaretPosition()
            )
        );
      };

  private final ChangeListener<Integer> mCaretParagraphListener =
      ( observable, oldIndex, newIndex ) ->
          scrollToParagraph( newIndex, true );

  private DefinitionSource mDefinitionSource = createDefaultDefinitionSource();
  private final DefinitionPane mDefinitionPane = createDefinitionPane();
  private final HTMLPreviewPane mPreviewPane = createHTMLPreviewPane();
  private final FileEditorTabPane mFileEditorPane = new FileEditorTabPane(
      mCaretPositionListener,
      mCaretParagraphListener );

  /**
   * Listens on the definition pane for double-click events.
   */
  private final DefinitionNameInjector mDefinitionNameInjector
      = new DefinitionNameInjector( mDefinitionPane );

  public MainWindow() {
    mStatusBar = createStatusBar();
    mLineNumberText = createLineNumberText();
    mFindTextField = createFindTextField();
    mScene = createScene();
    mSpellChecker = createSpellChecker();

    // Add the close request listener before the window is shown.
    initLayout();
    StatusBarNotifier.setStatusBar( mStatusBar );
  }

  /**
   * Called after the stage is shown.
   */
  public void init() {
    initFindInput();
    initSnitch();
    initDefinitionListener();
    initTabAddedListener();
    initTabChangedListener();
    initPreferences();
    initVariableNameInjector();
  }

  private void initLayout() {
    final var scene = getScene();

    scene.getStylesheets().add( STYLESHEET_SCENE );
    scene.windowProperty().addListener(
        ( unused, oldWindow, newWindow ) ->
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
    SNITCH.addObserver( this );
  }

  /**
   * Listen for {@link FileEditorTabPane} to receive open definition file
   * event.
   */
  private void initDefinitionListener() {
    getFileEditorPane().onOpenDefinitionFileProperty().addListener(
        ( final ObservableValue<? extends Path> file,
          final Path oldPath, final Path newPath ) -> {
          openDefinitions( newPath );
          rerender();
        }
    );
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
          renderActiveTab();
        }
    );
  }

  /**
   * When tabs are added, hook the various change listeners onto the new
   * tab sothat the preview pane refreshes as necessary.
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
                initScrollEventListener( tab );
                initSpellCheckListener( tab );
//              initSyntaxListener( tab );
              }
            }
          }
        }
    );
  }

  private void initTextChangeListener( final FileEditorTab tab ) {
    tab.addTextChangeListener(
        ( __, ov, nv ) -> {
          process( tab );
          scrollToParagraph( getCurrentParagraphIndex() );
        }
    );
  }

  private void initScrollEventListener( final FileEditorTab tab ) {
    final var scrollPane = tab.getScrollPane();
    final var scrollBar = getPreviewPane().getVerticalScrollBar();

    addShowListener( scrollPane, ( __ ) -> {
      final var handler = new ScrollEventHandler( scrollPane, scrollBar );
      handler.enabledProperty().bind( tab.selectedProperty() );
    } );
  }

  /**
   * Listen for changes to the any particular paragraph and perform a quick
   * spell check upon it. The style classes in the editor will be changed to
   * mark any spelling mistakes in the paragraph. The user may then interact
   * with any misspelled word (i.e., any piece of text that is marked) to
   * revise the spelling.
   *
   * @param tab The tab to spellcheck.
   */
  private void initSpellCheckListener( final FileEditorTab tab ) {
    final var editor = tab.getEditorPane().getEditor();

    // When the editor first appears, run a full spell check. This allows
    // spell checking while typing to be restricted to the active paragraph,
    // which is usually substantially smaller than the whole document.
    addShowListener(
        editor, ( __ ) -> spellcheck( editor, editor.getText() )
    );

    // Use the plain text changes so that notifications of style changes
    // are suppressed. Checking against the identity ensures that only
    // new text additions or deletions trigger proofreading.
    editor.plainTextChanges()
          .filter( p -> !p.isIdentity() ).subscribe( change -> {

      // Only perform a spell check on the current paragraph. The
      // entire document is processed once, when opened.
      final var offset = change.getPosition();
      final var position = editor.offsetToPosition( offset, Forward );
      final var paraId = position.getMajor();
      final var paragraph = editor.getParagraph( paraId );
      final var text = paragraph.getText();

      // Ensure that styles aren't doubled-up.
      editor.clearStyle( paraId );

      spellcheck( editor, text, paraId );
    } );
  }

  /**
   * Listen for new tab selection events.
   */
  private void initTabChangedListener() {
    final FileEditorTabPane editorPane = getFileEditorPane();

    // Update the preview pane changing tabs.
    editorPane.addTabSelectionListener(
        ( tabPane, oldTab, newTab ) -> {
          if( newTab == null ) {
            // Clear the preview pane when closing an editor. When the last
            // tab is closed, this ensures that the preview pane is empty.
            getPreviewPane().clear();
          }
          else {
            final var tab = (FileEditorTab) newTab;
            updateVariableNameInjector( tab );
            process( tab );
          }
        }
    );
  }

  /**
   * Reloads the preferences from the previous session.
   */
  private void initPreferences() {
    initDefinitionPane();
    getFileEditorPane().initPreferences();
    getUserPreferences().addSaveEventHandler( mRPreferencesListener );
  }

  private void initVariableNameInjector() {
    updateVariableNameInjector( getActiveFileEditorTab() );
  }

  /**
   * Calls the listener when the given node is shown for the first time. The
   * visible property is not the same as the initial showing event; visibility
   * can be triggered numerous times (such as going off screen).
   * <p>
   * This is called, for example, before the drag handler can be attached,
   * because the scrollbar for the text editor pane must be visible.
   * </p>
   *
   * @param node     The node to watch for showing.
   * @param consumer The consumer to invoke when the event fires.
   */
  private void addShowListener(
      final Node node, final Consumer<Void> consumer ) {
    final ChangeListener<? super Boolean> listener = ( o, oldShow, newShow ) ->
        runLater( () -> {
          if( newShow != null && newShow ) {
            try {
              consumer.accept( null );
            } catch( final Exception ex ) {
              alert( ex );
            }
          }
        } );

    Val.flatMap( node.sceneProperty(), Scene::windowProperty )
       .flatMap( Window::showingProperty )
       .addListener( listener );
  }

  private void scrollToParagraph( final int id ) {
    scrollToParagraph( id, false );
  }

  /**
   * @param id    The paragraph to scroll to, will be approximated if it doesn't
   *              exist.
   * @param force {@code true} means to force scrolling immediately, which
   *              should only be attempted when it is known that the document
   *              has been fully rendered. Otherwise the internal map of ID
   *              attributes will be incomplete and scrolling will flounder.
   */
  private void scrollToParagraph( final int id, final boolean force ) {
    synchronized( mMutex ) {
      final var previewPane = getPreviewPane();
      final var scrollPane = previewPane.getScrollPane();
      final int approxId = getActiveEditorPane().approximateParagraphId( id );

      if( force ) {
        previewPane.scrollTo( approxId );
      }
      else {
        previewPane.tryScrollTo( approxId );
      }

      scrollPane.repaint();
    }
  }

  private void updateVariableNameInjector( final FileEditorTab tab ) {
    getDefinitionNameInjector().addListener( tab );
  }

  /**
   * Called whenever the preview pane becomes out of sync with the file editor
   * tab. This can be called when the text changes, the caret paragraph
   * changes, or the file tab changes.
   *
   * @param tab The file editor tab that has been changed in some fashion.
   */
  private void process( final FileEditorTab tab ) {
    if( tab != null ) {
      getPreviewPane().setPath( tab.getPath() );

      final Processor<String> processor = getProcessors().computeIfAbsent(
          tab, p -> createProcessors( tab )
      );

      try {
        processChain( processor, tab.getEditorText() );
      } catch( final Exception ex ) {
        alert( ex );
      }
    }
  }

  /**
   * Executes the processing chain, operating on the given string.
   *
   * @param handler The first processor in the chain to call.
   * @param text    The initial value of the text to process.
   * @return The final value of the text that was processed by the chain.
   */
  private String processChain( Processor<String> handler, String text ) {
    while( handler != null && text != null ) {
      text = handler.apply( text );
      handler = handler.next();
    }

    return text;
  }

  private void renderActiveTab() {
    process( getActiveFileEditorTab() );
  }

  /**
   * Called when a definition source is opened.
   *
   * @param path Path to the definition source that was opened.
   */
  private void openDefinitions( final Path path ) {
    try {
      final var ds = createDefinitionSource( path );
      setDefinitionSource( ds );

      final var prefs = getUserPreferences();
      prefs.definitionPathProperty().setValue( path.toFile() );
      prefs.save();

      final var tooltipPath = new Tooltip( path.toString() );
      tooltipPath.setShowDelay( Duration.millis( 200 ) );

      final var pane = getDefinitionPane();
      pane.update( ds );
      pane.addTreeChangeHandler( mTreeHandler );
      pane.addKeyEventHandler( mDefinitionKeyHandler );
      pane.filenameProperty().setValue( path.getFileName().toString() );
      pane.setTooltip( tooltipPath );

      interpolateResolvedMap();
    } catch( final Exception ex ) {
      alert( ex );
    }
  }

  private void exportDefinitions( final Path path ) {
    try {
      final var pane = getDefinitionPane();
      final var root = pane.getTreeView().getRoot();
      final var problemChild = pane.isTreeWellFormed();

      if( problemChild == null ) {
        getDefinitionSource().getTreeAdapter().export( root, path );
      }
      else {
        alert( "yaml.error.tree.form", problemChild.getValue() );
      }
    } catch( final Exception ex ) {
      alert( ex );
    }
  }

  private void interpolateResolvedMap() {
    final var treeMap = getDefinitionPane().toMap();
    final var map = new HashMap<>( treeMap );
    MapInterpolator.interpolate( map );

    getResolvedMap().clear();
    getResolvedMap().putAll( map );
  }

  private void initDefinitionPane() {
    openDefinitions( getDefinitionPath() );
  }

  //---- File actions -------------------------------------------------------

  /**
   * Called when an {@link Observable} instance has changed. This is called
   * by both the {@link Snitch} service and the notify service. The @link
   * Snitch} service can be called for different file types, including
   * {@link DefinitionSource} instances.
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

  private void fileNew() {
    getFileEditorPane().newEditor();
  }

  private void fileOpen() {
    getFileEditorPane().openFileDialog();
  }

  private void fileClose() {
    getFileEditorPane().closeEditor( getActiveFileEditorTab(), true );
  }

  /**
   * TODO: Upon closing, first remove the tab change listeners. (There's no
   * need to re-render each tab when all are being closed.)
   */
  private void fileCloseAll() {
    getFileEditorPane().closeAllEditors();
  }

  private void fileSave() {
    getFileEditorPane().saveEditor( getActiveFileEditorTab() );
  }

  private void fileSaveAs() {
    final FileEditorTab editor = getActiveFileEditorTab();
    getFileEditorPane().saveEditorAs( editor );
    getProcessors().remove( editor );

    try {
      process( editor );
    } catch( final Exception ex ) {
      alert( ex );
    }
  }

  private void fileSaveAll() {
    getFileEditorPane().saveAllEditors();
  }

  private void fileExit() {
    final Window window = getWindow();
    fireEvent( window, new WindowEvent( window, WINDOW_CLOSE_REQUEST ) );
  }

  //---- Edit actions -------------------------------------------------------

  /**
   * Transform the Markdown into HTML then copy that HTML into the copy
   * buffer.
   */
  private void copyHtml() {
    final var markdown = getActiveEditorPane().getText();
    final var processors = createProcessorFactory().createProcessors(
        getActiveFileEditorTab()
    );

    final var chain = processors.remove( HtmlPreviewProcessor.class );

    final String html = processChain( chain, markdown );

    final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    content.putString( html );
    clipboard.setContent( content );
  }

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
    getUserPreferences().show();
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

  //---- View actions -------------------------------------------------------

  private void viewRefresh() {
    rerender();
  }

  //---- Help actions -------------------------------------------------------

  private void helpAbout() {
    final Alert alert = new Alert( AlertType.INFORMATION );
    alert.setTitle( get( "Dialog.about.title" ) );
    alert.setHeaderText( get( "Dialog.about.header" ) );
    alert.setContentText( get( "Dialog.about.content" ) );
    alert.setGraphic( new ImageView( new Image( FILE_LOGO_32 ) ) );
    alert.initOwner( getWindow() );

    alert.showAndWait();
  }

  //---- Member creators ----------------------------------------------------

  private SpellChecker createSpellChecker() {
    try {
      final Collection<String> lexicon = readLexicon( "en.txt" );
      return SymSpellSpeller.forLexicon( lexicon );
    } catch( final Exception ex ) {
      alert( ex );
      return new PermissiveSpeller();
    }
  }

  /**
   * Factory to create processors that are suited to different file types.
   *
   * @param tab The tab that is subjected to processing.
   * @return A processor suited to the file type specified by the tab's path.
   */
  private Processor<String> createProcessors( final FileEditorTab tab ) {
    return createProcessorFactory().createProcessors( tab );
  }

  private ProcessorFactory createProcessorFactory() {
    return new ProcessorFactory( getPreviewPane(), getResolvedMap() );
  }

  private DefinitionPane createDefinitionPane() {
    return new DefinitionPane();
  }

  private HTMLPreviewPane createHTMLPreviewPane() {
    return new HTMLPreviewPane();
  }

  private DefinitionSource createDefaultDefinitionSource() {
    return new YamlDefinitionSource( getDefinitionPath() );
  }

  private DefinitionSource createDefinitionSource( final Path path ) {
    try {
      return createDefinitionFactory().createDefinitionSource( path );
    } catch( final Exception ex ) {
      alert( ex );
      return createDefaultDefinitionSource();
    }
  }

  private TextField createFindTextField() {
    return new TextField();
  }

  private DefinitionFactory createDefinitionFactory() {
    return new DefinitionFactory();
  }

  private StatusBar createStatusBar() {
    return new StatusBar();
  }

  private Scene createScene() {
    final SplitPane splitPane = new SplitPane(
        getDefinitionPane(),
        getFileEditorPane(),
        getPreviewPane() );

    splitPane.setDividerPositions(
        getFloat( K_PANE_SPLIT_DEFINITION, .22f ),
        getFloat( K_PANE_SPLIT_EDITOR, .60f ),
        getFloat( K_PANE_SPLIT_PREVIEW, .18f ) );

    getDefinitionPane().prefHeightProperty()
                       .bind( splitPane.heightProperty() );

    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1280, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setBottom( getStatusBar() );
    borderPane.setCenter( splitPane );

    final VBox statusBar = new VBox();
    statusBar.setAlignment( Pos.BASELINE_CENTER );
    statusBar.getChildren().add( getLineNumberText() );
    getStatusBar().getRightItems().add( statusBar );

    // Force preview pane refresh on Windows.
    if( SystemUtils.IS_OS_WINDOWS ) {
      splitPane.getDividers().get( 1 ).positionProperty().addListener(
          ( l, oValue, nValue ) -> runLater(
              () -> getPreviewPane().getScrollPane().repaint()
          )
      );
    }

    return new Scene( borderPane );
  }

  private Text createLineNumberText() {
    return new Text( get( STATUS_BAR_LINE, 1, 1, 1 ) );
  }

  private Node createMenuBar() {
    final BooleanBinding activeFileEditorIsNull =
        getFileEditorPane().activeFileEditorProperty().isNull();

    // File actions
    final Action fileNewAction = new ActionBuilder()
        .setText( "Main.menu.file.new" )
        .setAccelerator( "Shortcut+N" )
        .setIcon( FILE_ALT )
        .setAction( e -> fileNew() )
        .build();
    final Action fileOpenAction = new ActionBuilder()
        .setText( "Main.menu.file.open" )
        .setAccelerator( "Shortcut+O" )
        .setIcon( FOLDER_OPEN_ALT )
        .setAction( e -> fileOpen() )
        .build();
    final Action fileCloseAction = new ActionBuilder()
        .setText( "Main.menu.file.close" )
        .setAccelerator( "Shortcut+W" )
        .setAction( e -> fileClose() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action fileCloseAllAction = new ActionBuilder()
        .setText( "Main.menu.file.close_all" )
        .setAction( e -> fileCloseAll() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action fileSaveAction = new ActionBuilder()
        .setText( "Main.menu.file.save" )
        .setAccelerator( "Shortcut+S" )
        .setIcon( FLOPPY_ALT )
        .setAction( e -> fileSave() )
        .setDisable( createActiveBooleanProperty(
            FileEditorTab::modifiedProperty ).not() )
        .build();
    final Action fileSaveAsAction = new ActionBuilder()
        .setText( "Main.menu.file.save_as" )
        .setAction( e -> fileSaveAs() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action fileSaveAllAction = new ActionBuilder()
        .setText( "Main.menu.file.save_all" )
        .setAccelerator( "Shortcut+Shift+S" )
        .setAction( e -> fileSaveAll() )
        .setDisable( Bindings.not(
            getFileEditorPane().anyFileEditorModifiedProperty() ) )
        .build();
    final Action fileExitAction = new ActionBuilder()
        .setText( "Main.menu.file.exit" )
        .setAction( e -> fileExit() )
        .build();

    // Edit actions
    final Action editCopyHtmlAction = new ActionBuilder()
        .setText( "Main.menu.edit.copy.html" )
        .setIcon( HTML5 )
        .setAction( e -> copyHtml() )
        .setDisable( activeFileEditorIsNull )
        .build();

    final Action editUndoAction = new ActionBuilder()
        .setText( "Main.menu.edit.undo" )
        .setAccelerator( "Shortcut+Z" )
        .setIcon( UNDO )
        .setAction( e -> getActiveEditorPane().undo() )
        .setDisable( createActiveBooleanProperty(
            FileEditorTab::canUndoProperty ).not() )
        .build();
    final Action editRedoAction = new ActionBuilder()
        .setText( "Main.menu.edit.redo" )
        .setAccelerator( "Shortcut+Y" )
        .setIcon( REPEAT )
        .setAction( e -> getActiveEditorPane().redo() )
        .setDisable( createActiveBooleanProperty(
            FileEditorTab::canRedoProperty ).not() )
        .build();

    final Action editCutAction = new ActionBuilder()
        .setText( "Main.menu.edit.cut" )
        .setAccelerator( "Shortcut+X" )
        .setIcon( CUT )
        .setAction( e -> getActiveEditorPane().cut() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action editCopyAction = new ActionBuilder()
        .setText( "Main.menu.edit.copy" )
        .setAccelerator( "Shortcut+C" )
        .setIcon( COPY )
        .setAction( e -> getActiveEditorPane().copy() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action editPasteAction = new ActionBuilder()
        .setText( "Main.menu.edit.paste" )
        .setAccelerator( "Shortcut+V" )
        .setIcon( PASTE )
        .setAction( e -> getActiveEditorPane().paste() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action editSelectAllAction = new ActionBuilder()
        .setText( "Main.menu.edit.selectAll" )
        .setAccelerator( "Shortcut+A" )
        .setAction( e -> getActiveEditorPane().selectAll() )
        .setDisable( activeFileEditorIsNull )
        .build();

    final Action editFindAction = new ActionBuilder()
        .setText( "Main.menu.edit.find" )
        .setAccelerator( "Ctrl+F" )
        .setIcon( SEARCH )
        .setAction( e -> editFind() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action editFindNextAction = new ActionBuilder()
        .setText( "Main.menu.edit.find.next" )
        .setAccelerator( "F3" )
        .setIcon( null )
        .setAction( e -> editFindNext() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action editPreferencesAction = new ActionBuilder()
        .setText( "Main.menu.edit.preferences" )
        .setAccelerator( "Ctrl+Alt+S" )
        .setAction( e -> editPreferences() )
        .build();

    // Format actions
    final Action formatBoldAction = new ActionBuilder()
        .setText( "Main.menu.format.bold" )
        .setAccelerator( "Shortcut+B" )
        .setIcon( BOLD )
        .setAction( e -> insertMarkdown( "**", "**" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action formatItalicAction = new ActionBuilder()
        .setText( "Main.menu.format.italic" )
        .setAccelerator( "Shortcut+I" )
        .setIcon( ITALIC )
        .setAction( e -> insertMarkdown( "*", "*" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action formatSuperscriptAction = new ActionBuilder()
        .setText( "Main.menu.format.superscript" )
        .setAccelerator( "Shortcut+[" )
        .setIcon( SUPERSCRIPT )
        .setAction( e -> insertMarkdown( "^", "^" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action formatSubscriptAction = new ActionBuilder()
        .setText( "Main.menu.format.subscript" )
        .setAccelerator( "Shortcut+]" )
        .setIcon( SUBSCRIPT )
        .setAction( e -> insertMarkdown( "~", "~" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action formatStrikethroughAction = new ActionBuilder()
        .setText( "Main.menu.format.strikethrough" )
        .setAccelerator( "Shortcut+T" )
        .setIcon( STRIKETHROUGH )
        .setAction( e -> insertMarkdown( "~~", "~~" ) )
        .setDisable( activeFileEditorIsNull )
        .build();

    // Insert actions
    final Action insertBlockquoteAction = new ActionBuilder()
        .setText( "Main.menu.insert.blockquote" )
        .setAccelerator( "Ctrl+Q" )
        .setIcon( QUOTE_LEFT )
        .setAction( e -> insertMarkdown( "\n\n> ", "" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertCodeAction = new ActionBuilder()
        .setText( "Main.menu.insert.code" )
        .setAccelerator( "Shortcut+K" )
        .setIcon( CODE )
        .setAction( e -> insertMarkdown( "`", "`" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertFencedCodeBlockAction = new ActionBuilder()
        .setText( "Main.menu.insert.fenced_code_block" )
        .setAccelerator( "Shortcut+Shift+K" )
        .setIcon( FILE_CODE_ALT )
        .setAction( e -> insertMarkdown(
            "\n\n```\n",
            "\n```\n\n",
            get( "Main.menu.insert.fenced_code_block.prompt" ) ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertLinkAction = new ActionBuilder()
        .setText( "Main.menu.insert.link" )
        .setAccelerator( "Shortcut+L" )
        .setIcon( LINK )
        .setAction( e -> getActiveEditorPane().insertLink() )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertImageAction = new ActionBuilder()
        .setText( "Main.menu.insert.image" )
        .setAccelerator( "Shortcut+G" )
        .setIcon( PICTURE_ALT )
        .setAction( e -> getActiveEditorPane().insertImage() )
        .setDisable( activeFileEditorIsNull )
        .build();

    // Number of heading actions (H1 ... H3)
    final int HEADINGS = 3;
    final Action[] headings = new Action[ HEADINGS ];

    for( int i = 1; i <= HEADINGS; i++ ) {
      final String hashes = new String( new char[ i ] ).replace( "\0", "#" );
      final String markup = String.format( "%n%n%s ", hashes );
      final String text = "Main.menu.insert.heading." + i;
      final String accelerator = "Shortcut+" + i;
      final String prompt = text + ".prompt";

      headings[ i - 1 ] = new ActionBuilder()
          .setText( text )
          .setAccelerator( accelerator )
          .setIcon( HEADER )
          .setAction( e -> insertMarkdown( markup, "", get( prompt ) ) )
          .setDisable( activeFileEditorIsNull )
          .build();
    }

    final Action insertUnorderedListAction = new ActionBuilder()
        .setText( "Main.menu.insert.unordered_list" )
        .setAccelerator( "Shortcut+U" )
        .setIcon( LIST_UL )
        .setAction( e -> insertMarkdown( "\n\n* ", "" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertOrderedListAction = new ActionBuilder()
        .setText( "Main.menu.insert.ordered_list" )
        .setAccelerator( "Shortcut+Shift+O" )
        .setIcon( LIST_OL )
        .setAction( e -> insertMarkdown(
            "\n\n1. ", "" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertHorizontalRuleAction = new ActionBuilder()
        .setText( "Main.menu.insert.horizontal_rule" )
        .setAccelerator( "Shortcut+H" )
        .setAction( e -> insertMarkdown(
            "\n\n---\n\n", "" ) )
        .setDisable( activeFileEditorIsNull )
        .build();

    // Definition actions
    final Action definitionCreateAction = new ActionBuilder()
        .setText( "Main.menu.definition.create" )
        .setIcon( TREE )
        .setAction( e -> getDefinitionPane().addItem() )
        .build();
    final Action definitionInsertAction = new ActionBuilder()
        .setText( "Main.menu.definition.insert" )
        .setAccelerator( "Ctrl+Space" )
        .setIcon( STAR )
        .setAction( e -> definitionInsert() )
        .build();

    // Help actions
    final Action helpAboutAction = new ActionBuilder()
        .setText( "Main.menu.help.about" )
        .setAction( e -> helpAbout() )
        .build();

    //---- MenuBar ----

    // File Menu
    final var fileMenu = ActionUtils.createMenu(
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

    // Edit Menu
    final var editMenu = ActionUtils.createMenu(
        get( "Main.menu.edit" ),
        editCopyHtmlAction,
        null,
        editUndoAction,
        editRedoAction,
        null,
        editCutAction,
        editCopyAction,
        editPasteAction,
        editSelectAllAction,
        null,
        editFindAction,
        editFindNextAction,
        null,
        editPreferencesAction );

    // Format Menu
    final var formatMenu = ActionUtils.createMenu(
        get( "Main.menu.format" ),
        formatBoldAction,
        formatItalicAction,
        formatSuperscriptAction,
        formatSubscriptAction,
        formatStrikethroughAction
    );

    // Insert Menu
    final var insertMenu = ActionUtils.createMenu(
        get( "Main.menu.insert" ),
        insertBlockquoteAction,
        insertCodeAction,
        insertFencedCodeBlockAction,
        null,
        insertLinkAction,
        insertImageAction,
        null,
        headings[ 0 ],
        headings[ 1 ],
        headings[ 2 ],
        null,
        insertUnorderedListAction,
        insertOrderedListAction,
        insertHorizontalRuleAction
    );

    // Definition Menu
    final var definitionMenu = ActionUtils.createMenu(
        get( "Main.menu.definition" ),
        definitionCreateAction,
        definitionInsertAction );

    // Help Menu
    final var helpMenu = ActionUtils.createMenu(
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

    //---- ToolBar ----
    final var toolBar = ActionUtils.createToolBar(
        fileNewAction,
        fileOpenAction,
        fileSaveAction,
        null,
        editUndoAction,
        editRedoAction,
        editCutAction,
        editCopyAction,
        editPasteAction,
        null,
        formatBoldAction,
        formatItalicAction,
        formatSuperscriptAction,
        formatSubscriptAction,
        insertBlockquoteAction,
        insertCodeAction,
        insertFencedCodeBlockAction,
        null,
        insertLinkAction,
        insertImageAction,
        null,
        headings[ 0 ],
        null,
        insertUnorderedListAction,
        insertOrderedListAction );

    return new VBox( menuBar, toolBar );
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
      final Function<FileEditorTab, ObservableBooleanValue> func ) {

    final BooleanProperty b = new SimpleBooleanProperty();
    final FileEditorTab tab = getActiveFileEditorTab();

    if( tab != null ) {
      b.bind( func.apply( tab ) );
    }

    getFileEditorPane().activeFileEditorProperty().addListener(
        ( observable, oldFileEditor, newFileEditor ) -> {
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

  private int getCurrentParagraphIndex() {
    return getActiveEditorPane().getCurrentParagraphIndex();
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

  private FileEditorTab getActiveFileEditorTab() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------

  protected Scene getScene() {
    return mScene;
  }

  private SpellChecker getSpellChecker() {
    return mSpellChecker;
  }

  private Map<FileEditorTab, Processor<String>> getProcessors() {
    return mProcessors;
  }

  private FileEditorTabPane getFileEditorPane() {
    return mFileEditorPane;
  }

  private HTMLPreviewPane getPreviewPane() {
    return mPreviewPane;
  }

  private void setDefinitionSource(
      final DefinitionSource definitionSource ) {
    assert definitionSource != null;
    mDefinitionSource = definitionSource;
  }

  private DefinitionSource getDefinitionSource() {
    return mDefinitionSource;
  }

  private DefinitionPane getDefinitionPane() {
    return mDefinitionPane;
  }

  private Text getLineNumberText() {
    return mLineNumberText;
  }

  private StatusBar getStatusBar() {
    return mStatusBar;
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

  private Path getDefinitionPath() {
    return getUserPreferences().getDefinitionPath();
  }

  //---- Spelling -----------------------------------------------------------

  /**
   * Delegates to {@link #spellcheck(StyleClassedTextArea, String, int)}.
   * This is called to spell check the document, rather than a single paragraph.
   *
   * @param text The full document text.
   */
  private void spellcheck(
      final StyleClassedTextArea editor, final String text ) {
    spellcheck( editor, text, -1 );
  }

  /**
   * Spellchecks a subset of the entire document.
   *
   * @param text   Look up words for this text in the lexicon.
   * @param paraId Set to -1 to apply resulting style spans to the entire
   *               text.
   */
  private void spellcheck(
      final StyleClassedTextArea editor, final String text, final int paraId ) {
    final var builder = new StyleSpansBuilder<Collection<String>>();
    final var runningIndex = new AtomicInteger( 0 );
    final var checker = getSpellChecker();

    // The text nodes must be relayed through a contextual "visitor" that
    // can return text in chunks with correlative offsets into the string.
    // This allows Markdown, R Markdown, XML, and R XML documents to return
    // sets of words to check.

    final var node = mParser.parse( text );
    final var visitor = new TextVisitor( ( visited, bIndex, eIndex ) -> {
      // Treat hyphenated compound words as individual words.
      final var check = visited.replace( '-', ' ' );

      checker.proofread( check, ( misspelled, prevIndex, currIndex ) -> {
        prevIndex += bIndex;
        currIndex += bIndex;

        // Clear styling between lexiconically absent words.
        builder.add( emptyList(), prevIndex - runningIndex.get() );
        builder.add( singleton( "spelling" ), currIndex - prevIndex );
        runningIndex.set( currIndex );
      } );
    } );

    visitor.visit( node );

    // If the running index was set, at least one word triggered the listener.
    if( runningIndex.get() > 0 ) {
      // Clear styling after the last lexiconically absent word.
      builder.add( emptyList(), text.length() - runningIndex.get() );

      final var spans = builder.create();

      if( paraId >= 0 ) {
        editor.setStyleSpans( paraId, 0, spans );
      }
      else {
        editor.setStyleSpans( 0, spans );
      }
    }
  }

  @SuppressWarnings("SameParameterValue")
  private Collection<String> readLexicon( final String filename )
      throws Exception {
    final var path = "/" + LEXICONS_DIRECTORY + "/" + filename;

    try( final var resource = getClass().getResourceAsStream( path ) ) {
      if( resource == null ) {
        throw new FileNotFoundException( path );
      }

      try( final var isr = new InputStreamReader( resource, UTF_8 );
           final var reader = new BufferedReader( isr ) ) {
        return reader.lines().collect( Collectors.toList() );
      }
    }
  }

  // TODO: Replace using Markdown processor instantiated for Markdown files.
  // FIXME: https://github.com/DaveJarvis/scrivenvar/issues/59
  private final Parser mParser = Parser.builder().build();

  // TODO: Replace with generic interface; provide Markdown/XML implementations.
  // FIXME: https://github.com/DaveJarvis/scrivenvar/issues/59
  private static final class TextVisitor {
    private final NodeVisitor mVisitor = new NodeVisitor( new VisitHandler<>(
        com.vladsch.flexmark.ast.Text.class, this::visit )
    );

    private final SpellCheckListener mConsumer;

    public TextVisitor( final SpellCheckListener consumer ) {
      mConsumer = consumer;
    }

    private void visit( final com.vladsch.flexmark.util.ast.Node node ) {
      if( node instanceof com.vladsch.flexmark.ast.Text ) {
        mConsumer.accept( node.getChars().toString(),
                          node.getStartOffset(),
                          node.getEndOffset() );
      }

      mVisitor.visitChildren( node );
    }
  }
}
