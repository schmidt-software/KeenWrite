/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.TextResource;
import com.keenwrite.editors.definition.DefinitionEditor;
import com.keenwrite.editors.definition.DefinitionTabSceneFactory;
import com.keenwrite.editors.definition.TreeTransformer;
import com.keenwrite.editors.definition.yaml.YamlTreeTransformer;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.io.MediaType;
import com.keenwrite.preferences.Key;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.IdentityProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorFactory;
import com.keenwrite.processors.markdown.extensions.caret.CaretExtension;
import com.keenwrite.service.events.Notifier;
import com.keenwrite.sigils.RSigilOperator;
import com.keenwrite.sigils.SigilOperator;
import com.keenwrite.sigils.Tokens;
import com.keenwrite.sigils.YamlSigilOperator;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.preferences.WorkspaceKeys.*;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static com.keenwrite.service.events.Notifier.NO;
import static com.keenwrite.service.events.Notifier.YES;
import static java.util.stream.Collectors.groupingBy;
import static javafx.application.Platform.runLater;
import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.util.Duration.millis;
import static javax.swing.SwingUtilities.invokeLater;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Responsible for wiring together the main application components for a
 * particular workspace (project). These include the definition views,
 * text editors, and preview pane along with any corresponding controllers.
 */
public final class MainPane extends SplitPane {
  private static final Notifier sNotifier = Services.load( Notifier.class );

  /**
   * Used when opening files to determine how each file should be binned and
   * therefore what tab pane to be opened within.
   */
  private static final Set<MediaType> PLAIN_TEXT_FORMAT = Set.of(
    TEXT_MARKDOWN, TEXT_R_MARKDOWN, TEXT_R_XML, UNDEFINED
  );

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<TextResource, Processor<String>> mProcessors =
    new HashMap<>();

  private final Workspace mWorkspace;

  /**
   * Groups similar file type tabs together.
   */
  private final Map<MediaType, DetachableTabPane> mTabPanes = new HashMap<>();

  /**
   * Stores definition names and values.
   */
  private final Map<String, String> mResolvedMap =
    new HashMap<>( MAP_SIZE_DEFAULT );

  /**
   * Renders the actively selected plain text editor tab.
   */
  private final HtmlPreview mHtmlPreview;

  /**
   * Changing the active editor fires the value changed event. This allows
   * refreshes to happen when external definitions are modified and need to
   * trigger the processing chain.
   */
  private final ObjectProperty<TextEditor> mActiveTextEditor =
    createActiveTextEditor();

  /**
   * Changing the active definition editor fires the value changed event. This
   * allows refreshes to happen when external definitions are modified and need
   * to trigger the processing chain.
   */
  private final ObjectProperty<TextDefinition> mActiveDefinitionEditor =
    createActiveDefinitionEditor( mActiveTextEditor );

  /**
   * Responsible for creating a new scene when a tab is detached into
   * its own window frame.
   */
  private final DefinitionTabSceneFactory mDefinitionTabSceneFactory =
    createDefinitionTabSceneFactory( mActiveDefinitionEditor );

  /**
   * Tracks the number of detached tab panels opened into their own windows,
   * which allows unique identification of subordinate windows by their title.
   * It is doubtful more than 128 windows, much less 256, will be created.
   */
  private byte mWindowCount;

  /**
   * Called when the definition data is changed.
   */
  private final EventHandler<TreeModificationEvent<Event>> mTreeHandler =
    event -> {
      final var editor = mActiveDefinitionEditor.get();

      resolve( editor );
      process( getActiveTextEditor() );
      save( editor );
    };

