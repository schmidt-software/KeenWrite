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
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

import javax.swing.*;
import java.nio.file.Path;

import static com.scrivenvar.Constants.STYLESHEET_PREVIEW;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class HTMLPreviewPane extends Pane {
  private static class HTMLPanel extends XHTMLPanel {
    /**
     * Prevent scrolling to the top.
     */
    @Override
    public void resetScrollPosition() {
    }
  }

  private final static String HTML_HEADER = "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" +
      HTMLPreviewPane.class.getResource( STYLESHEET_PREVIEW ) + "'/>"
      + "</head>"
      + "<body>";
  private final static String HTML_FOOTER = "</body></html>";

  private final StringBuilder mHtml = new StringBuilder( 65536 );
  private final int mHtmlPrefixLength;

  private final W3CDom mW3cDom = new W3CDom();
  private final XhtmlNamespaceHandler mNamespaceHandler =
      new XhtmlNamespaceHandler();
  private final HTMLPanel mRenderer = new HTMLPanel();
  private final SwingNode mSwingNode = new SwingNode();
  private final JScrollPane mScrollPane = new JScrollPane( mRenderer );

  private Path mPath;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HTMLPreviewPane() {
    final ChainedReplacedElementFactory factory =
        new ChainedReplacedElementFactory();
    factory.addFactory( new SVGReplacedElementFactory() );
    factory.addFactory( new SwingReplacedElementFactory() );

    mRenderer.getSharedContext().setReplacedElementFactory( factory );
    mRenderer.getSharedContext().getTextRenderer().setSmoothingThreshold( 0 );
    mSwingNode.setContent( mScrollPane );

    mHtml.append( HTML_HEADER );
    mHtmlPrefixLength = mHtml.length();
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

  private String decorate( final String html ) {
    mHtml.setLength( mHtmlPrefixLength );
    return mHtml.append( html )
                .append( HTML_FOOTER )
                .toString();
  }

  /**
   * Clears out the HTML content from the preview.
   */
  public void clear() {
    update( "" );
  }

  private String getBaseUrl() {
    final Path basePath = getPath();
    final Path parent = basePath == null ? null : basePath.getParent();

    return parent == null ? "" : parent.toUri().toString();
  }

  private Path getPath() {
    return mPath;
  }

  public void setPath( final Path path ) {
    assert path != null;
    mPath = path;
  }

  /**
   * Content to embed in a panel.
   *
   * @return The content to display to the user.
   */
  public Node getNode() {
    return mSwingNode;
  }

  public JScrollPane getScrollPane() {
    return mScrollPane;
  }

  public JScrollBar getVerticalScrollBar() {
    return getScrollPane().getVerticalScrollBar();
  }
}
