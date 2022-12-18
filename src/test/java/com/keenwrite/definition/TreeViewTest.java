/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.definition;

import com.keenwrite.editors.definition.DefinitionEditor;
import com.keenwrite.editors.definition.yaml.YamlTreeTransformer;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.assertj.core.util.Files;
import org.testfx.framework.junit5.Start;

import static com.keenwrite.util.FontLoader.initFonts;

public class TreeViewTest extends Application {
  private final SimpleObjectProperty<Node> mTextEditor =
    new SimpleObjectProperty<>();

  private final EventHandler<TreeItem.TreeModificationEvent<Event>> mTreeHandler =
    event -> refresh( mTextEditor.get() );

  private void refresh( final Node node ) {
    throw new RuntimeException( "Derp: " + node );
  }

  public static void main( final String[] args ) {
    initFonts();
    launch( args );
  }

  public void start( final Stage stage ) {
    onStart( stage );
  }

  @Start
  private void onStart( final Stage stage ) {
    final var workspace = new Workspace();
    final var mainPane = new SplitPane();
    final var transformer = new YamlTreeTransformer();
    final var editor = new DefinitionEditor( transformer );
    final var file = Files.newTemporaryFile();

    final var tabPane1 = new DetachableTabPane();
    tabPane1.addTab( "Editor", editor );

    final var tabPane2 = new DetachableTabPane();
    final var tab21 =
      tabPane2.addTab( "Picker", new ColorPicker() );
    final var tab22 =
      tabPane2.addTab( "Editor", new MarkdownEditor( file, workspace ) );
    tab21.setTooltip( new Tooltip( "Colour Picker" ) );
    tab22.setTooltip( new Tooltip( "Text Editor" ) );

    final var tabPane3 = new DetachableTabPane();
    tabPane3.addTab( "Preview", new HtmlPreview( workspace ) );

    editor.addTreeChangeHandler( mTreeHandler );

    mainPane.getItems().addAll( tabPane1, tabPane2, tabPane3 );

    stage.setScene( new Scene( mainPane ) );
    stage.show();
  }
}
