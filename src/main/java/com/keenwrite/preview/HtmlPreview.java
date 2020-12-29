/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.preferences.LocaleProperty;
import com.keenwrite.preferences.Workspace;
import javafx.beans.property.DoubleProperty;
import javafx.embed.swing.SwingNode;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;

import static com.keenwrite.Constants.*;
import static com.keenwrite.Messages.get;
import static com.keenwrite.preferences.Workspace.KEY_UI_FONT_LOCALE;
import static com.keenwrite.preferences.Workspace.KEY_UI_FONT_PREVIEW_SIZE;
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
  private static final String HTML_HEAD =
    """
      <!DOCTYPE html>
      <html lang='%s'><head><title> </title><meta charset='utf-8'>
      <link rel='stylesheet' href='%s'>
      <link rel='stylesheet' href='%s'>
      <style>body{font-size: %spt;}</style>
      <base href='%s'>
      </head><body>
      """;

  private static final String HTML_TAIL = "</body></html>";

  private static final URL HTML_STYLE_PREVIEW = toUrl( STYLESHEET_PREVIEW );

  /**
   * The buffer is reused so that previous memory allocations need not repeat.
   */
  private final StringBuilder mHtmlDocument = new StringBuilder( 65536 );

  private HtmlPanel mView;
  private JScrollPane mScrollPane;
  private String mBaseUriPath = "";
  private URL mLocaleUrl;
  private final Workspace mWorkspace;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   *
   * @param workspace Contains locale and font size information.
   */
  public HtmlPreview( final Workspace workspace ) {
    mWorkspace = workspace;
    mLocaleUrl = toUrl( getLocale() );

    // Attempts to prevent a flash of black un-styled content upon load.
    setStyle( "-fx-background-color: white;" );

    invokeLater( () -> {
      mView = new HtmlPanel();
      mScrollPane = new JScrollPane( mView );

      // Enabling the cache attempts to prevent black flashes when resizing.
      setCache( true );
      setCacheHint( SPEED );
      setContent( mScrollPane );

      final var creFactory = new ChainedReplacedElementFactory();
      creFactory.addFactory( new SvgReplacedElementFactory() );
      creFactory.addFactory( new SwingReplacedElementFactory() );

      final var context = mView.getSharedContext();
      final var textRenderer = context.getTextRenderer();
      context.setReplacedElementFactory( creFactory );
      textRenderer.setSmoothingThreshold( 0 );

      localeProperty().addListener( ( c, o, n ) -> {
        mLocaleUrl = toUrl( getLocale() );
        rerender();
      } );

      fontSizeProperty().addListener( ( c, o, n ) -> rerender() );
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

  private void rerender() {
    render( mHtmlDocument.toString() );
  }

  /**
   * Attaches the HTML head prefix and HTML tail suffix to the given HTML
   * string.
   *
   * @param html The HTML to adorn with opening and closing tags.
   * @return A complete HTML document, ready for rendering.
   */
  private String decorate( final String html ) {
    mHtmlDocument.setLength( 0 );
    mHtmlDocument.append( html );
    return head() + mHtmlDocument + tail();
  }

  private String head() {
    return format(
      HTML_HEAD,
      getLocale().getLanguage(),
      HTML_STYLE_PREVIEW,
      mLocaleUrl,
      getFontSize(),
      mBaseUriPath
    );
  }

  private String tail() {
    return HTML_TAIL;
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

  /**
   * Returns the ISO 639 alpha-2 or alpha-3 language code followed by a hyphen
   * followed by the ISO 15924 alpha-4 script code, followed by an ISO 3166
   * alpha-2 country code or UN M.49 numeric-3 area code. For example, this
   * could return "en-Latn-CA" for Canadian English written in the Latin
   * character set.
   *
   * @return Unique identifier for language and country.
   */
  private static URL toUrl( final Locale locale ) {
    return toUrl(
      get(
        sSettings.getSetting( STYLESHEET_PREVIEW_LOCALE, "" ),
        locale.getLanguage(),
        locale.getScript(),
        locale.getCountry()
      )
    );
  }

  private static URL toUrl( final String path ) {
    return HtmlPreview.class.getResource( path );
  }

  private Locale getLocale() {
    return localeProperty().toLocale();
  }

  private LocaleProperty localeProperty() {
    return mWorkspace.localeProperty( KEY_UI_FONT_LOCALE );
  }

  private double getFontSize() {
    return fontSizeProperty().get();
  }

  private DoubleProperty fontSizeProperty() {
    return mWorkspace.doubleProperty( KEY_UI_FONT_PREVIEW_SIZE );
  }
}
