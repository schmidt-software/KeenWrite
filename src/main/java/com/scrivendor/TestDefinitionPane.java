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
package com.scrivendor;

import com.scrivendor.definition.DefinitionPane;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * TestDefinitionPane application for debugging and head-banging.
 */
public final class TestDefinitionPane extends Application {

  private static Application app;
  private Scene scene;

  public static void main( String[] args ) {
    launch( args );
  }

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

    DefinitionPane pane = new DefinitionPane();

    test( pane, "language.ai.", "article" );
    test( pane, "language.ai", "ai" );
    test( pane, "l", "location" );
    test( pane, "la", "language" );
    test( pane, "c.p.n", "name" );
    test( pane, "c.p.n.", "First" );
    test( pane, "...", "c" );
    test( pane, "foo", "c" );
    test( pane, "foo.bar", "c" );
    test( pane, "", "c" );
    test( pane, "c", "protagonist" );
    test( pane, "c.", "protagonist" );
    test( pane, "c.p", "protagonist" );
    test( pane, "c.protagonist", "protagonist" );

    System.exit( 0 );
  }

  private void test( DefinitionPane pane, String path, String value ) {
    System.out.println( "---------------------------" );
    System.out.println( "Find Path: '" + path + "'" );
    final TreeItem<String> node = pane.findNode( path );
    System.out.println( "Path Node: " + node );
    System.out.println( "Node Val : " + node.getValue() );
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
}
