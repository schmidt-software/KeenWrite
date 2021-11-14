/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.ui.adapters.DocumentAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.w3c.dom.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;

import static com.keenwrite.events.FileOpenEvent.fireFileOpenEvent;
import static com.keenwrite.events.HyperlinkOpenEvent.fireHyperlinkOpenEvent;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.max;
import static java.lang.Thread.sleep;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Responsible for configuring FlyingSaucer's {@link XHTMLPanel}.
 */
public final class FlyingSaucerPanel extends XHTMLPanel
  implements HtmlRenderer {

  /**
   * Suppresses scroll attempts until after the document has loaded.
   */
  private static final class DocumentEventHandler extends DocumentAdapter {
    private final BooleanProperty mReadyProperty = new SimpleBooleanProperty();

    @Override
    public void documentStarted() {
      mReadyProperty.setValue( FALSE );
    }

    @Override
    public void documentLoaded() {
      mReadyProperty.setValue( TRUE );
    }
  }

  /**
   * Ensures that the preview panel fills its container's area completely.
   */
  private final class ComponentEventHandler extends ComponentAdapter {
    /**
     * Invoked when the component's size changes.
     */
    public void componentResized( final ComponentEvent e ) {
      setPreferredSize( e.getComponent().getPreferredSize() );
    }
  }

  /**
   * Responsible for opening hyperlinks. External hyperlinks are opened in
   * the system's default browser; local file system links are opened in the
   * editor.
   */
  private static final class HyperlinkListener extends LinkListener {
    @Override
    public void linkClicked( final BasicPanel panel, final String link ) {
      try {
        final var uri = new URI( link );

        switch( getProtocol( uri ) ) {
          case HTTP -> fireHyperlinkOpenEvent( uri );
          case FILE -> fireFileOpenEvent( uri );
        }
      } catch( final Exception ex ) {
        clue( ex );
      }
    }
  }

  private static final XhtmlNamespaceHandler XNH = new XhtmlNamespaceHandler();
  private final ChainedReplacedElementFactory mFactory;

  FlyingSaucerPanel() {
    // The order is important: SwingReplacedElementFactory replaces SVG images
    // with a blank image, which will cause the chained factory to cache the
    // image and exit. Instead, the SVG must execute first to rasterize the
    // content. Consequently, the chained factory must maintain insertion order.
    mFactory = new ChainedReplacedElementFactory(
      new SvgReplacedElementFactory(),
      new SwingReplacedElementFactory()
    );

    final var context = getSharedContext();
    final var textRenderer = context.getTextRenderer();
    context.setReplacedElementFactory( mFactory );
    textRenderer.setSmoothingThreshold( 0 );

    addDocumentListener( new DocumentEventHandler() );
    removeMouseTrackingListeners();
    addMouseTrackingListener( new HyperlinkListener() );
    addComponentListener( new ComponentEventHandler() );
  }

  /**
   * Updates the document model displayed by the renderer. Effectively, this
   * updates the HTML document to provide new content.
   *
   * @param doc     A complete HTML5 document, including doctype.
   * @param baseUri URI to use for finding relative files, such as images.
   */
  @Override
  public void render( final Document doc, final String baseUri ) {
    setDocument( doc, baseUri, XNH );
  }

  @Override
  public void clearCache() {
    mFactory.clearCache();
  }

  @Override
  public void scrollTo( final String id, final JScrollPane scrollPane ) {
    int iter = 0;
    Box box = null;

    while( iter++ < 3 && ((box = getBoxById( id )) == null) ) {
      try {
        sleep( 10 );
      } catch( final Exception ex ) {
        clue( ex );
      }
    }

    scrollTo( box, scrollPane );
  }

  /**
   * Scrolls to the location specified by the {@link Box} that corresponds
   * to a point somewhere in the preview pane. If there is no caret, then
   * this will not change the scroll position. Changing the scroll position
   * to the top if the {@link Box} instance is {@code null} will result in
   * jumping around a lot and inconsistent synchronization issues.
   *
   * @param box The rectangular region containing the caret, or {@code null}
   *            if the HTML does not have a caret.
   */
  private void scrollTo( final Box box, final JScrollPane scrollPane ) {
    if( box != null ) {
      invokeLater( () -> {
        scrollTo( createPoint( box, scrollPane ) );
        scrollPane.repaint();
      } );
    }
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
  private Point createPoint( final Box box, final JScrollPane scrollPane ) {
    assert box != null;

    // Scroll back up by half the height of the scroll bar to keep the typing
    // area within the view port. Otherwise the view port will have jumped too
    // high up and the most recently typed letters won't be visible.
    int y = max( box.getAbsY() - scrollPane.getVerticalScrollBar()
                                           .getHeight() / 2, 0 );
    int x = box.getAbsX();

    if( !box.getStyle().isInline() ) {
      final var margin = box.getMargin( getLayoutContext() );
      y += margin.top();
      x += margin.left();
    }

    return new Point( x, y );
  }

  /**
   * Delegates to the {@link SharedContext}.
   *
   * @param id The HTML element identifier to retrieve in {@link Box} form.
   * @return The {@link Box} that corresponds to the given element ID, or
   * {@code null} if none found.
   */
  Box getBoxById( final String id ) {
    return getSharedContext().getBoxById( id );
  }

  /**
   * Suppress scrolling to the top on updates.
   */
  @Override
  public void resetScrollPosition() {
  }

  /**
   * The default mouse click listener attempts navigation within the preview
   * panel. We want to usurp that behaviour to open the link in a
   * platform-specific browser.
   */
  private void removeMouseTrackingListeners() {
    for( final var listener : getMouseTrackingListeners() ) {
      if( !(listener instanceof HoverListener) ) {
        removeMouseTrackingListener( (FSMouseListener) listener );
      }
    }
  }
}
