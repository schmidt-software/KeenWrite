/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.constants;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

import static com.keenwrite.constants.Constants.get;

/**
 * Defines application-wide default values for GUI-related items. This helps
 * ensure that unit tests that have no graphical dependencies will pass.
 */
public class GraphicsConstants {
  public static final List<Image> LOGOS = createImages(
    "file.logo.16",
    "file.logo.32",
    "file.logo.128",
    "file.logo.256",
    "file.logo.512"
  );

  public static final Image ICON_DIALOG = LOGOS.get( 1 );

  public static final ImageView ICON_DIALOG_NODE = new ImageView( ICON_DIALOG );

  /**
   * Converts the given file names to images, such as application icons.
   *
   * @param keys The file names to convert to images.
   * @return The images loaded from the file name references.
   */
  private static List<Image> createImages( final String... keys ) {
    final List<Image> images = new ArrayList<>( keys.length );

    for( final var key : keys ) {
      images.add( new Image( get( key ) ) );
    }

    return images;
  }
}
