/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import com.keenwrite.preview.HtmlPreview;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static com.keenwrite.Constants.FONT_DIRECTORY;
import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.util.ProtocolScheme.valueFrom;
import static com.keenwrite.util.ResourceWalker.GLOB_FONTS;
import static com.keenwrite.util.ResourceWalker.walk;
import static java.awt.Font.TRUETYPE_FONT;
import static java.awt.Font.createFont;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import static java.awt.font.TextAttribute.*;

/**
 * Responsible for loading fonts into the application's
 * {@link GraphicsEnvironment} so that the {@link HtmlPreview} can display
 * the text using a non-system font.
 */
public final class FontLoader {

  /**
   * Walks the resources associated with the application to load all TrueType
   * font resources found. This method must run before the windowing system
   * kicks in, otherwise the fonts will not be found.
   * <p>
   * All fonts must be TrueType fonts. No PostScript Type 1 fonts are
   * supported.
   * </p>
   */
  @SuppressWarnings( "unchecked" )
  public static void initFonts() {
    try {
      final var ge = getLocalGraphicsEnvironment();
      walk(
        FONT_DIRECTORY, GLOB_FONTS, path -> {
          final var uri = path.toUri();
          final var filename = path.toString();

          try( final var is = openFont( uri, filename ) ) {
            final var font = createFont( TRUETYPE_FONT, is );
            final var attributes =
              (Map<TextAttribute, Integer>) font.getAttributes();

            attributes.put( LIGATURES, LIGATURES_ON );
            attributes.put( KERNING, KERNING_ON );
            ge.registerFont( font.deriveFont( attributes ) );
          } catch( final Exception ex ) {
            clue( ex );
          }
        }
      );
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  /**
   * Attempts to open a font, regardless of whether the font is a resource in
   * a JAR file or somewhere on the file system.
   *
   * @param uri      Directory or archive containing a font.
   * @param filename Name of the font file.
   * @return An open file handled to the font.
   * @throws IOException Could not open the resource as a stream.
   */
  private static InputStream openFont( final URI uri, final String filename )
    throws IOException {
    return valueFrom( uri ).isJar()
      ? FontLoader.class.getResourceAsStream( filename )
      : new FileInputStream( filename );
  }
}
