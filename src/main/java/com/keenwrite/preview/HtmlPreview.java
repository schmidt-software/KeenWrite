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
package com.keenwrite.preview;

import javafx.embed.swing.SwingNode;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;

import static com.keenwrite.Constants.STYLESHEET_PREVIEW;
import static java.lang.Math.max;
import static java.lang.String.format;
import static javafx.scene.CacheHint.SPEED;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Responsible for parsing an HTML document.
 */
public final class HtmlPreview extends SwingNode {

  /**
   * Render CSS using points (pt) not pixels (px) to reduce the chance of
   * poor rendering.
   */
  private static final String HTML_HEAD_OPEN = format(
      """
          <!DOCTYPE html>
          <html lang='en'><head><title> </title><meta charset='utf-8'/>
          <link rel='stylesheet' href='%s'/>
          """,
      HtmlPreview.class.getResource( STYLESHEET_PREVIEW )
  );

  /**
   * Used by SVG rendering when resolving local image files.
   */
  private static final String HTML_BASE = "<base href='%s'>";
  private static final String HTML_HEAD_CLOSE = "</head><body>";
  private static final String HTML_TAIL = "</body></html>";

  /**
   * Used to reset the {@link #mHtmlDocument} buffer so that the
   * {@link #HTML_HEAD_OPEN} need not be appended all the time.
   */
  private static final int HTML_PREFIX_LENGTH = HTML_HEAD_OPEN.length();

  /**
   * The buffer is reused so that previous memory allocations need not repeat.
   */
  private final StringBuilder mHtmlDocument = new StringBuilder( 65536 );

  private HtmlPanel mView;
  private JScrollPane mScrollPane;

  private String mBaseUriPath = "";
  private String mBaseUriHtml = "";

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   */
  public HtmlPreview() {
    setStyle( "-fx-background-color: white;" );

    // No need to append same prefix each time the HTML content is updated.
    mHtmlDocument.append( HTML_HEAD_OPEN );

    invokeLater( () -> {
      mView = new HtmlPanel();
      mScrollPane = new JScrollPane( mView );

      // Enabling the cache eliminates black background flashes when resizing.
      setCache( true );
      setCacheHint( SPEED );
      setContent( mScrollPane );

      final var factory = new ChainedReplacedElementFactory();
      factory.addFactory( new SvgReplacedElementFactory() );
      factory.addFactory( new SwingReplacedElementFactory() );

      final var context = mView.getSharedContext();
      final var textRenderer = context.getTextRenderer();
      context.setReplacedElementFactory( factory );
      textRenderer.setSmoothingThreshold( 0 );
    } );
  }

  /**
   * Updates the internal HTML source shown in the preview pane.
   *
   * @param html The new HTML document to display.
   */
  public void render( final String html ) {
    mView.render( decorate( html ), getBaseUri() );
  }

  /**
   * Clears the preview pane by rendering an empty string.
   */
  public void clear() {
    render( "" );
  }

  /**
   * Sets the base URI to the containing directory the file being edited.
   *
   * @param path The path to the file being edited.
   */
  public void setBaseUri( final Path path ) {
    final var parent = path.getParent();
    mBaseUriPath = parent == null ? "" : parent.toUri().toString();
    mBaseUriHtml = format( HTML_BASE, mBaseUriPath );
  }

  /**
   * Scrolls to the closest element matching the given identifier without
   * waiting for the document to be ready. Be sure the document is ready
   * before calling this method.
   *
   * @param id Scroll the preview pane to this unique paragraph identifier.
   */
  public void scrollTo( final String id ) {
    scrollTo( mView.getBoxById( id ) );
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
  private void scrollTo( final Box box ) {
    if( box != null ) {
      scrollTo( createPoint( box ) );
    }
  }

  private void scrollTo( final Point point ) {
    invokeLater( () -> {
      mView.scrollTo( point );
      getScrollPane().repaint();
    } );
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

    // Scroll back up by half the height of the scroll bar to keep the typing
    // area within the view port. Otherwise the view port will have jumped too
    // high up and the most recently typed letters won't be visible.
    int y = max( box.getAbsY() - getVerticalScrollBarHeight() / 2, 0 );
    int x = box.getAbsX();

    if( !box.getStyle().isInline() ) {
      final var margin = box.getMargin( mView.getLayoutContext() );
      y += margin.top();
      x += margin.left();
    }

    return new Point( x, y );
  }

  private String decorate( final String html ) {
    // Trim the HTML back to only the prefix.
    mHtmlDocument.setLength( HTML_PREFIX_LENGTH );

    // Write the HTML body element followed by closing tags.
    return mHtmlDocument.append( mBaseUriHtml )
                        .append( HTML_HEAD_CLOSE )
                        .append( html )
                        .append( HTML_TAIL )
                        .toString();
  }

  private String getBaseUri() {
    return mBaseUriPath;
  }

  private JScrollPane getScrollPane() {
    return mScrollPane;
  }

  public JScrollBar getVerticalScrollBar() {
    return getScrollPane().getVerticalScrollBar();
  }

  private int getVerticalScrollBarHeight() {
    return getVerticalScrollBar().getHeight();
  }
}
