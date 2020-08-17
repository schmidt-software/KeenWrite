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

import com.scrivenvar.Services;
import com.scrivenvar.adapters.DocumentAdapter;
import com.scrivenvar.graphics.SVGReplacedElementFactory;
import com.scrivenvar.service.events.Notifier;
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
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.nio.file.Path;

import static com.scrivenvar.Constants.*;
import static java.awt.Desktop.Action.BROWSE;
import static java.awt.Desktop.getDesktop;
import static java.lang.Math.max;
import static org.xhtmlrenderer.swing.ImageResourceLoader.NO_OP_REPAINT_LISTENER;

/**
 * HTML preview pane is responsible for rendering an HTML document.
 */
public final class HTMLPreviewPane extends Pane {
  private final static Notifier sNotifier = Services.load( Notifier.class );

  final IndieFrame mIndie = new IndieFrame();

  private static class IndieFrame {
    private final XHTMLPanel panel = new XHTMLPanel();
    private final JFrame frame = new JFrame( "Single Page Demo" );

    private IndieFrame() {
      frame.setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
      frame.getContentPane().add( new FSScrollPane( panel ) );
      frame.pack();
      frame.setSize( 1024, 768 );
    }

    private void update( final org.w3c.dom.Document html ) {
      frame.setVisible( true );

      // Set the XHTML document to render. We use the simplest form
      // of the API call, which uses a File reference. There
      // are a variety of overloads for setDocument().
      try {
        panel.setDocument( html );
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  private static class HTMLPanel extends XHTMLPanel {
    /**
     * Suppresses scrolling to the top on every key press.
     */
    @Override
    public void resetScrollPosition() {
    }
  }

  /**
   * Suppresses scroll attempts until after the document has loaded.
   */
  private static final class DocumentEventHandler extends DocumentAdapter {
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
  }

  /**
   * Responsible for ensuring that images are constrained to the panel width
   * upon resizing.
   */
  private final class ResizeListener extends ComponentAdapter {
    @Override
    public void componentResized( final ComponentEvent e ) {
      setWidth( e );
    }

    @Override
    public void componentShown( final ComponentEvent e ) {
      setWidth( e );
    }

    /**
     * Sets the width of the {@link HTMLPreviewPane} so that images can be
     * scaled to fit. The scale factor is adjusted a bit below the full width
     * to prevent the horizontal scrollbar from appearing.
     *
     * @param e The component that defines the image scaling width.
     */
    private void setWidth( final ComponentEvent e ) {
      final int width = (int) (e.getComponent().getWidth() * .95);
      HTMLPreviewPane.this.mImageLoader.widthProperty().set( width );
    }
  }

  /**
   * Responsible for launching hyperlinks in the system's default browser.
   */
  private static class HyperlinkListener extends LinkListener {
    @Override
    public void linkClicked( final BasicPanel panel, final String uri ) {
      try {
        final var desktop = getDesktop();

        if( desktop.isSupported( BROWSE ) ) {
          desktop.browse( new URI( uri ) );
        }
      } catch( final Exception e ) {
        sNotifier.notify( e );
      }
    }
  }

  /**
   * The CSS must be rendered in points (pt) not pixels (px) to avoid blurry
   * rendering on some platforms.
   */
  private final static String HTML_PREFIX = "<!DOCTYPE html>"
      + "<html>"
      + "<head>"
      + "<link rel='stylesheet' href='" +
      HTMLPreviewPane.class.getResource( STYLESHEET_PREVIEW ) + "'/>"
      + "</head>"
      + "<body>";

  // Provide some extra space at the end for scrolling past the last line.
  private final static String HTML_SUFFIX =
      "<p style='height=2em'>&nbsp;</p></body></html>";

  private final static W3CDom W3C_DOM = new W3CDom();
  private final static XhtmlNamespaceHandler NS_HANDLER =
      new XhtmlNamespaceHandler();

  private final StringBuilder mHtmlDocument = new StringBuilder( 65536 );
  private final int mHtmlPrefixLength;

  private final HTMLPanel mHtmlRenderer = new HTMLPanel();
  private final SwingNode mSwingNode = new SwingNode();
  private final JScrollPane mScrollPane = new JScrollPane( mHtmlRenderer );
  private final DocumentEventHandler mDocHandler = new DocumentEventHandler();
  private final CustomImageLoader mImageLoader = new CustomImageLoader();

  private Path mPath = DEFAULT_DIRECTORY;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HTMLPreviewPane() {
    setStyle( "-fx-background-color: white;" );

    // No need to append same prefix each time the HTML content is updated.
    mHtmlDocument.append( HTML_PREFIX );
    mHtmlPrefixLength = mHtmlDocument.length();

    // Inject an SVG renderer that produces high-quality SVG buffered images.
    final var factory = new ChainedReplacedElementFactory();
    factory.addFactory( new SVGReplacedElementFactory() );
    factory.addFactory( new SwingReplacedElementFactory(
        NO_OP_REPAINT_LISTENER, mImageLoader ) );

    final var context = getSharedContext();
    final var textRenderer = context.getTextRenderer();
    context.setReplacedElementFactory( factory );
    textRenderer.setSmoothingThreshold( 0 );

    mSwingNode.setContent( mScrollPane );

    mHtmlRenderer.addDocumentListener( mDocHandler );
    mHtmlRenderer.addComponentListener( new ResizeListener() );

    // The default mouse click listener attempts navigation within the
    // preview panel. We want to usurp that behaviour to open the link in
    // a platform-specific browser.
    for( final var listener : mHtmlRenderer.getMouseTrackingListeners() ) {
      if( !(listener instanceof HoverListener) ) {
        mHtmlRenderer.removeMouseTrackingListener( (FSMouseListener) listener );
      }
    }

    mHtmlRenderer.addMouseTrackingListener( new HyperlinkListener() );
  }

  /**
   * Updates the internal HTML source, loads it into the preview pane, then
   * scrolls to the caret position.
   *
   * @param html The new HTML document to display.
   */
  public void process( final String html ) {
    final Document jsoupDoc = Jsoup.parse( decorate( html ) );
    final org.w3c.dom.Document w3cDoc = W3C_DOM.fromJsoup( jsoupDoc );

    mHtmlRenderer.setDocument( w3cDoc, getBaseUrl(), NS_HANDLER );

    //mIndie.update( w3cDoc );
  }

  public void clear() {
    process( "" );
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

          mDocHandler.readyProperty().removeListener( this );
        }
      }
    };

