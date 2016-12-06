/*
 * Copyright 2016 Karl Tauber and White Magic Software, Ltd.
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
package com.scrivenvar.preview;

import java.nio.file.Path;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ScrollPane;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class HTMLPreviewPane extends ScrollPane {

  private Path path;
  private final DoubleProperty scrollY = new SimpleDoubleProperty();

  private final WebView webView = new WebView();
  private int lastScrollX;
  private int lastScrollY;

  private boolean delayScroll;

  private String html;

  public HTMLPreviewPane( final Path path ) {
    setPath( path );
    setVbarPolicy( ALWAYS );
    scrollYProperty().addListener( (observable, oldValue, newValue) -> {
      scrollY();
    } );
  }

  public void update( final String html ) {
    setHtml( html );
    update();
  }

  private void update() {
    if( !getEngine().getLoadWorker().isRunning() ) {
      setScrollXY();
    }

    getEngine().loadContent(
      "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" + getClass().getResource( "pane.css" ) + "'>"
      + getBase()
      + "</head>"
      + "<body" + getScrollScript() + ">"
      + getHtml()
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
    final Path basePath = getPath();

    return basePath == null
      ? ""
      : ("<base href='" + basePath.getParent().toUri().toString() + "'>");
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

  private void scrollY( final double value ) {
    execute(
      "window.scrollTo(0, (document.body.scrollHeight - window.innerHeight) * "
      + value
      + ");" );
  }

  public double getScrollY() {
    return scrollYProperty().get();
  }

  public void setScrollY( final double value ) {
    scrollYProperty().set( value );
  }

  public DoubleProperty scrollYProperty() {
    return this.scrollY;
  }

  private Object execute( final String script ) {
    return getEngine().executeScript( script );
  }

  private WebEngine getEngine() {
    return getWebView().getEngine();
  }

  public WebView getWebView() {
    return this.webView;
  }

  private String getHtml() {
    return this.html;
  }

  private void setHtml( final String html ) {
    this.html = html;
  }

  private Path getPath() {
    return this.path;
  }

  private void setPath( final Path path ) {
    this.path = path;
  }
}
