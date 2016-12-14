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

import static com.scrivenvar.Constants.CARET_POSITION_BASE;
import static com.scrivenvar.Constants.STYLESHEET_PREVIEW;
import java.nio.file.Path;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import static javafx.concurrent.Worker.State.SUCCEEDED;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class HTMLPreviewPane extends Pane {

  private final WebView webView = new WebView();
  private Path path;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HTMLPreviewPane() {
    initListeners();
    initTraversal();
  }

  /**
   * Initializes observers for document changes. When the document is reloaded
   * with new HTML, this triggers a scroll event that repositions the document
   * to the injected caret (that corresponds with the position in the text
   * editor).
   */
  private void initListeners() {
    // Scrolls to the caret after the content has been loaded.
    getEngine().getLoadWorker().stateProperty().addListener(
      (ObservableValue<? extends State> observable,
        final State oldValue, final State newValue) -> {
        if( newValue == SUCCEEDED ) {
          scrollToCaret();
        }
      } );
  }

  /**
   * Ensures images can be found relative to the document.
   *
   * @return The base path element to use for the document, or the empty string
   * if no path has been set, yet.
   */
  private String getBase() {
    final Path basePath = getPath();

    return basePath == null
      ? ""
      : ("<base href='" + basePath.getParent().toUri().toString() + "'>");
  }

  /**
   * Updates the internal HTML source, loads it into the preview pane, then
   * scrolls to the caret position.
   *
   * @param html The new HTML document to display.
   */
  public void update( final String html ) {
    getEngine().loadContent(
      "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" + getClass().getResource( STYLESHEET_PREVIEW ) + "'>"
      + getBase()
      + "</head>"
      + "<body>"
      + html
      + "</body>"
      + "</html>" );
  }

  /**
   * Clears out the HTML content from the preview.
   */
  public void clear() {
    update( "" );
  }

  /**
   * Scrolls to the caret position in the document.
   */
  private void scrollToCaret() {
    execute( getScrollScript() );
  }

  /**
   * Returns the JavaScript used to scroll the WebView pane.
   *
   * @return A script that tries to center the view port on the CARET POSITION.
   */
  private String getScrollScript() {
    return ""
      + "var e = document.getElementById('" + CARET_POSITION_BASE + "');"
      + "if( e != null ) { "
      + "  Element.prototype.topOffset = function () {"
      + "    return this.offsetTop + (this.offsetParent ? this.offsetParent.topOffset() : 0);"
      + "  };"
      + "  window.scrollTo( 0, e.topOffset() - (window.innerHeight / 2 ) );"
      + "}";
  }

  /**
   * Prevent tabbing into the preview pane.
   */
  private void initTraversal() {
    getWebView().setFocusTraversable( false );
  }

  private Object execute( final String script ) {
    return getEngine().executeScript( script );
  }

  private WebEngine getEngine() {
    return getWebView().getEngine();
  }

  private WebView getWebView() {
    return this.webView;
  }

  private Path getPath() {
    return this.path;
  }

  public void setPath( final Path path ) {
    this.path = path;
  }

  /**
   * Content to embed in a panel.
   *
   * @return The content to display to the user.
   */
  public Node getNode() {
    return getWebView();
  }
}
