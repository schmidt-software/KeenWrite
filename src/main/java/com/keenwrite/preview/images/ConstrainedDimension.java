/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package com.keenwrite.preview.images;

import java.awt.*;

/**
 * This class let you create dimension constrains based on a actual image.
 */
public class ConstrainedDimension {
  private ConstrainedDimension() {
  }

  /**
   * Will always return a dimension with positive width and height;
   *
   * @param dimension of the unscaled image
   * @return the dimension of the scaled image
   */
  public Dimension getDimension( Dimension dimension ) {
    return dimension;
  }

  /**
   * Used when the destination size is fixed. This may not keep the image
   * aspect radio.
   *
   * @param width  destination dimension width
   * @param height destination dimension height
   * @return destination dimension (width x height)
   */
  public static ConstrainedDimension createAbsolutionDimension(
    final int width, final int height ) {
    assert width > 0 && height > 0 : "Dimensions must be positive integers";
    return new ConstrainedDimension() {
      public Dimension getDimension( Dimension dimension ) {
        return new Dimension( width, height );
      }
    };
  }
}
