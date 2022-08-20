/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.TextResource;
import com.keenwrite.editors.common.ScrollEventHandler;
import com.keenwrite.editors.common.VariableNameInjector;
import com.keenwrite.editors.definition.DefinitionEditor;
import com.keenwrite.editors.definition.TreeTransformer;
import com.keenwrite.editors.definition.yaml.YamlTreeTransformer;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.events.*;
import com.keenwrite.io.MediaType;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.HtmlPreviewProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorFactory;
import com.keenwrite.processors.r.Engine;
import com.keenwrite.processors.r.RBootstrapController;
import com.keenwrite.service.events.Notifier;
import com.keenwrite.ui.explorer.FilePickerFactory;
import com.keenwrite.ui.heuristics.DocumentStatistics;
import com.keenwrite.ui.outline.DocumentOutline;
import com.keenwrite.util.GenericBuilder;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.Launcher.terminate;
import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.constants.GraphicsConstants.ICON_DIALOG_NODE;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.preferences.AppKeys.*;
import static com.keenwrite.processors.IdentityProcessor.IDENTITY;
import static com.keenwrite.processors.ProcessorContext.Mutator;
import static com.keenwrite.processors.ProcessorContext.builder;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.groupingBy;
import static javafx.application.Platform.runLater;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonType.*;
import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;
import static javafx.scene.input.KeyCode.SPACE;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.util.Duration.millis;
import static javax.swing.SwingUtilities.invokeLater;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;

/**
 * Responsible for wiring together the main application components for a
 * particular {@link Workspace} (project). These include the definition views,
 * text editors, and preview pane along with any corresponding controllers.
 */
public final class MainPane extends SplitPane {

  private static final ExecutorService sExecutor = newFixedThreadPool( 1 );
  private static final Notifier sNotifier = Services.load( Notifier.class );

  /**
   * Used when opening files to determine how each file should be binned and
   * therefore what tab pane to be opened within.
   */
  private static final Set<MediaType> PLAIN_TEXT_FORMAT = Set.of(
    TEXT_MARKDOWN, TEXT_R_MARKDOWN, UNDEFINED
  );

  private final ScheduledExecutorService mSaver = newScheduledThreadPool( 1 );
  private final AtomicReference<ScheduledFuture<?>> mSaveTask =
    new AtomicReference<>();

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<TextResource, Processor<String>> mProcessors =
    new HashMap<>();

  private final Workspace mWorkspace;

  /**
   * Groups similar file type tabs together.
   */
  private final List<TabPane> mTabPanes = new ArrayList<>();

  /**
   * Renders the actively selected plain text editor tab.
   */
  private final HtmlPreview mPreview;

  /**
   * Provides an interactive document outline.
   */
  private final DocumentOutline mOutline = new DocumentOutline();

  /**
   * Changing the active editor fires the value changed event. This allows
   * refreshes to happen when external definitions are modified and need to
   * trigger the processing chain.
   */
  private final ObjectProperty<TextEditor> mTextEditor =
    createActiveTextEditor();

  /**
   * Changing the active definition editor fires the value changed event. This
   * allows refreshes to happen when external definitions are modified and need
   * to trigger the processing chain.
   */
  private final ObjectProperty<TextDefinition> mDefinitionEditor;

  /**
   * Called when the definition data is changed.
   */
  private final EventHandler<TreeModificationEvent<Event>> mTreeHandler =
    event -> {
      process( getTextEditor() );
      save( getTextDefinition() );
    };

  /**
   * Tracks the number of detached tab panels opened into their own windows,
   * which allows unique identification of subordinate windows by their title.
   * It is doubtful more than 128 windows, much less 256, will be created.
   */
  private byte mWindowCount;

  private final VariableNameInjector mVariableNameInjector;

  private final RBootstrapController mRBootstrapController;

  private final DocumentStatistics mStatistics;

  /**
   * Adds all content panels to the main user interface. This will load the
   * configuration settings from the workspace to reproduce the settings from
   * a previous session.
   */
  public MainPane( final Workspace workspace ) {
    mWorkspace = workspace;
    mPreview = new HtmlPreview( workspace );
    mStatistics = new DocumentStatistics( workspace );
    mTextEditor.set( new MarkdownEditor( workspace ) );
    mDefinitionEditor = createActiveDefinitionEditor( mTextEditor );
    mVariableNameInjector = new VariableNameInjector( mWorkspace );
    mRBootstrapController = new RBootstrapController(
      mWorkspace, this::getDefinitions );

    open( collect( getRecentFiles() ) );
    viewPreview();
    setDividerPositions( calculateDividerPositions() );

    // Once the main scene's window regains focus, update the active definition
    // editor to the currently selected tab.
    runLater( () -> getWindow().setOnCloseRequest( event -> {
      // Order matters: Open file names must be persisted before closing all.
      mWorkspace.save();

      if( closeAll() ) {
        Platform.exit();
        terminate( 0 );
      }

      event.consume();
    } ) );

    register( this );
    initAutosave( workspace );
  }

