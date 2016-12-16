/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.test;

import static com.scrivenvar.Messages.get;
import com.scrivenvar.definition.DefinitionPane;
import com.scrivenvar.definition.yaml.YamlParser;
import com.scrivenvar.definition.yaml.YamlTreeAdapter;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * TestDefinitionPane application for debugging and head-banging.
 */
public abstract class TestHarness extends Application {

  private static Application app;
  private Scene scene;

  /**
   * Application entry point.
   *
   * @param stage The primary application stage.
   *
   * @throws Exception Could not read configuration file.
   */
  @Override
  public void start( final Stage stage ) throws Exception {
    initApplication();
    initScene();
    initStage( stage );
  }
  
  protected TreeView<String> createTreeView() throws IOException {
    return new YamlTreeAdapter( new YamlParser() ).adapt(
      asStream( "/com/scrivenvar/variables.yaml" ),
      get( "Pane.defintion.node.root.title" )
    );
  }
  
  protected DefinitionPane createDefinitionPane( TreeView<String> root ) {
    return new DefinitionPane( root );
  }

  private void initApplication() {
    app = this;
  }

  private void initScene() {
    final StyleClassedTextArea editor = new StyleClassedTextArea( false );
    final VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>( editor );

    final BorderPane borderPane = new BorderPane();
    borderPane.setPrefSize( 1024, 800 );
    borderPane.setCenter( scrollPane );

    setScene( new Scene( borderPane ) );
  }

  private void initStage( Stage stage ) {
    stage.setScene( getScene() );
  }

  private Scene getScene() {
    return this.scene;
  }

  private void setScene( Scene scene ) {
    this.scene = scene;
  }

  private static Application getApplication() {
    return app;
  }

  public static void showDocument( String uri ) {
    getApplication().getHostServices().showDocument( uri );
  }

  protected InputStream asStream( String resource ) {
    return TestHarness.class.getResourceAsStream( resource );
  }
}
