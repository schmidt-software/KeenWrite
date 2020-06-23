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

import com.scrivenvar.definition.DefinitionFactory;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.definition.DefinitionSource;
import com.scrivenvar.definition.MapInterpolator;
import com.scrivenvar.definition.yaml.YamlDefinitionSource;
import com.scrivenvar.editors.EditorPane;
import com.scrivenvar.editors.VariableNameInjector;
import com.scrivenvar.editors.markdown.MarkdownEditorPane;
import com.scrivenvar.preferences.UserPreferences;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.Processor;
import com.scrivenvar.processors.ProcessorFactory;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.Snitch;
import com.scrivenvar.service.events.Notifier;
import com.scrivenvar.util.Action;
import com.scrivenvar.util.ActionBuilder;
import com.scrivenvar.util.ActionUtils;
import javafx.application.Platform;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.StatusBar;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.reactfx.value.Val;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.prefs.Preferences;

import static com.scrivenvar.Constants.*;
import static com.scrivenvar.Messages.get;
import static com.scrivenvar.util.StageState.*;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static javafx.event.Event.fireEvent;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public class MainWindow implements Observer {
  /**
   * The {@code OPTIONS} variable must be declared before all other variables
   * to prevent subsequent initializations from failing due to missing user
   * preferences.
   */
  private final static Options OPTIONS = Services.load( Options.class );
  private final static Snitch SNITCH = Services.load( Snitch.class );
  private final static Notifier NOTIFIER = Services.load( Notifier.class );

  private final Scene mScene;
  private final StatusBar mStatusBar;
  private final Text mLineNumberText;
  private final TextField mFindTextField;

  private final Object mMutex = new Object();

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<FileEditorTab, Processor<String>> mProcessors =
      new HashMap<>();

  private final Map<String, String> mResolvedMap =
      new HashMap<>( DEFAULT_MAP_SIZE );

  /**
   * Called when the definition data is changed.
   */
  private final EventHandler<TreeItem.TreeModificationEvent<Event>>
      mTreeHandler = event -> {
    exportDefinitions( getDefinitionPath() );
    interpolateResolvedMap();
    renderActiveTab();
  };

  /**
   * Called to switch to the definition pane when the user presses the TAB key.
   */
  private final EventHandler<? super KeyEvent> mTabKeyHandler =
      (EventHandler<KeyEvent>) event -> {
        if( event.getCode() == TAB ) {
          getDefinitionPane().requestFocus();
          event.consume();
        }
      };

  /**
   * Called to inject the selected item when the user presses ENTER in the
   * definition pane.
   */
  private final EventHandler<? super KeyEvent> mDefinitionKeyHandler =
      event -> {
        if( event.getCode() == ENTER ) {
          getVariableNameInjector().injectSelectedItem();
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
  private final DefinitionPane mDefinitionPane = new DefinitionPane();
  private final HTMLPreviewPane mPreviewPane = createHTMLPreviewPane();
  private final FileEditorTabPane mFileEditorPane = new FileEditorTabPane(
      mCaretPositionListener,
      mCaretParagraphListener );

  /**
   * Listens on the definition pane for double-click events.
   */
  private final VariableNameInjector mVariableNameInjector
      = new VariableNameInjector( mDefinitionPane );

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
    initVariableNameInjector();

    NOTIFIER.addObserver( this );
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
          // Indirectly refresh the resolved map.
          resetProcessors();

          openDefinitions( newPath );

          // Will create new processors and therefore a new resolved map.
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
                initTabKeyEventListener( tab );
                initScrollEventListener( tab );
//              initSyntaxListener( tab );
              }
            }
          }
        }
    );
  }

  private void initScrollEventListener( final FileEditorTab tab ) {
    final var scrollPane = tab.getEditorPane().getScrollPane();
    final var scrollBar = getPreviewPane().getVerticalScrollBar();

    // Before the drag handler can be attached, the scroll bar for the
    // text editor pane must be visible.
    final ChangeListener<? super Boolean> listener = ( o, oldShow, newShow ) ->
        Platform.runLater( () -> {
          if( newShow ) {
            final var handler = new ScrollEventHandler( scrollPane, scrollBar );
            handler.enabledProperty().bind( tab.selectedProperty() );
          }
        } );

    Val.flatMap( scrollPane.sceneProperty(), Scene::windowProperty )
       .flatMap( Window::showingProperty )
       .addListener( listener );
  }

  /**
   * Listen for new tab selection events.
   */
  private void initTabChangedListener() {
    final FileEditorTabPane editorPane = getFileEditorPane();

    // Update the preview pane changing tabs.
    editorPane.addTabSelectionListener(
        ( tabPane, oldTab, newTab ) -> {
          // If there was no old tab, then this is a first time load, which
          // can be ignored.
          if( oldTab != null ) {
            if( newTab != null ) {
              final FileEditorTab tab = (FileEditorTab) newTab;
              updateVariableNameInjector( tab );
              process( tab );
            }
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
  }

  private void initVariableNameInjector() {
    updateVariableNameInjector( getActiveFileEditorTab() );
  }

  /**
   * Ensure that the keyboard events are received when a new tab is added
   * to the user interface.
   *
   * @param tab The tab editor that can trigger keyboard events.
   */
  private void initTabKeyEventListener( final FileEditorTab tab ) {
    tab.addEventFilter( KeyEvent.KEY_PRESSED, mTabKeyHandler );
  }

  private void initTextChangeListener( final FileEditorTab tab ) {
    tab.addTextChangeListener(
        ( editor, oldValue, newValue ) -> {
          process( tab );
          scrollToParagraph( getCurrentParagraphIndex() );
        }
    );
  }

  private int getCurrentParagraphIndex() {
    return getActiveEditorPane().getCurrentParagraphIndex();
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
    getVariableNameInjector().addListener( tab );
  }

  /**
   * Called whenever the preview pane becomes out of sync with the file editor
   * tab. This can be called when the text changes, the caret paragraph
   * changes,
   * or the file tab changes.
   *
   * @param tab The file editor tab that has been changed in some fashion.
   */
  private void process( final FileEditorTab tab ) {
    if( tab == null ) {
      return;
    }

    getPreviewPane().setPath( tab.getPath() );

    final Processor<String> processor = getProcessors().computeIfAbsent(
        tab, p -> createProcessor( tab )
    );

    try {
      processor.processChain( tab.getEditorText() );
    } catch( final Exception ex ) {
      error( ex );
    }
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
      final DefinitionSource ds = createDefinitionSource( path );
      setDefinitionSource( ds );
      getUserPreferences().definitionPathProperty().setValue( path.toFile() );
      getUserPreferences().save();

      final Tooltip tooltipPath = new Tooltip( path.toString() );
      tooltipPath.setShowDelay( Duration.millis( 200 ) );

      final DefinitionPane pane = getDefinitionPane();
      pane.update( ds );
      pane.addTreeChangeHandler( mTreeHandler );
      pane.addKeyEventHandler( mDefinitionKeyHandler );
      pane.filenameProperty().setValue( path.getFileName().toString() );
      pane.setTooltip( tooltipPath );

      interpolateResolvedMap();
    } catch( final Exception e ) {
      error( e );
    }
  }

  private void exportDefinitions( final Path path ) {
    try {
      final DefinitionPane pane = getDefinitionPane();
      final TreeItem<String> root = pane.getTreeView().getRoot();
      final TreeItem<String> problemChild = pane.isTreeWellFormed();

      if( problemChild == null ) {
        getDefinitionSource().getTreeAdapter().export( root, path );
        getNotifier().clear();
      }
      else {
        final String msg = get(
            "yaml.error.tree.form", problemChild.getValue() );
        getNotifier().notify( msg );
      }
    } catch( final Exception e ) {
      error( e );
    }
  }

  private void interpolateResolvedMap() {
    final Map<String, String> treeMap = getDefinitionPane().toMap();
    final Map<String, String> map = new HashMap<>( treeMap );
    MapInterpolator.interpolate( map );

    getResolvedMap().clear();
    getResolvedMap().putAll( map );
  }

  private void initDefinitionPane() {
    openDefinitions( getDefinitionPath() );
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
    if( value != null ) {
      if( observable instanceof Snitch && value instanceof Path ) {
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
          renderActiveTab();
        }
    );
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

  @SuppressWarnings("SameParameterValue")
  private void insertMarkdown(
      final String leading, final String trailing, final String hint ) {
    getActiveEditorPane().surroundSelection( leading, trailing, hint );
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
      error( ex );
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
        getDefinitionPane().getNode(),
        getFileEditorPane().getNode(),
        getPreviewPane().getNode() );

    splitPane.setDividerPositions(
        getFloat( K_PANE_SPLIT_DEFINITION, .10f ),
        getFloat( K_PANE_SPLIT_EDITOR, .45f ),
        getFloat( K_PANE_SPLIT_PREVIEW, .45f ) );

    getDefinitionPane().prefHeightProperty()
                       .bind( splitPane.heightProperty() );

    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1024, 800 );
    borderPane.setTop( createMenuBar() );
    borderPane.setBottom( getStatusBar() );
    borderPane.setCenter( splitPane );

    final VBox statusBar = new VBox();
    statusBar.setAlignment( Pos.BASELINE_CENTER );
    statusBar.getChildren().add( getLineNumberText() );
    getStatusBar().getRightItems().add( statusBar );

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

    // Insert actions
    final Action insertBoldAction = new ActionBuilder()
        .setText( "Main.menu.insert.bold" )
        .setAccelerator( "Shortcut+B" )
        .setIcon( BOLD )
        .setAction( e -> insertMarkdown( "**", "**" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertItalicAction = new ActionBuilder()
        .setText( "Main.menu.insert.italic" )
        .setAccelerator( "Shortcut+I" )
        .setIcon( ITALIC )
        .setAction( e -> insertMarkdown( "*", "*" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertSuperscriptAction = new ActionBuilder()
        .setText( "Main.menu.insert.superscript" )
        .setAccelerator( "Shortcut+[" )
        .setIcon( SUPERSCRIPT )
        .setAction( e -> insertMarkdown( "^", "^" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertSubscriptAction = new ActionBuilder()
        .setText( "Main.menu.insert.subscript" )
        .setAccelerator( "Shortcut+]" )
        .setIcon( SUBSCRIPT )
        .setAction( e -> insertMarkdown( "~", "~" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
    final Action insertStrikethroughAction = new ActionBuilder()
        .setText( "Main.menu.insert.strikethrough" )
        .setAccelerator( "Shortcut+T" )
        .setIcon( STRIKETHROUGH )
        .setAction( e -> insertMarkdown( "~~", "~~" ) )
        .setDisable( activeFileEditorIsNull )
        .build();
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
        .setAction( e -> getActiveEditorPane().surroundSelection(
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

    // Number of header actions (H1 ... H3)
    final int HEADERS = 3;
    final Action[] headers = new Action[ HEADERS ];

    for( int i = 1; i <= HEADERS; i++ ) {
      final String hashes = new String( new char[ i ] ).replace( "\0", "#" );
      final String markup = String.format( "%n%n%s ", hashes );
      final String text = "Main.menu.insert.header." + i;
      final String accelerator = "Shortcut+" + i;
      final String prompt = text + ".prompt";

      headers[ i - 1 ] = new ActionBuilder()
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
        .setAction( e -> getActiveEditorPane()
            .surroundSelection( "\n\n* ", "" ) )
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

    // Help actions
    final Action helpAboutAction = new ActionBuilder()
        .setText( "Main.menu.help.about" )
        .setAction( e -> helpAbout() )
        .build();

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
        editFindNextAction,
        null,
        editPreferencesAction );

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
        null,
        insertUnorderedListAction,
        insertOrderedListAction,
        insertHorizontalRuleAction );

    final Menu helpMenu = ActionUtils.createMenu(
        get( "Main.menu.help" ),
        helpAboutAction );

    final MenuBar menuBar = new MenuBar(
        fileMenu,
        editMenu,
        insertMenu,
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
    return OPTIONS.getState();
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

  private VariableNameInjector getVariableNameInjector() {
    return mVariableNameInjector;
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return mResolvedMap;
  }

  private Notifier getNotifier() {
    return NOTIFIER;
  }

  //---- Persistence accessors ----------------------------------------------

  private UserPreferences getUserPreferences() {
    return OPTIONS.getUserPreferences();
  }

  private Path getDefinitionPath() {
    return getUserPreferences().getDefinitionPath();
  }
}