  /**
   * Adds all content panels to the main user interface. This will load the
   * configuration settings from the workspace to reproduce the settings from
   * a previous session.
   */
  public MainPane( final Workspace workspace ) {
    mWorkspace = workspace;
    mHtmlPreview = new HtmlPreview( workspace );

    open( bin( getRecentFiles() ) );
    viewPreview();
    setDividerPositions( calculateDividerPositions() );

    // Once the main scene's window regains focus, update the active definition
    // editor to the currently selected tab.
    runLater(
      () -> {
        getWindow().focusedProperty().addListener( ( c, o, n ) -> {
          if( n != null && n ) {
            final var pane = mTabPanes.get( TEXT_YAML );
            final var model = pane.getSelectionModel();
            final var tab = model.getSelectedItem();

            if( tab != null ) {
              final var resource = tab.getContent();

              if( resource instanceof TextDefinition ) {
                mActiveDefinitionEditor.set( (TextDefinition) tab.getContent() );
              }
            }
          }
        } );

        getWindow().setOnCloseRequest( ( event ) -> {
          // Order matters here. We want to close all the tabs to ensure each
          // is saved, but after they are closed, the workspace should still
          // retain the list of files that were open. If this line came after
          // closing, then restarting the application would list no files.
          mWorkspace.save();

          if( closeAll() ) {
            Platform.exit();
            System.exit( 0 );
          }
          else {
            event.consume();
          }
        } );
      }
    );
  }

  /**
   * TODO: Load divider positions from exported settings, see bin() comment.
   */
  private double[] calculateDividerPositions() {
    final var ratio = 100f / getItems().size() / 100;
    final var positions = getDividerPositions();

    for( int i = 0; i < positions.length; i++ ) {
      positions[ i ] = ratio * i;
    }

    return positions;
  }

  /**
   * Opens all the files into the application, provided the paths are unique.
   * This may only be called for any type of files that a user can edit
   * (i.e., update and persist), such as definitions and text files.
   *
   * @param files The list of files to open.
   */
  public void open( final List<File> files ) {
    files.forEach( this::open );
  }

  /**
   * This opens the given file. Since the preview pane is not a file that
   * can be opened, it is safe to add a listener to the detachable pane.
   *
   * @param file The file to open.
   */
  private void open( final File file ) {
    final var tab = createTab( file );
    final var node = tab.getContent();
    final var mediaType = MediaType.valueFrom( file );
    final var tabPane = obtainDetachableTabPane( mediaType );
    final var newTabPane = !getItems().contains( tabPane );

    tab.setTooltip( createTooltip( file ) );
    tabPane.setFocusTraversable( false );
    tabPane.setTabClosingPolicy( ALL_TABS );
    tabPane.getTabs().add( tab );

    if( newTabPane ) {
      var index = getItems().size();

      if( node instanceof TextDefinition ) {
        tabPane.setSceneFactory( mDefinitionTabSceneFactory::create );
        index = 0;
      }

      addTabPane( index, tabPane );
    }

    getRecentFiles().add( file.getAbsolutePath() );
  }

  /**
   * Opens a new text editor document using the default document file name.
   */
  public void newTextEditor() {
    open( DOCUMENT_DEFAULT );
  }

  /**
   * Opens a new definition editor document using the default definition
   * file name.
   */
  public void newDefinitionEditor() {
    open( DEFINITION_DEFAULT );
  }

  /**
   * Iterates over all tab panes to find all {@link TextEditor}s and request
   * that they save themselves.
   */
  public void saveAll() {
    mTabPanes.forEach(
      ( mt, tp ) -> tp.getTabs().forEach( ( tab ) -> {
        final var node = tab.getContent();
        if( node instanceof TextEditor ) {
          save( ((TextEditor) node) );
        }
      } )
    );
  }

  /**
   * Requests that the active {@link TextEditor} saves itself. Don't bother
   * checking if modified first because if the user swaps external media from
   * an external source (e.g., USB thumb drive), save should not second-guess
   * the user: save always re-saves. Also, it's less code.
   */
  public void save() {
    save( getActiveTextEditor() );
  }

  /**
   * Saves the active {@link TextEditor} under a new name.
   *
   * @param file The new active editor {@link File} reference.
   */
  public void saveAs( final File file ) {
    assert file != null;
    final var editor = getActiveTextEditor();
    final var tab = getTab( editor );

    editor.rename( file );
    tab.ifPresent( t -> {
      t.setText( editor.getFilename() );
      t.setTooltip( createTooltip( file ) );
    } );

    save();
  }

