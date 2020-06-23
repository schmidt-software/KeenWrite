/*
 * Copyright 2020 Karl Tauber and White Magic Software, Ltd.
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static com.scrivenvar.Constants.PARAGRAPH_ID_PREFIX;
import static com.scrivenvar.Constants.STYLESHEET_PREVIEW;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 *
 * @author Karl Tauber and White Magic Software, Ltd.
 */
public final class HTMLPreviewPane extends Pane {
  /**
   * Prevent scrolling to the top on every key press.
   */
  private static class HTMLPanel extends XHTMLPanel {
    @Override
    public void resetScrollPosition() {
    }
  }

  /**
   * Prevent scroll attempts until after the document has loaded.
   */
  private static final class DocumentEventHandler implements DocumentListener {
    private final BooleanProperty mReadyProperty = new SimpleBooleanProperty();

    public BooleanProperty readyProperty() {
      return mReadyProperty;
    }

    @Override
    public void documentStarted() {
      mReadyProperty.setValue( Boolean.FALSE );
    }

    @Override
    public void documentLoaded() {
      mReadyProperty.setValue( Boolean.TRUE );
    }

    @Override
    public void onLayoutException( final Throwable t ) {
    }

    @Override
    public void onRenderException( final Throwable t ) {
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
  private final DocumentEventHandler mDocumentHandler =
      new DocumentEventHandler();

  private Path mPath;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HTMLPreviewPane() {
    final var factory = new ChainedReplacedElementFactory();
    factory.addFactory( new SVGReplacedElementFactory() );
    factory.addFactory( new SwingReplacedElementFactory() );

    final var context = getSharedContext();
    context.setReplacedElementFactory( factory );
    context.getTextRenderer().setSmoothingThreshold( 0 );

    mSwingNode.setContent( mScrollPane );

    mHtml.append( HTML_HEADER );
    mHtmlPrefixLength = mHtml.length();

    mRenderer.addDocumentListener( mDocumentHandler );
  }

  /**
   * Updates the internal HTML source, loads it into the preview pane, then
   * scrolls to the caret position.
   *
   * @param html The new HTML document to display.
   */
  public void update( final String html ) {
    final Document jsoupDoc = Jsoup.parse( decorate( html ) );
    final org.w3c.dom.Document w3cDoc = mW3cDom.fromJsoup( jsoupDoc );

    mRenderer.setDocument( w3cDoc, getBaseUrl(), mNamespaceHandler );
  }

  /**
   * Scrolls to an anchor link. The anchor links are injected when the
   * HTML document is created.
   *
   * @param id The unique anchor link identifier.
   */
  public void tryScrollTo( final int id ) {
    final ChangeListener<Boolean> listener = new ChangeListener<>() {
      @Override
      public void changed(
          final ObservableValue<? extends Boolean> observable,
          final Boolean oldValue,
          final Boolean newValue ) {
        if( newValue ) {
          scrollTo( id );

          mDocumentHandler.readyProperty().removeListener( this );
        }
      }
    };

    mDocumentHandler.readyProperty().addListener( listener );
  }

  /**
   * Scrolls to the closest element matching the given identifier without
   * waiting for the document to be ready. Be sure the document is ready
   * before calling this method.
   *
   * @param id Paragraph index.
   */
  public void scrollTo( final int id ) {
    if( id < 2 ) {
      scrollToTop();
    }
    else {
      Box box = findPrevBox( id );
      box = box == null ? findNextBox( id + 1 ) : box;

      if( box == null ) {
        srollToBottom();
      }
      else {
        scrollTo( box );
      }
    }
  }

  private Box findPrevBox( final int id ) {
    int prevId = id;
    Box box = null;

    while( prevId > 0 && (box = getBoxById( PARAGRAPH_ID_PREFIX + prevId )) == null ) {
      prevId--;
    }

    return box;
  }

  private Box findNextBox( final int id ) {
    int nextId = id;
    Box box = null;

    while( nextId - id < 5 &&
        (box = getBoxById( PARAGRAPH_ID_PREFIX + nextId )) == null ) {
      nextId++;
    }

    return box;
  }

  private void scrollTo( final Point point ) {
    mRenderer.scrollTo( point );
  }

  private void scrollTo( final Box box ) {
    scrollTo( createPoint( box ) );
  }

  private void scrollToY( final int y ) {
    scrollTo( new Point( 0, y ) );
  }

  private void scrollToTop() {
    scrollToY( 0 );
  }

  private void srollToBottom() {
    scrollToY( mRenderer.getHeight() );
  }

  private Box getBoxById( final String id ) {
    return getSharedContext().getBoxById( id );
  }

  private String decorate( final String html ) {
    // Trim the HTML back to the header.
    mHtml.setLength( mHtmlPrefixLength );

    // Write the HTML body element followed by closing tags.
    return mHtml.append( html )
                .append( HTML_FOOTER )
                .toString();
  }

  public Path getPath() {
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

  private Point createPoint( final Box box ) {
    assert box != null;

    int x = box.getAbsX();

    // Scroll back up by half the height of the scroll bar to keep the typing
    // area within the view port. Otherwise the view port will have jumped too
    // high up and the whatever gets typed won't be visible.
    int y = Math.max(
        box.getAbsY() - (mScrollPane.getVerticalScrollBar().getHeight() / 2),
        0 );

    if( !box.getStyle().isInline() ) {
      final var margin = box.getMargin( mRenderer.getLayoutContext() );
      x += margin.left();
      y += margin.top();
    }

    return new Point( x, y );
  }

  private String getBaseUrl() {
    final Path basePath = getPath();
    final Path parent = basePath == null ? null : basePath.getParent();

    return parent == null ? "" : parent.toUri().toString();
  }

  private SharedContext getSharedContext() {
    return mRenderer.getSharedContext();
  }
}
