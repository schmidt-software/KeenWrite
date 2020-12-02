/* Copyright 2020 White Magic Software, Ltd.
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

import com.keenwrite.definition.DefinitionEditor;
import com.keenwrite.definition.yaml.YamlTreeTransformer;
import com.keenwrite.editors.PlainTextEditor;
import com.keenwrite.editors.TextDefinition;
import com.keenwrite.editors.TextEditor;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.io.File;
import com.keenwrite.io.MediaType;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.IdentityProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.ProcessorFactory;
import com.keenwrite.processors.markdown.CaretExtension;
import com.keenwrite.processors.markdown.CaretPosition;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.*;

import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.Messages.get;
import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.StatusBarNotifier.getNotifier;
import static com.keenwrite.definition.MapInterpolator.interpolate;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static javafx.application.Platform.runLater;
import static javafx.util.Duration.millis;

/**
 * Responsible for wiring together the main application components for a
 * particular workspace (project). These include the definition views,
 * text editors, and preview pane along with any corresponding controllers.
 */
public class MainView extends SplitPane {

  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<TextResource, Processor<String>> mProcessors =
      new HashMap<>();

  /**
   * Groups similar file type tabs together.
   */
  private final Map<MediaType, DetachableTabPane> mTabPanes = new HashMap<>();

  /**
   * Stores definition names and values.
   */
  private final Map<String, String> mResolvedMap =
      new HashMap<>( DEFAULT_MAP_SIZE );

  /**
   * Renders the actively selected plain text editor tab.
   */
  private final HtmlPreview mHtmlPreview = new HtmlPreview();

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
   * <p>
   * TODO: Export the YAML file on change.
   */
  private final EventHandler<TreeModificationEvent<Event>> mTreeHandler =
      event -> {
        resolve( mActiveDefinitionEditor.get() );
        process( mActiveTextEditor );
        save( mActiveDefinitionEditor.get() );
      };

  /**
   * Adds all content panels to the main user interface. This will load the
   * configuration settings from the workspace to reproduce the settings from
   * a previous session.
   */
  public MainView() {
    final var workspace = new Workspace( "default" );
    open( bin( workspace.restoreFiles() ) );

    final var tabPane = obtainDetachableTabPane( TEXT_HTML );
    tabPane.addTab( "HTML", mHtmlPreview );
    addTabPane( tabPane );

    final var ratio = 100f / getItems().size() / 100;
    final var positions = getDividerPositions();

    for( int i = 0; i < positions.length; i++ ) {
      positions[ i ] = ratio * i;
    }

    // TODO: Load divider positions from exported settings, see bin() comment.
    setDividerPositions( positions );

    // Once the main scene's window regains focus, update the active definition
    // editor to the currently selected tab.
    runLater(
        () -> getWindow().focusedProperty().addListener( ( c, o, n ) -> {
          if( n != null && n ) {
            final var pane = mTabPanes.get( TEXT_YAML );
            final var model = pane.getSelectionModel();
            final var tab = model.getSelectedItem();

            if( tab != null ) {
              final var editor = (TextDefinition) tab.getContent();

              mActiveDefinitionEditor.set( editor );
            }
          }
        } )
    );
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
    final var mediaType = file.getMediaType();
    final var tab = createTab( file );
    final var node = tab.getContent();
    final var tabPane = obtainDetachableTabPane( mediaType );
    final var newTabPane = !getItems().contains( tabPane );

    tab.setTooltip( createTooltip( file ) );
    tabPane.getTabs().add( tab );

    if( newTabPane ) {
      var index = getItems().size();

      if( node instanceof TextDefinition ) {
        tabPane.setSceneFactory( mDefinitionTabSceneFactory::create );
        index = 0;
      }

      addTabPane( index, tabPane );
    }
  }

  /**
   * Opens a new text editor document using the default document file name.
   */
  public void newTextEditor() {
    open( DEFAULT_DOCUMENT );
  }

  /**
   * Opens a new definition editor document using the default definition
   * file name.
   */
  public void newDefinitionEditor() {
    open( DEFAULT_DEFINITION );
  }

  /**
   * Requests that the active {@link TextEditor} saves itself. Don't bother
   * checking if modified first because if the user swaps external media from
   * an external source (e.g., USB thumb drive), save should not second-guess
   * the user: save always re-saves. Also, it's less code.
   */
  public void save() {
    save( mActiveTextEditor.get() );
  }

