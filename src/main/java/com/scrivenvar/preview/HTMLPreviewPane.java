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

import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;

import javax.swing.*;
import java.nio.file.Path;

import static com.scrivenvar.Constants.CARET_POSITION_BASE;
import static com.scrivenvar.Constants.STYLESHEET_PREVIEW;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class HTMLPreviewPane extends Pane {
  final W3CDom mW3cDom = new W3CDom();
  final XhtmlNamespaceHandler mNamespaceHandler = new XhtmlNamespaceHandler();
  final XHTMLPanel mRenderer = new XHTMLPanel();
  final SwingNode mSwingNode = new SwingNode();
  final JScrollPane mScrollPane = new JScrollPane( mRenderer );

  //private final WebView mWebView = new WebView();
  private Path mPath;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HTMLPreviewPane() {
    mSwingNode.setContent( mScrollPane );
    mRenderer.getSharedContext().getTextRenderer().setSmoothingThreshold( 0 );
    initListeners();
    //initTraversal();
  }

  /**
   * Initializes observers for document changes. When the document is reloaded
   * with new HTML, this triggers a scroll event that repositions the document
   * to the injected caret (that corresponds with the position in the text
   * editor).
   */
  private void initListeners() {
    // Scrolls to the caret after the content has been loaded.
//    getEngine().getLoadWorker().stateProperty().addListener(
//        ( ObservableValue<? extends State> observable,
//          final State oldValue, final State newValue ) -> {
//          if( newValue == SUCCEEDED ) {
//            scrollToCaret();
//          }
//        } );
  }

  private String getBaseUrl() {
    final Path basePath = getPath();
    final Path parent = basePath == null ? null : basePath.getParent();

    return parent == null ? "" : parent.toUri().toString();
  }

  /**
   * Ensures images can be found relative to the document.
   *
   * @return The base path element to use for the document, or the empty string
   * if no path has been set, yet.
   */
  private String getBaseElement() {
    return "<base href='" + getBaseUrl() + "'/>";
  }

  /**
   * Updates the internal HTML source, loads it into the preview pane, then
   * scrolls to the caret position.
   *
   * @param html The new HTML document to display.
   */
  public void update( final String html ) {
    final Document jsoupDoc = Jsoup.parse( decorate( html ) );
    org.w3c.dom.Document w3cDoc = mW3cDom.fromJsoup( jsoupDoc );

    mRenderer.setDocument( w3cDoc, getBaseUrl(), mNamespaceHandler );
  }

  private final static String HTML_HEADER = "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" +
      HTMLPreviewPane.class.getResource( STYLESHEET_PREVIEW ) + "'/>"
//            + getBase()
      + "</head>"
      + "<body>";
  private final static String HTML_FOOTER = "</body></html>";

  private final StringBuilder htmlDoc = new StringBuilder( 65536 );

  private String decorate( final String html ) {
    htmlDoc.setLength( 0 );
    return htmlDoc.append( HTML_HEADER )
                  .append( html )
                  .append( HTML_FOOTER )
                  .toString();
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
//  private void scrollToCaret() {
//    execute( getScrollScript() );
//  }

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
        + "    return this.offsetTop + (this.offsetParent ? this.offsetParent" +
        ".topOffset() : 0);"
        + "  };"
        + "  window.scrollTo( 0, e.topOffset() - (window.innerHeight / 2 ) );"
        + "}";
  }

  /**
   * Prevent tabbing into the preview pane.
   */
//  private void initTraversal() {
//    getWebView().setFocusTraversable( false );
//  }
//
//  private void execute( final String script ) {
//    getEngine().executeScript( script );
//  }

//  private WebEngine getEngine() {
//    return getWebView().getEngine();
//  }
//
//  private WebView getWebView() {
//    return mWebView;
//  }
  private Path getPath() {
    return this.mPath;
  }

  public void setPath( final Path path ) {
    assert path != null;

    this.mPath = path;
  }

  /**
   * Content to embed in a panel.
   *
   * @return The content to display to the user.
   */
  public Node getNode() {
    return mSwingNode;
    //return getWebView();
  }

  public JScrollPane getScrollPane() {
    return mScrollPane;
  }

  public JScrollBar getVerticalScrollBar() {
    return getScrollPane().getVerticalScrollBar();
  }
}