  /**
   * Saves the given {@link TextResource} to a file. This is typically used
   * to save either an instance of {@link TextEditor} or {@link TextDefinition}.
   *
   * @param resource The resource to export.
   */
  private void save( final TextResource resource ) {
    try {
      resource.save();
    } catch( final Exception ex ) {
      clue( ex );
      sNotifier.alert(
        getWindow(), resource.getPath(), "TextResource.saveFailed", ex
      );
    }
  }

  /**
   * Closes all open {@link TextEditor}s; all {@link TextDefinition}s stay open.
   *
   * @return {@code true} when all editors, modified or otherwise, were
   * permitted to close; {@code false} when one or more editors were modified
   * and the user requested no closing.
   */
  public boolean closeAll() {
    var closable = true;

    for( final var entry : mTabPanes.entrySet() ) {
      final var tabPane = entry.getValue();
      final var tabIterator = tabPane.getTabs().iterator();

      while( tabIterator.hasNext() ) {
        final var tab = tabIterator.next();
        final var resource = tab.getContent();

        if( !(resource instanceof TextResource) ) {
          continue;
        }

        if( canClose( (TextResource) resource ) ) {
          tabIterator.remove();
          close( tab );
        }
        else {
          closable = false;
        }
      }
    }

    return closable;
  }

  /**
   * Calls the tab's {@link Tab#getOnClosed()} handler to carry out a close
   * event.
   *
   * @param tab The {@link Tab} that was closed.
   */
  private void close( final Tab tab ) {
    final var handler = tab.getOnClosed();

    if( handler != null ) {
      handler.handle( new ActionEvent() );
    }
  }

  /**
   * Closes the active tab; delegates to {@link #canClose(TextResource)}.
   */
  public void close() {
    final var editor = getActiveTextEditor();
    if( canClose( editor ) ) {
      close( editor );
    }
  }

  /**
   * Closes the given {@link TextResource}. This must not be called from within
   * a loop that iterates over the tab panes using {@code forEach}, lest a
   * concurrent modification exception be thrown.
   *
   * @param resource The {@link TextResource} to close, without confirming with
   *                 the user.
   */
  private void close( final TextResource resource ) {
    getTab( resource ).ifPresent(
      ( tab ) -> {
        tab.getTabPane().getTabs().remove( tab );
        close( tab );
      }
    );
  }

  /**
   * Answers whether the given {@link TextResource} may be closed.
   *
   * @param editor The {@link TextResource} to try closing.
   * @return {@code true} when the editor may be closed; {@code false} when
   * the user has requested to keep the editor open.
   */
  private boolean canClose( final TextResource editor ) {
    final var editorTab = getTab( editor );
    final var canClose = new AtomicBoolean( true );

    if( editor.isModified() ) {
      final var filename = new StringBuilder();
      editorTab.ifPresent( ( tab ) -> filename.append( tab.getText() ) );

      final var message = sNotifier.createNotification(
        Messages.get( "Alert.file.close.title" ),
        Messages.get( "Alert.file.close.text" ),
        filename.toString()
      );

      final var dialog = sNotifier.createConfirmation( getWindow(), message );

      dialog.showAndWait().ifPresent(
        save -> canClose.set( save == YES ? editor.save() : save == NO )
      );
    }

    return canClose.get();
  }

  private ObjectProperty<TextEditor> createActiveTextEditor() {
    final var editor = new SimpleObjectProperty<TextEditor>();

    editor.addListener( ( c, o, n ) -> {
      if( n != null ) {
        mHtmlPreview.setBaseUri( n.getPath() );
        process( n );
      }
    } );

    return editor;
  }

  /**
   * Adds the HTML preview tab to its own tab pane. This will only add the
   * preview once.
   */
  public void viewPreview() {
    final var tabPane = obtainDetachableTabPane( TEXT_HTML );

    // Prevent multiple HTML previews because in the end, there can be only one.
    for( final var tab : tabPane.getTabs() ) {
      if( tab.getContent() == mHtmlPreview ) {
        return;
      }
    }

    tabPane.addTab( "HTML", mHtmlPreview );
    addTabPane( tabPane );
  }

