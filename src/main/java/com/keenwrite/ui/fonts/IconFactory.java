/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.fonts;

import com.keenwrite.io.MediaType;
import com.keenwrite.io.MediaTypeExtension;
import com.keenwrite.io.SysFile;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.MediaTypeExtension.MEDIA_UNDEFINED;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.preview.SvgRasterizer.rasterize;
import static java.awt.Font.*;
import static java.nio.file.Files.readAttributes;
import static javafx.embed.swing.SwingFXUtils.toFXImage;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.controlsfx.glyphfont.FontAwesome.Glyph.valueOf;

/**
 * Responsible for creating FontAwesome glyphs and graphics.
 */
public class IconFactory {
  /**
   * File icon height, in pixels.
   */
  private static final int ICON_HEIGHT = 16;

  /**
   * Singleton to prevent re-loading the TTF file.
   */
  private static final FontAwesome FONT_AWESOME = new FontAwesome();

  /**
   * Caches file type icons encountered.
   */
  private static final Map<String, Image> ICONS = new HashMap<>();

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

  /**
   * Creates a suitable {@link Node} icon representation for the given file.
   * This will first look up the {@link MediaType} before matching based on
   * the file name extension.
   *
   * @param path The file to represent graphically.
   * @return An icon representation for the given file.
   */
  public static ImageView createFileIcon( final Path path ) throws IOException {
    final var attrs = readAttributes( path, BasicFileAttributes.class );
    final var filename = SysFile.getFileName( path );
    String extension;

    if( "..".equals( filename ) ) {
      extension = "folder-up";
    }
    else if( attrs.isDirectory() ) {
      extension = "folder";
    }
    else if( attrs.isSymbolicLink() ) {
      extension = "folder-link";
    }
    else {
      final var mediaType = MediaType.fromFilename( path );
      final var mte = MediaTypeExtension.valueFrom( mediaType );

      // if the file extension is not known to the app, try loading an icon
      // that corresponds to the extension directly.
      extension = mte == MEDIA_UNDEFINED
        ? getExtension( filename )
        : mte.getExtension();
    }

    if( extension == null ) {
      extension = "";
    }
    else {
      extension = extension.toLowerCase();
    }

    // Each cell in the table must have a distinct parent, so the image views
    // cannot be reused. The underlying buffered image can be cached, though.
    final var image =
      ICONS.computeIfAbsent( extension, IconFactory::createFxImage );
    final var imageView = new ImageView();
    imageView.setPreserveRatio( true );
    imageView.setFitHeight( ICON_HEIGHT );
    imageView.setImage( image );

    return imageView;
  }

  private static Image createFxImage( final String extension ) {
    return toFXImage( createImage( extension ), null );
  }

  private static BufferedImage createImage( final String extension ) {
    try( final var icon = open( "icons/" + extension + ".svg" ) ) {
      if( icon == null ) {
        throw new IllegalArgumentException( extension );
      }

      return rasterize( icon );
    } catch( final Exception ex ) {
      clue( ex );

      // If the extension was unknown, fall back to a blank icon, falling
      // back again to a broken image if blank cannot be found (to avoid
      // infinite recursion).
      return "blank".equals( extension )
        ? BROKEN_IMAGE_PLACEHOLDER
        : createImage( "blank" );
    }
  }

  private static InputStream open( final String resource ) {
    return IconFactory.class.getResourceAsStream( resource );
  }

  /**
   * Returns the font to use when adding icons to the UI.
   *
   * @param size The font size to use when drawing the icon.
   * @return A font containing numerous icons.
   */
  public static Font getIconFont( final int size ) {
    try( final var fontStream = openFont() ) {
      final var font = createFont( TRUETYPE_FONT, fontStream );
      return font.deriveFont( PLAIN, size );
    } catch( final Exception e ) {
      // This doesn't actually work, seemingly after an upgrade to ControlsFX.
      // As such, creating the font and deriving it will work.
      return new Font( FONT_AWESOME.getName(), PLAIN, size );
    }
  }

  /**
   * This re-reads the {@link FontAwesome} font TTF resource. For a reason
   * not yet investigated, the font doesn't appear to be accessible to the
   * application. This may have happened during an upgrade to ControlsFX.
   * Callers are responsible for closing the stream.
   *
   * @return A stream containing font TrueType glyph information.
   */
  private static InputStream openFont() {
    return FontAwesome.class.getResourceAsStream( "fontawesome-webfont.ttf" );
  }

  private static Node createGlyph( final String icon ) {
    return createGraphic( valueOf( icon.toUpperCase() ) );
  }

  /**
   * Prevent instantiation. Use the {@link #createGraphic(String)} method to
   * create an icon for display.
   */
  private IconFactory() { }
}