  public void saveAs( final File file ) {
    assert file != null;
    final var editor = mActiveTextEditor.get();
    final var tab = getTab( editor );

    editor.rename( file );
    tab.ifPresent( t -> {
      t.setText( editor.getFilename() );
      t.setTooltip( createTooltip( file ) );
    } );

    save();
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
      alert(
          resource.getPath(),
          "TextResource.saveFailed.title",
          "TextResource.saveFailed.message",
          ex
      );
    }
  }

  private ObjectProperty<TextEditor> createActiveTextEditor() {
    final var editor = new SimpleObjectProperty<TextEditor>();
    editor.addListener( ( c, o, n ) -> {
      if( n != null ) {
        mHtmlPreview.setBaseUri( n.getPath() );
        n.getNode().requestFocus();
        process( n );
      }
    } );

    return editor;
  }

  /**
   * Returns the tab that contains the given {@link TextEditor}.
   *
   * @param editor The {@link TextEditor} instance to find amongst the tabs.
   * @return The first tab having content that matches the given tab.
   */
  private Optional<Tab> getTab( final TextEditor editor ) {
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
      process( editor );
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
    final var resource = createTextResource( file );
    return new DetachableTab( resource.getFilename(), resource.getNode() );
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
   * order that the corresponding panes are rendered in the UI. Each different
   * {@link MediaType} will be created in its own pane.
   * </p>
   *
   * @param files The files to bin by {@link MediaType}.
   * @return An in-order list of files, first by structured definition files,
   * then by plain text documents.
   */
  private List<File> bin( final List<File> files ) {
    final var map = new HashMap<MediaType, List<File>>();
    map.put( TEXT_YAML, new ArrayList<>() );
    map.put( TEXT_MARKDOWN, new ArrayList<>() );
    map.put( UNDEFINED, new ArrayList<>() );

    for( final var file : files ) {
      final var list = map.computeIfAbsent(
          file.getMediaType(), k -> new ArrayList<>()
      );

      list.add( file );
    }

    final var definitions = map.get( TEXT_YAML );
    final var documents = map.get( TEXT_MARKDOWN );
    final var undefined = map.get( UNDEFINED );

    if( definitions.isEmpty() ) {
      definitions.add( DEFAULT_DEFINITION );
    }

    if( documents.isEmpty() ) {
      documents.add( DEFAULT_DOCUMENT );
    }

    final var result = new ArrayList<File>( files.size() );
    result.addAll( definitions );
    result.addAll( documents );
    result.addAll( undefined );

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
    mResolvedMap.clear();
    mResolvedMap.putAll( interpolate( new HashMap<>( editor.toMap() ) ) );
  }

  /**
   * Re-run the processor for the selected node so that the rendered view of
   * the document contents are updated.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void process( final ObjectProperty<TextEditor> editor ) {
    assert editor != null;
    process( editor.get() );
  }

  /**
   * Force the active editor to update, which will cause the processor
   * to re-evaluate the interpolated definition map thereby updating the
   * preview pane.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void process( final TextEditor editor ) {
    mProcessors.getOrDefault( editor, IdentityProcessor.INSTANCE )
               .apply( editor == null ? "" : editor.getText() );
    mHtmlPreview.scrollTo( CARET_ID );
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
        mediaType, ( mt ) -> {
          final var tabPane = new DetachableTabPane();

          // Derive the new title from the main window title.
          tabPane.setStageOwnerFactory( ( stage ) -> {
            final var title = get(
                "Detach.tab.title",
                ((Stage) getWindow()).getTitle(), ++mWindowCount
            );
            stage.setTitle( title );
            return getScene().getWindow();
          } );

          // Multiple tabs can be added simultaneously.
          tabPane.getTabs().addListener(
              ( final ListChangeListener.Change<? extends Tab> change ) -> {
                while( change.next() ) {
                  if( change.wasAdded() ) {
                    final var tabs = change.getAddedSubList();

                    for( final var tab : tabs ) {
                      final var node = tab.getContent();

                      if( node instanceof TextEditor ) {
                        initScrollEventListener( tab );
                      }
                    }

                    // Select the last tab opened.
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
              }
              else if( node instanceof TextDefinition ) {
                mActiveDefinitionEditor.set( (DefinitionEditor) node );
              }
            }
          } );

          return tabPane;
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
    final var scrollBar = mHtmlPreview.getVerticalScrollBar();
    final var handler = new ScrollEventHandler( scrollPane, scrollBar );
    handler.enabledProperty().bind( tab.selectedProperty() );
  }

  private void addTabPane( final int index, final DetachableTabPane tabPane ) {
    getItems().add( index, tabPane );
  }

  private void addTabPane( final DetachableTabPane tabPane ) {
    addTabPane( getItems().size(), tabPane );
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
      final Path path, final CaretPosition caret ) {
    return new ProcessorContext(
        mHtmlPreview, mResolvedMap, path, caret, NONE
    );
  }

  @SuppressWarnings({"RedundantCast", "unchecked", "RedundantSuppression"})
  private TextResource createTextResource( final File file ) {
    final var mediaType = file.getMediaType();

    return switch( mediaType ) {
      case TEXT_MARKDOWN -> createMarkdownEditor( file );
      case TEXT_YAML -> createDefinitionEditor( file );
      default -> new PlainTextEditor( file );
    };
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
    final var editor = new MarkdownEditor( file );
    final var caret = editor.createCaretPosition();
    final var context = createProcessorContext( path, caret );

    mProcessors.computeIfAbsent( editor, p -> createProcessors( context ) );

    editor.addDirtyListener( ( c, o, n ) -> {
      if( n ) {
        process( mActiveTextEditor );
      }
    } );

    // Set the active editor, which refreshes the preview panel.
    mActiveTextEditor.set( editor );

    return editor;
  }

  private TextDefinition createDefinitionEditor() {
    return createDefinitionEditor( DEFAULT_DEFINITION );
  }

  private TextDefinition createDefinitionEditor( final File file ) {
    final var editor = new DefinitionEditor( file, new YamlTreeTransformer() );

    editor.addTreeChangeHandler( mTreeHandler );

    return editor;
  }

  private Tooltip createTooltip( final File file ) {
    final var path = file.toPath();
    final var tooltip = new Tooltip( path.toString() );

    tooltip.setShowDelay( millis( 200 ) );
    return tooltip;
  }

  public Window getWindow() {
    return getScene().getWindow();
  }

  /**
   * Creates an alert dialog and waits for it to close.
   *
   * @param titleKey   Resource bundle key for the alert dialog title.
   * @param messageKey Resource bundle key for the alert dialog message.
   * @param ex         The unexpected happening.
   */
  @SuppressWarnings("SameParameterValue")
  private void alert(
      final Path path,
      final String titleKey,
      final String messageKey,
      final Exception ex ) {
    final var service = getNotifier();
    final var message = service.createNotification(
        get( titleKey ), get( messageKey ), path, ex.getMessage()
    );

    service.createError( getWindow(), message ).showAndWait();
  }
}
