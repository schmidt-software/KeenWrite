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
import com.keenwrite.processors.markdown.CaretPosition;
import com.keenwrite.ui.actions.FileChooserCommand;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem.TreeModificationEvent;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.definition.MapInterpolator.interpolate;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
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
   * Changing the active editor, even back to itself, will always fire the
   * value changed event. This allows refreshes to happen when external
   * definitions are modified and need to trigger the processing chain.
   */
  private final ObjectProperty<TextEditor> mActiveTextEditor =
      createActiveTextEditorListener();

  /**
   * Changing the active definition editor, even back to itself, will always
   * fire the value changed event. This allows refreshes to happen when external
   * definitions are modified and need to trigger the processing chain.
   */
  private final ObjectProperty<TextDefinition> mActiveDefinitionEditor =
      createActiveDefinitionEditorListener( mActiveTextEditor );

  /**
   * Responsible for creating a new scene when a tab is detached into
   * its own window frame.
   */
  private final DefinitionTabSceneFactory mDefinitionTabSceneFactory =
      createDefinitionTabSceneFactory( mActiveDefinitionEditor );

  /**
   * Called when the definition data is changed.
   * <p>
   * TODO: Export the YAML file on change.
   */
  private final EventHandler<TreeModificationEvent<Event>> mTreeHandler =
      event -> {
        refreshResolvedMap( mActiveDefinitionEditor.get() );
        refresh( mActiveTextEditor );
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
  }

  /**
   * Opens files selected by the user into the application.
   */
  public void open() {
    open( createFileChooser().openFiles() );
  }

  /**
   * Opens all the files into the application, provided the paths are unique.
   * This may only be called for any type of files that a user can edit
   * (i.e., update and persist), such as definitions and text files.
   *
   * @param files The list of files to open.
   */
  private void open( final List<File> files ) {
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

  private ObjectProperty<TextDefinition> createActiveDefinitionEditorListener(
      final ObjectProperty<TextEditor> editor ) {
    final var definitions = new SimpleObjectProperty<TextDefinition>();
    definitions.addListener( ( c, o, n ) -> {
      if( n == null ) {
        clearResolvedMap();
      }
      else {
        refreshResolvedMap( n );
      }

      refresh( editor );
    } );

    return definitions;
  }

  private ObjectProperty<TextEditor> createActiveTextEditorListener() {
    final var editor = new SimpleObjectProperty<TextEditor>();
    editor.addListener( ( c, o, n ) -> {
      if( n != null ) {
        n.getNode().requestFocus();
        refresh( n );
      }
    } );

    return editor;
  }

  private DefinitionTabSceneFactory createDefinitionTabSceneFactory(
      final ObjectProperty<TextDefinition> activeDefinitionEditor ) {
    return new DefinitionTabSceneFactory( ( t ) -> {
      var node = t.getContent();
      if( node instanceof TextDefinition ) {
        activeDefinitionEditor.set( (DefinitionEditor) node );
      }
    } );
  }

  private DetachableTab createTab( final File file ) {
    final var controller = createController( file );
    return new DetachableTab( controller.getFilename(), controller.getView() );
  }

  /**
   * Removes all definition values from the resolved map.
   */
  private void clearResolvedMap() {
    mResolvedMap.clear();
  }

  /**
   * Uses the given {@link TextDefinition} instance to update the resolved
   * map.
   *
   * @param editor A non-null, possibly empty definition editor.
   */
  private void refreshResolvedMap( final TextDefinition editor ) {
    assert editor != null;

    clearResolvedMap();
    mResolvedMap.putAll( interpolate( new HashMap<>( editor.toMap() ) ) );
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
   * Re-run the processor for the selected node so that the rendered view of
   * the document contents are updated.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void refresh( final ObjectProperty<TextEditor> editor ) {
    assert editor != null;
    refresh( editor.get() );
  }

  /**
   * Force the active editor to update, which will cause the processor
   * to re-evaluate the interpolated definition map thereby updating the
   * preview pane.
   *
   * @param editor Contains the source document to update in the preview pane.
   */
  private void refresh( final TextEditor editor ) {
    if( editor != null ) {
      mProcessors.getOrDefault( editor, IdentityProcessor.INSTANCE )
                 .apply( editor.getText() );
    }
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
          final var model = tabPane.getSelectionModel();

          model.selectedItemProperty().addListener( ( c, o, n ) -> {
            // If the last definition editor in the active pane was closed,
            // clear out the definitions then refresh the text editor.
            if( o != null && n == null ) {
              final var node = o.getContent();

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

  private void addTabPane( final int index, final DetachableTabPane tabPane ) {
    getItems().add( index, tabPane );
  }

  private void addTabPane( final DetachableTabPane tabPane ) {
    addTabPane( getItems().size(), tabPane );
  }

  private EditorController createController(
      final File file ) {
    final var view = createView( file.getMediaType() );

    if( view instanceof TextEditor ) {
      final var editor = (TextEditor) view;
      final var path = file.toPath();
      final var caret = view.createCaretPosition();
      final var context = createProcessorContext( path, caret );

      // THe active editor must be set for the preview panel to refresh.
      mActiveTextEditor.set( editor );

      // TODO: Change base URI when the text editor tab changes
      mHtmlPreview.setBaseUri( path );
      mProcessors.computeIfAbsent( editor, p -> createProcessors( context ) );

      // When the caret position changes, synchronize with the preview.
      context.getCaretPosition().textOffsetProperty().addListener(
          ( c, o, n ) -> refresh( mActiveTextEditor )
      );
    }

    return new EditorController( file.toPath(), view );
  }

  private ProcessorContext createProcessorContext(
      final Path path, final CaretPosition caretPosition ) {
    return new ProcessorContext(
        mHtmlPreview, mResolvedMap, path, caretPosition, NONE
    );
  }

  @SuppressWarnings({"RedundantCast", "unchecked", "RedundantSuppression"})
  private TextResource createView(
      final MediaType mediaType ) {
    return switch( mediaType ) {
      case TEXT_YAML -> createDefinitionEditor();
      case TEXT_MARKDOWN -> new MarkdownEditor();
      default -> new PlainTextEditor();
    };
  }

  private DefinitionEditor createDefinitionEditor() {
    final var editor = new DefinitionEditor( new YamlTreeTransformer() );

    editor.addTreeChangeHandler( mTreeHandler );

    return editor;
  }

  private Tooltip createTooltip( final File file ) {
    final var path = file.toPath();
    final var tooltip = new Tooltip( path.toString() );

    tooltip.setShowDelay( millis( 200 ) );
    return tooltip;
  }

  private FileChooserCommand createFileChooser() {
    return new FileChooserCommand( getWindow() );
  }

  private Window getWindow() {
    return getScene().getWindow();
  }
}