    mDocHandler.readyProperty().addListener( listener );
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
        scrollToBottom();
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
    mHtmlRenderer.scrollTo( point );
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

  private void scrollToBottom() {
    scrollToY( mHtmlRenderer.getHeight() );
  }

  private Box getBoxById( final String id ) {
    return getSharedContext().getBoxById( id );
  }

  private String decorate( final String html ) {
    // Trim the HTML back to only the prefix.
    mHtmlDocument.setLength( mHtmlPrefixLength );

    // Write the HTML body element followed by closing tags.
    return mHtmlDocument.append( html )
                        .append( HTML_SUFFIX )
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

  /**
   * Creates a {@link Point} to use as a reference for scrolling to the area
   * described by the given {@link Box}. The {@link Box} coordinates are used
   * to populate the {@link Point}'s location, with minor adjustments for
   * vertical centering.
   *
   * @param box The {@link Box} that represents a scrolling anchor reference.
   * @return A coordinate suitable for scrolling to.
   */
  private Point createPoint( final Box box ) {
    assert box != null;

    int x = box.getAbsX();

    // Scroll back up by half the height of the scroll bar to keep the typing
    // area within the view port. Otherwise the view port will have jumped too
    // high up and the whatever gets typed won't be visible.
    int y = max(
        box.getAbsY() - (mScrollPane.getVerticalScrollBar().getHeight() / 2),
        0 );

    if( !box.getStyle().isInline() ) {
      final var margin = box.getMargin( mHtmlRenderer.getLayoutContext() );
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
    return mHtmlRenderer.getSharedContext();
  }

}
