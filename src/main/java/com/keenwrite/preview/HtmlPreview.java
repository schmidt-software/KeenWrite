/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.dom.DocumentConverter;
import com.keenwrite.events.DocumentChangedEvent;
import com.keenwrite.events.ScrollLockEvent;
import com.keenwrite.preferences.LocaleProperty;
import com.keenwrite.preferences.Workspace;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingNode;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;

import static com.keenwrite.constants.Constants.*;
import static com.keenwrite.events.Bus.register;
import static com.keenwrite.events.ScrollLockEvent.fireScrollLockEvent;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preferences.AppKeys.*;
import static com.keenwrite.ui.fonts.IconFactory.getIconFont;
import static java.awt.BorderLayout.*;
import static java.awt.event.KeyEvent.*;
import static java.lang.String.format;
import static javafx.scene.CacheHint.SPEED;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingUtilities.invokeLater;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.LOCK;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.UNLOCK_ALT;

/**
 * Responsible for parsing an HTML document.
 */
public final class HtmlPreview extends SwingNode implements ComponentListener {
  /**
   * Converts a text string to a structured HTML document.
   */
  private static final DocumentConverter CONVERTER = new DocumentConverter();

  /**
   * Used to populate the {@link #HTML_HEAD} with stylesheet file references.
   */
  private static final String HTML_STYLESHEET =
    "<link rel='stylesheet' href='%s'/>";

  private static final String HTML_BASE =
    "<base href='%s'/>";

  /**
   * Render CSS using points (pt) not pixels (px) to reduce the chance of
   * poor rendering. The {@link #generateHead()} method fills placeholders.
   * When the user has not set a locale, only one stylesheet is added to
   * the document. In order, the placeholders are as follows:
   * <ol>
   * <li>%s --- language</li>
   * <li>%s --- default stylesheet</li>
   * <li>%s --- language-specific stylesheet</li>
   * <li>%s --- user-customized stylesheet</li>
   * <li>%s --- font family</li>
   * <li>%d --- font size (must be pixels, not points due to bug)</li>
   * <li>%s --- base href</li>
   * </p>
   */
  private static final String HTML_HEAD = """
    <!doctype html>
    <html lang='%s'><head><title> </title><meta charset='utf-8'/>
    %s%s%s<style>body{font-family:'%s';font-size: %dpx;}</style>%s</head><body>
    """;

  private static final String HTML_TAIL = "</body></html>";

  private static final URL HTML_STYLE_PREVIEW = toUrl( STYLESHEET_PREVIEW );

  /**
   * Reusing this buffer prevents repetitious memory re-allocations.
   */
  private final StringBuilder mDocument = new StringBuilder( 65536 );

  private HtmlRenderer mPreview;
  private JScrollPane mScrollPane;
  private String mBaseUriPath = "";
  private String mHead;

  private volatile boolean mScrollLocked;
  private final JButton mScrollLockButton = new JButton();
  private final Workspace mWorkspace;

  /**
   * Creates a new preview pane that can scroll to the caret position within the
   * document.
   *
   * @param workspace Contains locale and font size information.
   */
  public HtmlPreview( final Workspace workspace ) {
    mWorkspace = workspace;
    mHead = generateHead();

    // Attempts to prevent a flash of black un-styled content upon load.
    setStyle( "-fx-background-color: white;" );

    invokeLater( () -> {
      mPreview = new FlyingSaucerPanel();
      mScrollPane = new JScrollPane( (Component) mPreview );
      final var verticalBar = mScrollPane.getVerticalScrollBar();
      final var verticalPanel = new JPanel( new BorderLayout() );

      final var map = verticalBar.getInputMap( WHEN_IN_FOCUSED_WINDOW );
      addKeyboardEvents( map );

      mScrollLockButton.setFont( getIconFont( 14 ) );
      mScrollLockButton.setText( getLockText( mScrollLocked ) );
      mScrollLockButton.setMargin( new Insets( 1, 0, 0, 0 ) );
      mScrollLockButton.addActionListener(
        _ -> fireScrollLockEvent( !mScrollLocked )
      );

      verticalPanel.add( verticalBar, CENTER );
      verticalPanel.add( mScrollLockButton, PAGE_END );

      final var wrapper = new JPanel( new BorderLayout() );
      wrapper.add( mScrollPane, CENTER );
      wrapper.add( verticalPanel, LINE_END );

      // Enabling the cache attempts to prevent black flashes when resizing.
      setCache( true );
      setCacheHint( SPEED );
      setContent( wrapper );
      wrapper.addComponentListener( this );
    } );

    localeProperty().addListener( ( c, o, n ) -> rerender() );
    fontFamilyProperty().addListener( ( c, o, n ) -> rerender() );
    fontSizeProperty().addListener( ( c, o, n ) -> rerender() );

    register( this );
  }

  @Subscribe
  public void handle( final ScrollLockEvent event ) {
    mScrollLocked = event.isLocked();
    invokeLater(
      () -> mScrollLockButton.setText( getLockText( mScrollLocked ) )
    );
  }

