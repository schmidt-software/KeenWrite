/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.definition;

import com.keenwrite.editors.definition.DefinitionEditor;
import com.keenwrite.editors.definition.yaml.YamlTreeTransformer;
import com.keenwrite.editors.markdown.MarkdownEditor;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preferences.XmlStore;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.sigils.Sigils;
import com.keenwrite.sigils.YamlSigilOperator;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.testfx.framework.junit5.Start;

import static com.keenwrite.constants.Constants.DEF_DELIM_BEGAN_DEFAULT;
import static com.keenwrite.constants.Constants.DEF_DELIM_ENDED_DEFAULT;
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
    final var store = new XmlStore();
    final var workspace = new Workspace( store );
    final var mainPane = new SplitPane();

    final var began = new SimpleStringProperty( DEF_DELIM_BEGAN_DEFAULT );
    final var ended = new SimpleStringProperty( DEF_DELIM_ENDED_DEFAULT );
    final var sigils = new Sigils( began.get(), ended.get() );
    final var operator = new YamlSigilOperator( sigils );
    final var transformer = new YamlTreeTransformer();
    final var editor = new DefinitionEditor( transformer, operator );

    final var tabPane1 = new DetachableTabPane();
    tabPane1.addTab( "Editor", editor );

    final var tabPane2 = new DetachableTabPane();
    final var tab21 = tabPane2.addTab( "Picker", new ColorPicker() );
    final var tab22 = tabPane2.addTab( "Editor",
                                       new MarkdownEditor( workspace ) );
    tab21.setTooltip( new Tooltip( "Colour Picker" ) );
    tab22.setTooltip( new Tooltip( "Text Editor" ) );

    final var tabPane3 = new DetachableTabPane();
    tabPane3.addTab( "Preview", new HtmlPreview( workspace ) );

    editor.addTreeChangeHandler( mTreeHandler );

    mainPane.getItems().addAll( tabPane1, tabPane2, tabPane3 );

    final var scene = new Scene( mainPane );
    stage.setScene( scene );

    stage.show();
  }
}