  public void viewRefresh() {
    mHtmlPreview.refresh();
  }

  /**
   * Returns the tab that contains the given {@link TextEditor}.
   *
   * @param editor The {@link TextEditor} instance to find amongst the tabs.
   * @return The first tab having content that matches the given tab.
   */
  private Optional<Tab> getTab( final TextResource editor ) {
    return mTabPanes.values()
                    .stream()
                    .flatMap( pane -> pane.getTabs().stream() )
                    .filter( tab -> editor.equals( tab.getContent() ) )
                    .findFirst();
  }

  /**
   * Creates a new {@link DefinitionEditor} wrapped in a listener that
   * is used to detect when the active {@link DefinitionEditor} has changed.
   * Upon changing, the {@link #mResolvedMap} is updated and the active
   * text editor is refreshed.
   *
   * @param editor Text editor to update with the revised resolved map.
   * @return A newly configured property that represents the active
   * {@link DefinitionEditor}, never null.
   */
  private ObjectProperty<TextDefinition> createActiveDefinitionEditor(
    final ObjectProperty<TextEditor> editor ) {
    final var definitions = new SimpleObjectProperty<TextDefinition>();
    definitions.addListener( ( c, o, n ) -> {
      resolve( n == null ? createDefinitionEditor() : n );
      process( editor.get() );
    } );

    return definitions;
  }

  /**
   * Instantiates a factory that's responsible for creating new scenes when
   * a tab is dropped outside of any application window. The definition tabs
   * are fairly complex in that only one may be active at any time. When
   * activated, the {@link #mResolvedMap} must be updated to reflect the
   * hierarchy displayed in the {@link DefinitionEditor}.
   *
   * @param activeDefinitionEditor The current {@link DefinitionEditor}.
   * @return An object that listens to {@link DefinitionEditor} tab focus
   * changes.
   */
  private DefinitionTabSceneFactory createDefinitionTabSceneFactory(
    final ObjectProperty<TextDefinition> activeDefinitionEditor ) {
    return new DefinitionTabSceneFactory( ( tab ) -> {
      assert tab != null;

      var node = tab.getContent();
      if( node instanceof TextDefinition ) {
        activeDefinitionEditor.set( (DefinitionEditor) node );
      }
    } );
  }

  private DetachableTab createTab( final File file ) {
    final var r = createTextResource( file );
    final var tab = new DetachableTab( r.getFilename(), r.getNode() );

    r.modifiedProperty().addListener(
      ( c, o, n ) -> tab.setText( r.getFilename() + (n ? "*" : "") )
    );

    // This is called when either the tab is closed by the user clicking on
    // the tab's close icon or when closing (all) from the file menu.
    tab.setOnClosed(
      ( __ ) -> getRecentFiles().remove( file.getAbsolutePath() )
    );

    return tab;
  }

  /**
   * Creates bins for the different {@link MediaType}s, which eventually are
   * added to the UI as separate tab panes. If ever a general-purpose scene
   * exporter is developed to serialize a scene to an FXML file, this could
   * be replaced by such a class.
   * <p>
   * When binning the files, this makes sure that at least one file exists
   * for every type. If the user has opted to close a particular type (such
   * as the definition pane), the view will suppressed elsewhere.
   * </p>
   * <p>
   * The order that the binned files are returned will be reflected in the
   * order that the corresponding panes are rendered in the UI.
   * </p>
   *
   * @param paths The file paths to bin according to their type.
   * @return An in-order list of files, first by structured definition files,
   * then by plain text documents.
   */
  private List<File> bin( final SetProperty<String> paths ) {
    // Treat all files destined for the text editor as plain text documents
    // so that they are added to the same pane. Grouping by TEXT_PLAIN is a
    // bit arbitrary, but means explicitly capturing TEXT_PLAIN isn't needed.
    final Function<MediaType, MediaType> bin =
      m -> PLAIN_TEXT_FORMAT.contains( m ) ? TEXT_PLAIN : m;

    // Create two groups: YAML files and plain text files.
    final var bins = paths
      .stream()
      .collect(
        groupingBy( path -> bin.apply( MediaType.valueFrom( path ) ) )
      );

    bins.putIfAbsent( TEXT_YAML, List.of( DEFINITION_DEFAULT.toString() ) );
    bins.putIfAbsent( TEXT_PLAIN, List.of( DOCUMENT_DEFAULT.toString() ) );

    final var result = new ArrayList<File>( paths.size() );

    // Ensure that the same types are listed together (keep insertion order).
    bins.forEach( ( mediaType, files ) -> result.addAll(
      files.stream().map( File::new ).collect( Collectors.toList() ) )
    );

    return result;
  }

