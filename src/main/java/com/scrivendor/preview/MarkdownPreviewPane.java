/*
 * Copyright (c) 2015 Karl Tauber <karl at jformdesigner dot com>
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
package com.scrivendor.preview;

import java.nio.file.Path;
import java.util.Collections;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;

/**
 * Markdown preview pane.
 *
 * @author Karl Tauber
 */
public class MarkdownPreviewPane extends ScrollPane {

  private final ObjectProperty<RootNode> markdownAST = new SimpleObjectProperty<>();
  private final ObjectProperty<Path> path = new SimpleObjectProperty<>();
  private final DoubleProperty scrollY = new SimpleDoubleProperty();

  private final WebView webView = new WebView();
  private int lastScrollX;
  private int lastScrollY;

  private boolean delayScroll;

  public MarkdownPreviewPane() {
    setVbarPolicy( ALWAYS );

    markdownASTProperty().addListener( (observable, oldValue, newValue) -> {
      update();
    } );

    pathProperty().addListener( (observable, oldValue, newValue) -> {
      update();
    } );

    scrollYProperty().addListener( (observable, oldValue, newValue) -> {
      scrollY();
    } );
  }

  private String toHtml() {
    final RootNode root = getMarkdownAST();

    return root == null
      ? ""
      : new ToHtmlSerializer( new LinkRenderer(),
        Collections.<String, VerbatimSerializer>emptyMap(),
        PegDownPlugins.NONE.getHtmlSerializerPlugins() ).toHtml( root );
  }

  public void update() {
    if( !getEngine().getLoadWorker().isRunning() ) {
      setScrollXY();
    }

    getEngine().loadContent(
      "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" + getClass().getResource( "markdownpad-github.css" ) + "'>"
      + getBase()
      + "</head>"
      + "<body" + getScrollScript() + ">"
      + toHtml()
      + "</body>"
      + "</html>" );
  }

  /**
   * Obtain the window.scrollX and window.scrollY from web engine, but only no
   * worker is running (in this case the result would be zero).
   */
  private void setScrollXY() {
    lastScrollX = getNumber( execute( "window.scrollX" ) );
    lastScrollY = getNumber( execute( "window.scrollY" ) );
  }

  private int getNumber( final Object number ) {
    return (number instanceof Number) ? ((Number)number).intValue() : 0;
  }

  private String getBase() {
    final Path path = getPath();

    return path == null
      ? ""
      : ("<base href='" + path.getParent().toUri().toString() + "'>");
  }

  private String getScrollScript() {
    return (lastScrollX > 0 || lastScrollY > 0)
      ? (" onload='window.scrollTo(" + lastScrollX + "," + lastScrollY + ");'")
      : "";
  }

  /**
   * Helps avoid many superfluous runLater() calls.
   */
  private void scrollY() {
    if( !delayScroll ) {
      delayScroll = true;

      Platform.runLater( () -> {
        delayScroll = false;
        scrollY( getScrollY() );
      } );
    }
  }

  private void scrollY( double value ) {
    execute(
      "window.scrollTo(0, (document.body.scrollHeight - window.innerHeight) * "
      + value
      + ");" );
  }

  public Path getPath() {
    return pathProperty().get();
  }

  public void setPath( Path path ) {
    pathProperty().set( path );
  }

  public ObjectProperty<Path> pathProperty() {
    return this.path;
  }

  public RootNode getMarkdownAST() {
    return markdownASTProperty().get();
  }

  public void setMarkdownAST( RootNode astRoot ) {
    markdownASTProperty().set( astRoot );
  }

  public ObjectProperty<RootNode> markdownASTProperty() {
    return this.markdownAST;
  }

  public double getScrollY() {
    return scrollYProperty().get();
  }

  public void setScrollY( double value ) {
    scrollYProperty().set( value );
  }

  public DoubleProperty scrollYProperty() {
    return this.scrollY;
  }

  public Node getNode() {
    return getWebView();
  }

  private Object execute( String script ) {
    return getEngine().executeScript( script );
  }

  private WebEngine getEngine() {
    return getWebView().getEngine();
  }

  private WebView getWebView() {
    return this.webView;
  }
}