  /**
   * Updates the internal HTML source shown in the preview pane.
   *
   * @param html The new HTML document to display.
   */
  public void render( final String html ) {
    final var jsoupDoc = DocumentConverter.parse( decorate( html ) );
    final var doc = CONVERTER.fromJsoup( jsoupDoc );
    final var uri = getBaseUri();

    doc.setDocumentURI( uri );
    invokeLater( () -> mPreview.render( doc, uri ) );
    DocumentChangedEvent.fire( html );
  }

  /**
   * Clears the caches then re-renders the content.
   */
  public void refresh() {
    mPreview.clearCache();
    rerender();
  }

  /**
   * Recomputes the HTML head then renders the document.
   */
  private void rerender() {
    mHead = generateHead();
    render( mDocument.toString() );
  }

  /**
   * Attaches the HTML head prefix and HTML tail suffix to the given HTML
   * string.
   *
   * @param html The HTML to adorn with opening and closing tags.
   * @return A complete HTML document, ready for rendering.
   */
  private String decorate( final String html ) {
    mDocument.setLength( 0 );
    mDocument.append( html );

    // Head and tail must be separate from document due to re-rendering.
    return mHead + mDocument + HTML_TAIL;
  }

  /**
   * Called when settings are changed that affect the HTML document preamble.
   * This is a minor performance optimization to avoid generating the head
   * each time that the document itself changes.
   *
   * @return A new doctype and HTML {@code head} element.
   */
  private String generateHead() {
    final var locale = getLocale();
    final var base = getBaseUri();
    final var custom = getCustomStylesheetUrl();

    // Point sizes are converted to pixels because of a rendering bug.
    return format(
      HTML_HEAD,
      locale.getLanguage(),
      toStylesheetString( HTML_STYLE_PREVIEW ),
      toStylesheetString( toUrl( locale ) ),
      toStylesheetString( custom ),
      getFontFamily(),
      toPixels( getFontSize() ),
      base.isBlank() ? "" : format( HTML_BASE, base )
    );
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
   * waiting for the document to be ready.
   *
   * @param id Scroll the preview pane to this unique paragraph identifier.
   */
  public void scrollTo( final String id ) {
    if( !mScrollLocked ) {
      mPreview.scrollTo( id, mScrollPane );
      mScrollPane.repaint();
    }
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

  /**
   * Returns the ISO 639 alpha-2 or alpha-3 language code followed by a hyphen
   * followed by the ISO 15924 alpha-4 script code, followed by an ISO 3166
   * alpha-2 country code or UN M.49 numeric-3 area code. For example, this
   * could return {@code en-Latn-CA} for Canadian English written in the Latin
   * character set.
   *
   * @return Unique identifier for language and country.
   */
  private static URL toUrl( final Locale locale ) {
    return toUrl(
      String.format(
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
    return mWorkspace.localeProperty( KEY_LANGUAGE_LOCALE );
  }

  private String getFontFamily() {
    return fontFamilyProperty().get();
  }

  private StringProperty fontFamilyProperty() {
    return mWorkspace.stringProperty( KEY_UI_FONT_PREVIEW_NAME );
  }

  private double getFontSize() {
    return fontSizeProperty().get();
  }

  /**
   * Returns the font size in points.
   *
   * @return The user-defined font size (in pt).
   */
  private DoubleProperty fontSizeProperty() {
    return mWorkspace.doubleProperty( KEY_UI_FONT_PREVIEW_SIZE );
  }

  private String getLockText( final boolean locked ) {
    return Character.toString( (locked ? LOCK : UNLOCK_ALT).getChar() );
  }

  private URL getCustomStylesheetUrl() {
    try {
      return mWorkspace.getFile( KEY_UI_PREVIEW_STYLESHEET ).toURI().toURL();
    } catch( final Exception ex ) {
      clue( ex );
      return null;
    }
  }

  /**
   * Maps keyboard events to scrollbar commands so that users may control
   * the {@link HtmlPreview} panel using the keyboard.
   *
   * @param map The map to update with keyboard events.
   */
  private void addKeyboardEvents( final InputMap map ) {
    map.put( getKeyStroke( VK_DOWN, 0 ), "positiveUnitIncrement" );
    map.put( getKeyStroke( VK_UP, 0 ), "negativeUnitIncrement" );
    map.put( getKeyStroke( VK_PAGE_DOWN, 0 ), "positiveBlockIncrement" );
    map.put( getKeyStroke( VK_PAGE_UP, 0 ), "negativeBlockIncrement" );
    map.put( getKeyStroke( VK_HOME, 0 ), "minScroll" );
    map.put( getKeyStroke( VK_END, 0 ), "maxScroll" );
  }

  @Override
  public void componentResized( final ComponentEvent e ) {
    if( mWorkspace.getBoolean( KEY_IMAGE_RESIZE ) ) {
      mPreview.clearCache();
    }

    // Force update on the Swing EDT, otherwise the scrollbar and content
    // will not be updated correctly on some platforms.
    invokeLater( () -> getContent().repaint() );
  }

  @Override
  public void componentMoved( final ComponentEvent e ) { }

  @Override
  public void componentShown( final ComponentEvent e ) { }

  @Override
  public void componentHidden( final ComponentEvent e ) { }

  private static String toStylesheetString( final URL url ) {
    return url == null ? "" : format( HTML_STYLESHEET, url );
  }
}