  /**
   * Uses the given {@link TextDefinition} instance to update the
   * {@link #mResolvedMap}.
   *
   * @param editor A non-null, possibly empty definition editor.
   */
  private void resolve( final TextDefinition editor ) {
    assert editor != null;

    final var tokens = createDefinitionTokens();
    final var operator = new YamlSigilOperator( tokens );
    final var map = new HashMap<String, String>();

    editor.toMap().forEach( ( k, v ) -> map.put( operator.entoken( k ), v ) );

    mResolvedMap.clear();
    mResolvedMap.putAll( editor.interpolate( map, tokens ) );
  }

  /**
   * Force the active editor to update, which will cause the processor
   * to re-evaluate the interpolated definition map thereby updating the
   * preview pane.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void process( final TextEditor editor ) {
    // Ensure that these are run from within the Swing event dispatch thread
    // so that the text editor thread is immediately freed for caret movement.
    // This means that the preview will have a slight delay when catching up
    // to the caret position.
    invokeLater( () -> {
      mProcessors.getOrDefault( editor, IdentityProcessor.IDENTITY )
                 .apply( editor == null ? "" : editor.getText() );
      mHtmlPreview.scrollTo( CARET_ID );
    } );
  }

  /**
   * Lazily creates a {@link DetachableTabPane} configured to handle focus
   * requests by delegating to the selected tab's content. The tab pane is
   * associated with a given media type so that similar files can be grouped
   * together.
   *
   * @param mediaType The media type to associate with the tab pane.
   * @return An instance of {@link DetachableTabPane} that will handle
   * docking of tabs.
   */
  private DetachableTabPane obtainDetachableTabPane(
    final MediaType mediaType ) {
    return mTabPanes.computeIfAbsent(
      mediaType, ( mt ) -> createDetachableTabPane()
    );
  }

  /**
   * Creates an initialized {@link DetachableTabPane} instance.
   *
   * @return A new {@link DetachableTabPane} with all listeners configured.
   */
  private DetachableTabPane createDetachableTabPane() {
    final var tabPane = new DetachableTabPane();

    initStageOwnerFactory( tabPane );
    initTabListener( tabPane );
    initSelectionModelListener( tabPane );

    return tabPane;
  }

  /**
   * When any {@link DetachableTabPane} is detached from the main window,
   * the stage owner factory must be given its parent window, which will
   * own the child window. The parent window is the {@link MainPane}'s
   * {@link Scene}'s {@link Window} instance.
   *
   * <p>
   * This will derives the new title from the main window title, incrementing
   * the window count to help uniquely identify the child windows.
   * </p>
   *
   * @param tabPane A new {@link DetachableTabPane} to configure.
   */
  private void initStageOwnerFactory( final DetachableTabPane tabPane ) {
    tabPane.setStageOwnerFactory( ( stage ) -> {
      final var title = get(
        "Detach.tab.title",
        ((Stage) getWindow()).getTitle(), ++mWindowCount
      );
      stage.setTitle( title );
      return getScene().getWindow();
    } );
  }

