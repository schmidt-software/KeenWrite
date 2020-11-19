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
import com.keenwrite.definition.yaml.YamlTreeAdapter;
import com.keenwrite.editors.PlainTextEditor;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.io.File;
import com.keenwrite.io.MediaType;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.CaretPosition;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;

import java.nio.file.Path;
import java.util.*;

import static com.keenwrite.Constants.*;
import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.processors.ProcessorFactory.createProcessors;
import static javafx.application.Platform.runLater;

/**
 * Responsible for wiring together the main application components for a
 * particular workspace (project). These include the definition views,
 * text editors, and preview pane along with any corresponding controllers.
 */
public class MainView extends SplitPane {
  /**
   * Prevents re-instantiation of processing classes.
   */
  private final Map<Node, Processor<String>> mProcessors =
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

  public MainView() {
    initLayout();
  }

  /**
   * Opens all the files into the application, provided the paths are unique.
   *
   * @param files The list of files to open.
   */
  public void open( final List<File> files ) {
    for( final var file : files ) {
      final var controller = createController( file );
      final var mediaType = file.getMediaType();
      final var tabPane = obtainDetachableTabPane( mediaType );
      final var tab =
          tabPane.addTab( controller.getFilename(), controller.getView() );
      tab.setTooltip( createTooltip( file ) );
    }
  }

  /**
   * Adds all content panels to the main user interface. This will load the
   * configuration settings from the workspace to reproduce the settings from
   * a previous session.
   */
  private void initLayout() {
    final var workspace = new Workspace( "default" );
    open( bin( workspace.restoreFiles() ) );

    final var cTabPane = obtainDetachableTabPane( TEXT_HTML );
    cTabPane.addTab( "HTML", mHtmlPreview );

    final var items = getItems();
    final var ratio = 100f / items.size() / 100;
    final var positions = getDividerPositions();

    for( int i = 0; i < positions.length; i++ ) {
      positions[ i ] = ratio * i;
    }

    // TODO: Load divider positions from exported settings, see bin() comment.
    setDividerPositions( positions );
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
    final var linkedMap = new LinkedHashMap<MediaType, List<File>>();
    linkedMap.put( TEXT_YAML, new ArrayList<>() );
    linkedMap.put( TEXT_MARKDOWN, new ArrayList<>() );
    linkedMap.put( UNDEFINED, new ArrayList<>() );

    for( final var file : files ) {
      final var list = linkedMap.computeIfAbsent(
          file.getMediaType(), k -> new ArrayList<>()
      );

      list.add( file );
    }

    final var definitions = linkedMap.get( TEXT_YAML );
    final var documents = linkedMap.get( TEXT_MARKDOWN );
    final var undefined = linkedMap.get( UNDEFINED );

    if( definitions.isEmpty() ) {
      definitions.add( new File( DEFINITION_NAME ) );
    }

    if( documents.isEmpty() ) {
      documents.add( new File( DOCUMENT_NAME ) );
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
   * @param node Contains the source document to update in the preview pane.
   */
  private void refresh( final Node node ) {
    final var processor = mProcessors.get( node );
    processor.apply( ((TextResource) node).getText() );
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
    var tabPane = mTabPanes.get( mediaType );

    if( tabPane == null ) {
      tabPane = new DetachableTabPane();
      mTabPanes.put( mediaType, tabPane );

      final var model = tabPane.getSelectionModel();

      // When a tab is clicked or selected using Ctrl+PgUp/Ctrl+PgDn, this
      // ensures that the tab content retains focus.
      model.selectedIndexProperty().addListener(
          ( c, o, n ) -> runLater(
              () -> {
                final var node = model.getSelectedItem().getContent();
                node.requestFocus();
                refresh( node );
              }
          )
      );

      getItems().add( tabPane );
    }

    return tabPane;
  }

  @SuppressWarnings({"unchecked", "RedundantSuppression"})
  private <V extends Node & TextResource> EditorController<V> createController(
      final File file ) {
    final var view = createView( file.getMediaType() );

    if( !(view instanceof DefinitionEditor) ) {
      final var path = file.toPath();
      final var caret = view.createCaretPosition();
      final var context = createProcessorContext( path, caret );

      mHtmlPreview.setBaseUri( path );
      mProcessors.computeIfAbsent( view, p -> createProcessors( context ) );

      // When the caret position changes, synchronize with the preview.
      context.getCaretPosition().textOffsetProperty().addListener(
          ( c, o, n ) -> refresh( view )
      );
    }

    return new EditorController<>( file.toPath(), (V) view );
  }

  private ProcessorContext createProcessorContext(
      final Path path, final CaretPosition caretPosition ) {
    return new ProcessorContext(
        mHtmlPreview, mResolvedMap, path, caretPosition, NONE
    );
  }

  @SuppressWarnings({"RedundantCast", "unchecked", "RedundantSuppression"})
  private <V extends Node & TextResource> V createView(
      final MediaType mediaType ) {
    return (V) switch( mediaType ) {
      case TEXT_YAML -> new DefinitionEditor( new YamlTreeAdapter() );
      case TEXT_MARKDOWN -> new MarkdownEditor();
      default -> new PlainTextEditor();
    };
  }

  private Tooltip createTooltip( final File file ) {
    final var path = file.toPath();
    return new Tooltip( path.toString() );
  }
}