  @Subscribe
  public void handle( final TextEditorFocusEvent event ) {
    mTextEditor.set( event.get() );
  }

  @Subscribe
  public void handle( final TextDefinitionFocusEvent event ) {
    mDefinitionEditor.set( event.get() );
  }

  /**
   * Typically called when a file name is clicked in the preview panel.
   *
   * @param event The event to process, must contain a valid file reference.
   */
  @Subscribe
  public void handle( final FileOpenEvent event ) {
    final File eventFile;
    final var eventUri = event.getUri();

    if( eventUri.isAbsolute() ) {
      eventFile = new File( eventUri.getPath() );
    }
    else {
      final var activeFile = getTextEditor().getFile();
      final var parent = activeFile.getParentFile();

      if( parent == null ) {
        clue( new FileNotFoundException( eventUri.getPath() ) );
        return;
      }
      else {
        final var parentPath = parent.getAbsolutePath();
        eventFile = Path.of( parentPath, eventUri.getPath() ).toFile();
      }
    }

    runLater( () -> open( eventFile ) );
  }

  @Subscribe
  public void handle( final CaretNavigationEvent event ) {
    runLater( () -> {
      final var textArea = getTextEditor().getTextArea();
      textArea.moveTo( event.getOffset() );
      textArea.requestFollowCaret();
      textArea.requestFocus();
    } );
  }

  @Subscribe
  @SuppressWarnings( "unused" )
  public void handle( final ExportFailedEvent event ) {
    final var os = getProperty( "os.name" );
    final var arch = getProperty( "os.arch" ).toLowerCase();
    final var bits = getProperty( "sun.arch.data.model" );

    final var title = Messages.get( "Alert.typesetter.missing.title" );
    final var header = Messages.get( "Alert.typesetter.missing.header" );
    final var version = Messages.get(
      "Alert.typesetter.missing.version",
      os,
      arch
        .replaceAll( "amd.*|i.*|x86.*", "X86" )
        .replaceAll( "mips.*", "MIPS" )
        .replaceAll( "armv.*", "ARM" ),
      bits );
    final var text = Messages.get( "Alert.typesetter.missing.installer.text" );

    // Download and install ConTeXt for {0} {1} {2}-bit
    final var content = format( "%s %s", text, version );
    final var flowPane = new FlowPane();
    final var link = new Hyperlink( text );
    final var label = new Label( version );
    flowPane.getChildren().addAll( link, label );

    final var alert = new Alert( ERROR, content, OK );
    alert.setTitle( title );
    alert.setHeaderText( header );
    alert.getDialogPane().contentProperty().set( flowPane );
    alert.setGraphic( ICON_DIALOG_NODE );

    link.setOnAction( ( e ) -> {
      alert.close();
      final var url = Messages.get( "Alert.typesetter.missing.installer.url" );
      runLater( () -> HyperlinkOpenEvent.fire( url ) );
    } );

    alert.showAndWait();
  }

  @Subscribe
  public void handle( final InsertDefinitionEvent<String> event ) {
    final var leaf = event.getLeaf();
    final var editor = mTextEditor.get();

    System.out.println( "INJECT: " + leaf.toPath() );

    mVariableNameInjector.insert( editor, leaf );
  }

  private void initAutosave( final Workspace workspace ) {
    final var rate = workspace.integerProperty( KEY_EDITOR_AUTOSAVE );

    rate.addListener(
      ( c, o, n ) -> {
        final var taskRef = mSaveTask.get();

        // Prevent multiple autosaves from running.
        if( taskRef != null ) {
          taskRef.cancel( false );
        }

        initAutosave( rate );
      }
    );

    // Start the save listener (avoids duplicating some code).
    initAutosave( rate );
  }

