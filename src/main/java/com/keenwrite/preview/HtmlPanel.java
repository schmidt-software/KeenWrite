package com.keenwrite.preview;

import org.w3c.dom.Document;

import javax.swing.*;

public interface HtmlPanel {

  /**
   * Renders an HTML document with respect to a base location.
   *
   * @param doc     The document to render.
   * @param baseUri The document's relative URI.
   */
  void render( final Document doc, final String baseUri );

  /**
   * Scrolls the given {@link JScrollPane} to the first HTML element that
   * has an {@code id} attribute that matches the given identifier.
   *
   * @param id         The HTML element identifier.
   * @param scrollPane The GUI widget that controls scrolling.
   */
  void scrollTo( final String id, final JScrollPane scrollPane );

  /**
   * Clears the cache (e.g., so that images are re-rendered using updated
   * dimensions).
   */
  void clearCache();
}