  /**
   * Responsible for configuring the content of each {@link DetachableTab} when
   * it is added to the given {@link DetachableTabPane} instance.
   * <p>
   * For {@link TextEditor} contents, an instance of {@link ScrollEventHandler}
   * is initialized to perform synchronized scrolling between the editor and
   * its preview window. Additionally, the last tab in the tab pane's list of
   * tabs is given focus.
   * </p>
   * <p>
   * Note that multiple tabs can be added simultaneously.
   * </p>
   *
   * @param tabPane A new {@link DetachableTabPane} to configure.
   */
  private void initTabListener( final DetachableTabPane tabPane ) {
    tabPane.getTabs().addListener(
      ( final ListChangeListener.Change<? extends Tab> listener ) -> {
        while( listener.next() ) {
          if( listener.wasAdded() ) {
            final var tabs = listener.getAddedSubList();

            tabs.forEach( ( tab ) -> {
              final var node = tab.getContent();

              if( node instanceof TextEditor ) {
                initScrollEventListener( tab );
              }
            } );

            // Select and give focus to the last tab opened.
            final var index = tabs.size() - 1;
            if( index >= 0 ) {
              final var tab = tabs.get( index );
              tabPane.getSelectionModel().select( tab );
              tab.getContent().requestFocus();
            }
          }
        }
      }
    );
  }

  /**
   * Responsible for handling tab change events.
   *
   * @param tabPane A new {@link DetachableTabPane} to configure.
   */
  private void initSelectionModelListener( final DetachableTabPane tabPane ) {
    final var model = tabPane.getSelectionModel();

    model.selectedItemProperty().addListener( ( c, o, n ) -> {
      if( o != null && n == null ) {
        final var node = o.getContent();

        // If the last definition editor in the active pane was closed,
        // clear out the definitions then refresh the text editor.
        if( node instanceof TextDefinition ) {
          mActiveDefinitionEditor.set( createDefinitionEditor() );
        }
      }
      else if( n != null ) {
        final var node = n.getContent();

        if( node instanceof TextEditor ) {
          // Changing the active node will fire an event, which will
          // update the preview panel and grab focus.
          mActiveTextEditor.set( (TextEditor) node );
          runLater( node::requestFocus );
        }
        else if( node instanceof TextDefinition ) {
          mActiveDefinitionEditor.set( (DefinitionEditor) node );
        }
      }
    } );
  }

  /**
   * Synchronizes scrollbar positions between the given {@link Tab} that
   * contains an instance of {@link TextEditor} and {@link HtmlPreview} pane.
   *
   * @param tab The container for an instance of {@link TextEditor}.
   */
  private void initScrollEventListener( final Tab tab ) {
    final var editor = (TextEditor) tab.getContent();
    final var scrollPane = editor.getScrollPane();
    final var scrollBar = mHtmlPreview.getVerticalScrollBar();
    final var handler = new ScrollEventHandler( scrollPane, scrollBar );
    handler.enabledProperty().bind( tab.selectedProperty() );
  }

  private void addTabPane( final int index, final DetachableTabPane tabPane ) {
    final var items = getItems();
    if( !items.contains( tabPane ) ) {
      items.add( index, tabPane );
    }
  }

  private void addTabPane( final DetachableTabPane tabPane ) {
    addTabPane( getItems().size(), tabPane );
  }

  public ProcessorContext createProcessorContext() {
    return createProcessorContext( NONE );
  }

  public ProcessorContext createProcessorContext( final ExportFormat format ) {
    final var editor = getActiveTextEditor();
    return createProcessorContext(
      editor.getPath(), editor.getCaret(), format );
  }

  /**
   * @param path  Used by {@link ProcessorFactory} to determine
   *              {@link Processor} type to create based on file type.
   * @param caret Used by {@link CaretExtension} to add ID attribute into
   *              preview document for scrollbar synchronization.
   * @return A new {@link ProcessorContext} to use when creating an instance of
   * {@link Processor}.
   */
  private ProcessorContext createProcessorContext(
    final Path path, final Caret caret, final ExportFormat format ) {
    return new ProcessorContext(
      mHtmlPreview, mResolvedMap, path, caret, format, mWorkspace
    );
  }

  private TextResource createTextResource( final File file ) {
    // TODO: Create PlainTextEditor that's returned by default.
    return MediaType.valueFrom( file ) == TEXT_YAML
      ? createDefinitionEditor( file )
      : createMarkdownEditor( file );
  }