  private void initAutosave( final IntegerProperty rate ) {
    mSaveTask.set(
      mSaver.scheduleAtFixedRate(
        () -> {
          if( getTextEditor().isModified() ) {
            // Ensure the modified indicator is cleared by running on EDT.
            runLater( this::save );
          }
        }, 0, rate.intValue(), SECONDS
      )
    );
  }

  /**
   * TODO: Load divider positions from exported settings, see
   *   {@link #collect(SetProperty)} comment.
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
   * This will exit early if the given file is not a regular file (i.e., a
   * directory).
   *
   * @param inputFile The file to open.
   */
  private void open( final File inputFile ) {
    // Prevent opening directories (a non-existent "untitled.md" is fine).
    if( !inputFile.isFile() && inputFile.exists() ) {
      return;
    }

    final var tab = createTab( inputFile );
    final var node = tab.getContent();
    final var mediaType = MediaType.valueFrom( inputFile );
    final var tabPane = obtainTabPane( mediaType );

    tab.setTooltip( createTooltip( inputFile ) );
    tabPane.setFocusTraversable( false );
    tabPane.setTabClosingPolicy( ALL_TABS );
    tabPane.getTabs().add( tab );

    // Attach the tab scene factory for new tab panes.
    if( !getItems().contains( tabPane ) ) {
      addTabPane(
        node instanceof TextDefinition ? 0 : getItems().size(), tabPane
      );
    }

    if( inputFile.isFile() ) {
      getRecentFiles().add( inputFile.getAbsolutePath() );
    }
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
      tp -> tp.getTabs().forEach( tab -> {
        final var node = tab.getContent();

        if( node instanceof final TextEditor editor ) {
          save( editor );
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
    save( getTextEditor() );
  }

  /**
   * Saves the active {@link TextEditor} under a new name.
   *
   * @param files The new active editor {@link File} reference, must contain
   *              at least one element.
   */
  public void saveAs( final List<File> files ) {
    assert files != null;
    assert !files.isEmpty();
    final var editor = getTextEditor();
    final var tab = getTab( editor );
    final var file = files.get( 0 );

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

    for( final var tabPane : mTabPanes ) {
      final var tabIterator = tabPane.getTabs().iterator();

      while( tabIterator.hasNext() ) {
        final var tab = tabIterator.next();
        final var resource = tab.getContent();

        // The definition panes auto-save, so being specific here prevents
        // closing the definitions in the situation where the user wants to
        // continue editing (i.e., possibly save unsaved work).
        if( !(resource instanceof TextEditor) ) {
          continue;
        }

        if( canClose( (TextEditor) resource ) ) {
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
    assert tab != null;

    final var handler = tab.getOnClosed();

    if( handler != null ) {
      handler.handle( new ActionEvent() );
    }
  }

  /**
   * Closes the active tab; delegates to {@link #canClose(TextResource)}.
   */
  public void close() {
    final var editor = getTextEditor();

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
        close( tab );
        tab.getTabPane().getTabs().remove( tab );
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
        mPreview.setBaseUri( n.getPath() );
        process( n );
      }
    } );

    return editor;
  }

  /**
   * Adds the HTML preview tab to its own, singular tab pane.
   */
  public void viewPreview() {
    viewTab( mPreview, TEXT_HTML, "Pane.preview.title" );
  }

  /**
   * Adds the document outline tab to its own, singular tab pane.
   */
  public void viewOutline() {
    viewTab( mOutline, APP_DOCUMENT_OUTLINE, "Pane.outline.title" );
  }

  public void viewStatistics() {
    viewTab( mStatistics, APP_DOCUMENT_STATISTICS, "Pane.statistics.title" );
  }

  public void viewFiles() {
    try {
      final var factory = new FilePickerFactory( getWorkspace() );
      final var fileManager = factory.createModeless();
      viewTab( fileManager, APP_FILE_MANAGER, "Pane.files.title" );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  private void viewTab(
    final Node node, final MediaType mediaType, final String key ) {
    final var tabPane = obtainTabPane( mediaType );

    for( final var tab : tabPane.getTabs() ) {
      if( tab.getContent() == node ) {
        return;
      }
    }

    tabPane.getTabs().add( createTab( get( key ), node ) );
    addTabPane( tabPane );
  }

  public void viewRefresh() {
    mPreview.refresh();
    Engine.clear();
    mRBootstrapController.update();
  }

  /**
   * Returns the tab that contains the given {@link TextEditor}.
   *
   * @param editor The {@link TextEditor} instance to find amongst the tabs.
   * @return The first tab having content that matches the given tab.
   */
  private Optional<Tab> getTab( final TextResource editor ) {
    return mTabPanes.stream()
                    .flatMap( pane -> pane.getTabs().stream() )
                    .filter( tab -> editor.equals( tab.getContent() ) )
                    .findFirst();
  }

  /**
   * Creates a new {@link DefinitionEditor} wrapped in a listener that
   * is used to detect when the active {@link DefinitionEditor} has changed.
   * Upon changing, the variables are interpolated and the active text editor
   * is refreshed.
   *
   * @param textEditor Text editor to update with the revised resolved map.
   * @return A newly configured property that represents the active
   * {@link DefinitionEditor}, never null.
   */
  private ObjectProperty<TextDefinition> createActiveDefinitionEditor(
    final ObjectProperty<TextEditor> textEditor ) {
    final var defEditor = new SimpleObjectProperty<>(
      createDefinitionEditor()
    );

    defEditor.addListener( ( c, o, n ) -> {
      final var editor = textEditor.get();

      if( editor.isMediaType( TEXT_R_MARKDOWN ) ) {
        // Initialize R before the editor is added.
        mRBootstrapController.update();
      }

      process( editor );
    } );

    return defEditor;
  }

  private Tab createTab( final String filename, final Node node ) {
    return new DetachableTab( filename, node );
  }

  private Tab createTab( final File file ) {
    final var r = createTextResource( file );
    final var tab = createTab( r.getFilename(), r.getNode() );

    r.modifiedProperty().addListener(
      ( c, o, n ) -> tab.setText( r.getFilename() + (n ? "*" : "") )
    );

    // This is called when either the tab is closed by the user clicking on
    // the tab's close icon or when closing (all) from the file menu.
    tab.setOnClosed(
      ( __ ) -> getRecentFiles().remove( file.getAbsolutePath() )
    );

    // When closing a tab, give focus to the newly revealed tab.
    tab.selectedProperty().addListener( ( c, o, n ) -> {
      if( n != null && n ) {
        final var pane = tab.getTabPane();

        if( pane != null ) {
          pane.requestFocus();
        }
      }
    } );

    tab.tabPaneProperty().addListener( ( cPane, oPane, nPane ) -> {
      if( nPane != null ) {
        nPane.focusedProperty().addListener( ( c, o, n ) -> {
          if( n != null && n ) {
            final var selected = nPane.getSelectionModel().getSelectedItem();
            final var node = selected.getContent();
            node.requestFocus();
          }
        } );
      }
    } );

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
  private List<File> collect( final SetProperty<String> paths ) {
    // Treat all files destined for the text editor as plain text documents
    // so that they are added to the same pane. Grouping by TEXT_PLAIN is a
    // bit arbitrary, but means explicitly capturing TEXT_PLAIN isn't needed.
    final Function<MediaType, MediaType> bin =
      m -> PLAIN_TEXT_FORMAT.contains( m ) ? TEXT_PLAIN : m;

    // Create two groups: YAML files and plain text files. The order that
    // the elements are listed in the enumeration for media types determines
    // what files are loaded first. Variable definitions come before all other
    // plain text documents.
    final var bins = paths
      .stream()
      .collect(
        groupingBy(
          path -> bin.apply( MediaType.fromFilename( path ) ),
          () -> new TreeMap<>( Enum::compareTo ),
          Collectors.toList()
        )
      );

    bins.putIfAbsent( TEXT_YAML, List.of( DEFINITION_DEFAULT.toString() ) );
    bins.putIfAbsent( TEXT_PLAIN, List.of( DOCUMENT_DEFAULT.toString() ) );

    final var result = new LinkedList<File>();

    // Ensure that the same types are listed together (keep insertion order).
    bins.forEach( ( mediaType, files ) -> result.addAll(
      files.stream().map( File::new ).toList() )
    );

    return result;
  }

  /**
   * Force the active editor to update, which will cause the processor
   * to re-evaluate the interpolated definition map thereby updating the
   * preview pane.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void process( final TextEditor editor ) {
    // Ensure processing does not run on the JavaFX thread, which frees the
    // text editor immediately for caret movement. The preview will have a
    // slight delay when catching up to the caret position.
    final var task = new Task<Void>() {
      @Override
      public Void call() {
        try {
          final var p = mProcessors.getOrDefault( editor, IDENTITY );
          p.apply( editor == null ? "" : editor.getText() );
        } catch( final Exception ex ) {
          clue( ex );
        }

        return null;
      }
    };

    // TODO: Each time the editor successfully runs the processor the task is
    //   considered successful. Due to the rapid-fire nature of processing
    //   (e.g., keyboard navigation, fast typing), it isn't necessary to
    //   scroll each time.
    //   The algorithm:
    //   1. Peek at the oldest time.
    //   2. If the difference between the oldest time and current time exceeds
    //      250 milliseconds, then invoke the scrolling.
    //   3. Insert the current time into the circular queue.
    task.setOnSucceeded(
      e -> invokeLater( () -> mPreview.scrollTo( CARET_ID ) )
    );

    // Prevents multiple process requests from executing simultaneously (due
    // to having a restricted queue size).
    sExecutor.execute( task );
  }

  /**
   * Lazily creates a {@link TabPane} configured to listen for tab select
   * events. The tab pane is associated with a given media type so that
   * similar files can be grouped together.
   *
   * @param mediaType The media type to associate with the tab pane.
   * @return An instance of {@link TabPane} that will handle tab docking.
   */
  private TabPane obtainTabPane( final MediaType mediaType ) {
    for( final var pane : mTabPanes ) {
      for( final var tab : pane.getTabs() ) {
        final var node = tab.getContent();

        if( node instanceof TextResource r && r.supports( mediaType ) ) {
          return pane;
        }
      }
    }

    final var pane = createTabPane();
    mTabPanes.add( pane );
    return pane;
  }

  /**
   * Creates an initialized {@link TabPane} instance.
   *
   * @return A new {@link TabPane} with all listeners configured.
   */
  private TabPane createTabPane() {
    final var tabPane = new DetachableTabPane();

    initStageOwnerFactory( tabPane );
    initTabListener( tabPane );

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
   * @param tabPane A new {@link TabPane} to configure.
   */
  private void initTabListener( final TabPane tabPane ) {
    tabPane.getTabs().addListener(
      ( final ListChangeListener.Change<? extends Tab> listener ) -> {
        while( listener.next() ) {
          if( listener.wasAdded() ) {
            final var tabs = listener.getAddedSubList();

            tabs.forEach( tab -> {
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
   * Synchronizes scrollbar positions between the given {@link Tab} that
   * contains an instance of {@link TextEditor} and {@link HtmlPreview} pane.
   *
   * @param tab The container for an instance of {@link TextEditor}.
   */
  private void initScrollEventListener( final Tab tab ) {
    final var editor = (TextEditor) tab.getContent();
    final var scrollPane = editor.getScrollPane();
    final var scrollBar = mPreview.getVerticalScrollBar();
    final var handler = new ScrollEventHandler( scrollPane, scrollBar );

    handler.enabledProperty().bind( tab.selectedProperty() );
  }

  private void addTabPane( final int index, final TabPane tabPane ) {
    final var items = getItems();

    if( !items.contains( tabPane ) ) {
      items.add( index, tabPane );
    }
  }

  private void addTabPane( final TabPane tabPane ) {
    addTabPane( getItems().size(), tabPane );
  }

  private GenericBuilder<Mutator, ProcessorContext> createProcessorContextBuilder() {
    final var w = getWorkspace();

    return builder()
      .with( Mutator::setDefinitions, this::getDefinitions )
      .with( Mutator::setLocale, w::getLocale )
      .with( Mutator::setMetadata, w::getMetadata )
      .with( Mutator::setThemePath, w::getThemePath )
      .with( Mutator::setCaret,
             () -> getTextEditor().getCaret() )
      .with( Mutator::setImageDir,
             () -> w.getFile( KEY_IMAGES_DIR ) )
      .with( Mutator::setImageOrder,
             () -> w.getString( KEY_IMAGES_ORDER ) )
      .with( Mutator::setImageServer,
             () -> w.getString( KEY_IMAGES_SERVER ) )
      .with( Mutator::setSigilBegan,
             () -> w.getString( KEY_DEF_DELIM_BEGAN ) )
      .with( Mutator::setSigilEnded,
             () -> w.getString( KEY_DEF_DELIM_ENDED ) )
      .with( Mutator::setRScript,
             () -> w.getString( KEY_R_SCRIPT ) )
      .with( Mutator::setRWorkingDir,
             () -> w.getFile( KEY_R_DIR ).toPath() )
      .with( Mutator::setCurlQuotes,
             () -> w.getBoolean( KEY_TYPESET_TYPOGRAPHY_QUOTES ) )
      .with( Mutator::setAutoClean,
             () -> w.getBoolean( KEY_TYPESET_CONTEXT_CLEAN ) );
  }

  public ProcessorContext createProcessorContext() {
    return createProcessorContext( null, NONE );
  }

  /**
   * @param outputPath Used when exporting to a PDF file (binary).
   * @param format     Used when processors export to a new text format.
   * @return A new {@link ProcessorContext} to use when creating an instance of
   * {@link Processor}.
   */
  public ProcessorContext createProcessorContext(
    final Path outputPath, final ExportFormat format ) {
    final var textEditor = getTextEditor();
    final var inputPath = textEditor.getPath();

    return createProcessorContextBuilder()
      .with( Mutator::setInputPath, inputPath )
      .with( Mutator::setOutputPath, outputPath )
      .with( Mutator::setExportFormat, format )
      .build();
  }

  /**
   * @param inputPath Used by {@link ProcessorFactory} to determine
   *                  {@link Processor} type to create based on file type.
   * @return A new {@link ProcessorContext} to use when creating an instance of
   * {@link Processor}.
   */
  private ProcessorContext createProcessorContext( final Path inputPath ) {
    return createProcessorContextBuilder()
      .with( Mutator::setInputPath, inputPath )
      .with( Mutator::setExportFormat, NONE )
      .build();
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
   * @param inputFile The file containing contents for the text editor.
   * @return A non-null text editor.
   */
  private TextResource createMarkdownEditor( final File inputFile ) {
    final var editor = new MarkdownEditor( inputFile, getWorkspace() );

    mProcessors.computeIfAbsent(
      editor, p -> createProcessors(
        createProcessorContext( inputFile.toPath() ),
        createHtmlPreviewProcessor()
      )
    );

    // Listener for editor modifications or caret position changes.
    editor.addDirtyListener( ( c, o, n ) -> {
      if( n ) {
        // Reset the status bar after changing the text.
        clue();

        // Processing the text may update the status bar.
        process( getTextEditor() );

        // Update the caret position in the status bar.
        CaretMovedEvent.fire( editor.getCaret() );
      }
    } );

    editor.addEventListener(
      keyPressed( SPACE, CONTROL_DOWN ), this::autoinsert
    );

    // Set the active editor, which refreshes the preview panel.
    mTextEditor.set( editor );

    return editor;
  }

  /**
   * Creates a {@link Processor} capable of rendering an HTML document onto
   * a GUI widget.
   *
   * @return The {@link Processor} for rendering an HTML document.
   */
  private Processor<String> createHtmlPreviewProcessor() {
    return new HtmlPreviewProcessor( getPreview() );
  }

  /**
   * Delegates to {@link #autoinsert()}.
   *
   * @param keyEvent Ignored.
   */
  private void autoinsert( final KeyEvent keyEvent ) {
    autoinsert();
  }

  /**
   * Finds a node that matches the word at the caret, then inserts the
   * corresponding definition. The definition token delimiters depend on
   * the type of file being edited.
   */
  public void autoinsert() {
    mVariableNameInjector.autoinsert( getTextEditor(), getTextDefinition() );
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

  public HtmlPreview getPreview() {
    return mPreview;
  }

  /**
   * Returns the active text editor.
   *
   * @return The text editor that currently has focus.
   */
  public TextEditor getTextEditor() {
    return mTextEditor.get();
  }

  /**
   * Returns the active text editor property.
   *
   * @return The property container for the active text editor.
   */
  public ReadOnlyObjectProperty<TextEditor> textEditorProperty() {
    return mTextEditor;
  }

  /**
   * Returns the active text definition editor.
   *
   * @return The property container for the active definition editor.
   */
  public TextDefinition getTextDefinition() {
    return mDefinitionEditor.get();
  }

  /**
   * Returns the active variable definitions, without any interpolation.
   * Interpolation is a responsibility of {@link Processor} instances.
   *
   * @return The key-value pairs, not interpolated.
   */
  private Map<String, String> getDefinitions() {
    return getTextDefinition().getDefinitions();
  }

  public Window getWindow() {
    return getScene().getWindow();
  }

  public Workspace getWorkspace() {
    return mWorkspace;
  }

  /**
   * Returns the set of file names opened in the application. The names must
   * be converted to {@link File} objects.
   *
   * @return A {@link Set} of file names.
   */
  private <E> SetProperty<E> getRecentFiles() {
    return getWorkspace().setsProperty( KEY_UI_RECENT_OPEN_PATH );
  }
}
