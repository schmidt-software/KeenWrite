/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.fonts;

import javafx.scene.Node;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import static org.controlsfx.glyphfont.FontAwesome.Glyph.valueOf;

/**
 * Responsible for creating FontAwesome glyphs and graphics.
 */
public class IconFactory {

  /**
   * Singleton to prevent re-loading the TTF file.
   */
  private static final FontAwesome FONT_AWESOME = new FontAwesome();

  /**
   * Prevent instantiation. Use the {@link #createGraphic(String)} method to
   * create an icon for display.
   */
  private IconFactory() {}

  /**
   * Create a {@link Node} representation for the given icon name.
   *
   * @param icon Name of icon to convert to a UI object (case-insensitive).
   * @return A UI object suitable for display.
   */
  public static Node createGraphic( final String icon ) {
    assert icon != null;

    // Return a label glyph.
    return icon.isEmpty()
      ? new Glyph()
      : createGlyph( icon );
  }

  /**
   * Create a {@link Node} representation for the given FontAwesome glyph.
   *
   * @param glyph The glyph to convert to a {@link Node}.
   * @return The given glyph as a text label.
   */
  public static Node createGraphic( final FontAwesome.Glyph glyph ) {
    return FONT_AWESOME.create( glyph );
  }

  private static Node createGlyph( final String icon ) {
    return createGraphic( valueOf( icon.toUpperCase() ) );
  }
}
