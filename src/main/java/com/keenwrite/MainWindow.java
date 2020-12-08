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
import com.keenwrite.preferences.UserPreferencesView;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.preview.OutputTabPane;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorFactory;
import com.keenwrite.service.Options;
import com.keenwrite.ui.actions.Action;
import com.keenwrite.ui.actions.MenuAction;
import com.keenwrite.ui.actions.SeparatorAction;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.controlsfx.control.StatusBar;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.ui.actions.ApplicationMenuBar.createMenu;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.*;
import static javafx.application.Platform.runLater;

/**
 * Main window containing a tab pane in the center for file editors.
 *
 * @deprecated Use {@link MainView}.
 */
@Deprecated
public class MainWindow {

  /**
   * This variable must be declared before all other variables to prevent
   * subsequent initializations from failing due to missing user preferences.
   */
  private static final Options sOptions = Services.load( Options.class );

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

  private final DefinitionEditor mDefinitionPane = new DefinitionEditor( null );
  private final OutputTabPane mOutputPane = createOutputTabPane();
  private final FileEditorTabPane mFileEditorPane = new FileEditorTabPane();

  /**
   * Listens on the definition pane for double-click events.
   */
  private final DefinitionNameInjector mDefinitionNameInjector
      = new DefinitionNameInjector( mDefinitionPane );

  public MainWindow() {
    mLineNumberText = createLineNumberText();
    mFindTextField = createFindTextField();
  }

  /**
   * Called after the stage is shown.
   */
  public void init() {
    initFindInput();

    initPreferences();
    initVariableNameInjector();
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
   * After resetting the processors, they will refresh anew to be up-to-date
   * with the files (text and definition) currently loaded into the editor.
   */
  private void resetProcessors() {
    getProcessors().clear();
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

  private OutputTabPane createOutputTabPane() {
    return new OutputTabPane();
  }

  private TextField createFindTextField() {
    return new TextField();
  }

  private Text createLineNumberText() {
    return new Text( get( STATUS_BAR_LINE, 1, 1, 1 ) );
  }

  private Node createMenuBar() {
    // Edit actions
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

    // Insert actions
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

    final MenuAction SEPARATOR_ACTION = new SeparatorAction();

    //---- MenuBar ----

    // Edit Menu
    final var editMenu = createMenu(
        get( "Main.menu.edit" ),
        editFindAction,
        editFindNextAction,
        SEPARATOR_ACTION,
        editPreferencesAction );

    // Insert Menu
    final var insertMenu = createMenu(
        get( "Main.menu.insert" ),
        insertLinkAction,
        insertImageAction
    );

    // Definition Menu
    final var definitionMenu = createMenu(
        get( "Main.menu.definition" ),
        definitionCreateAction,
        definitionInsertAction );

    //---- MenuBar ----
    final var menuBar = new MenuBar(
        editMenu,
        insertMenu,
        definitionMenu );

    return new VBox( menuBar );
  }

  /**
   * Performs the autoinsert function on the active file editor.
   */
  private void definitionInsert() {
    getDefinitionNameInjector().autoinsert();
  }

  //---- Convenience accessors ----------------------------------------------

  private Preferences getPreferences() {
    return sOptions.getState();
  }

  private MarkdownEditorPane getActiveEditorPane() {
    return getActiveFileEditorTab().getEditorPane();
  }

  private FileEditorController getActiveFileEditorTab() {
    return getFileEditorPane().getActiveFileEditor();
  }

  //---- Member accessors ---------------------------------------------------

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

  private UserPreferencesView getUserPreferencesView() {
    return UserPreferencesView.getInstance();
  }
}