  /**
   * Creates an instance of {@link MarkdownEditor} that listens for both
   * caret change events and text change events. Text change events must
   * take priority over caret change events because it's possible to change
   * the text without moving the caret (e.g., delete selected text).
   *
   * @param file The file containing contents for the text editor.
   * @return A non-null text editor.
   */
  private TextResource createMarkdownEditor( final File file ) {
    final var path = file.toPath();
    final var editor = new MarkdownEditor( file, getWorkspace() );
    final var caret = editor.getCaret();
    final var context = createProcessorContext( path, caret, NONE );

    mProcessors.computeIfAbsent( editor, p -> createProcessors( context ) );

    editor.addDirtyListener( ( c, o, n ) -> {
      if( n ) {
        // Reset the status to OK after changing the text.
        clue();

        // Processing the text will update the status bar.
        process( getActiveTextEditor() );
      }
    } );

    editor.addEventListener(
      keyPressed( SPACE, CONTROL_DOWN ), this::autoinsert
    );

    // Set the active editor, which refreshes the preview panel.
    mActiveTextEditor.set( editor );

    return editor;
  }

  /**
   * Delegates to {@link #autoinsert()}.
   *
   * @param event Ignored.
   */
  private void autoinsert( final KeyEvent event ) {
    autoinsert();
  }

  /**
   * Finds a node that matches the word at the caret, then inserts the
   * corresponding definition. The definition token delimiters depend on
   * the type of file being edited.
   */
  public void autoinsert() {
    final var definitions = getActiveTextDefinition();
    final var editor = getActiveTextEditor();
    final var mediaType = editor.getMediaType();
    final var operator = getSigilOperator( mediaType );

    DefinitionNameInjector.autoinsert( editor, definitions, operator );
  }

  private TextDefinition createDefinitionEditor() {
    return createDefinitionEditor( DEFINITION_DEFAULT );
  }

  private TextDefinition createDefinitionEditor( final File file ) {
    final var editor = new DefinitionEditor( file, createTreeTransformer() );
    editor.addTreeChangeHandler( mTreeHandler );
    return editor;
  }

  private TreeTransformer createTreeTransformer() {
    return new YamlTreeTransformer();
  }

  private Tooltip createTooltip( final File file ) {
    final var path = file.toPath();
    final var tooltip = new Tooltip( path.toString() );

    tooltip.setShowDelay( millis( 200 ) );
    return tooltip;
  }

  public TextEditor getActiveTextEditor() {
    return mActiveTextEditor.get();
  }

  public ReadOnlyObjectProperty<TextEditor> activeTextEditorProperty() {
    return mActiveTextEditor;
  }

  public TextDefinition getActiveTextDefinition() {
    return mActiveDefinitionEditor.get();
  }

  public Window getWindow() {
    return getScene().getWindow();
  }

  public Workspace getWorkspace() {
    return mWorkspace;
  }

  /**
   * Returns the sigil operator for the given {@link MediaType}.
   *
   * @param mediaType The type of file being edited.
   */
  private SigilOperator getSigilOperator( final MediaType mediaType ) {
    final var operator = new YamlSigilOperator( createDefinitionTokens() );

    return switch( mediaType ) {
      case TEXT_R_MARKDOWN, TEXT_R_XML -> new RSigilOperator(
        createRTokens(), operator );
      default -> operator;
    };
  }

  /**
   * Returns the set of file names opened in the application. The names must
   * be converted to {@link File} objects.
   *
   * @return A {@link Set} of file names.
   */
  private SetProperty<String> getRecentFiles() {
    return getWorkspace().setsProperty( KEY_UI_FILES_PATH );
  }

  private StringProperty stringProperty( final Key key ) {
    return getWorkspace().stringProperty( key );
  }

  private Tokens createRTokens() {
    return createTokens( KEY_R_DELIM_BEGAN, KEY_R_DELIM_ENDED );
  }

  private Tokens createDefinitionTokens() {
    return createTokens( KEY_DEF_DELIM_BEGAN, KEY_DEF_DELIM_ENDED );
  }

  private Tokens createTokens( final Key began, final Key ended ) {
    return new Tokens( stringProperty( began ), stringProperty( ended ) );
  }
}
