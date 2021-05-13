/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package com.keenwrite.preview.images;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

/**
 * @author Morten Nobel-Joergensen
 */
public abstract class AdvancedResizeOp implements BufferedImageOp {
  private final ConstrainedDimension dimensionConstrain;

  public AdvancedResizeOp( ConstrainedDimension dimensionConstrain ) {
    this.dimensionConstrain = dimensionConstrain;
  }

  public final BufferedImage filter( BufferedImage src, BufferedImage dest ) {
    Dimension dstDimension = dimensionConstrain.getDimension(
      new Dimension( src.getWidth(), src.getHeight() ) );
    int dstWidth = dstDimension.width;
    int dstHeight = dstDimension.height;

    return doFilter( src, dest, dstWidth, dstHeight );
  }

  protected abstract BufferedImage doFilter(
    BufferedImage src, BufferedImage dest, int dstWidth, int dstHeight );

  @Override
  public final Rectangle2D getBounds2D( BufferedImage src ) {
    return new Rectangle( 0, 0, src.getWidth(), src.getHeight() );
  }

  @Override
  public final BufferedImage createCompatibleDestImage(
    BufferedImage src, ColorModel destCM ) {
    if( destCM == null ) {
      destCM = src.getColorModel();
    }

    return new BufferedImage(
      destCM,
      destCM.createCompatibleWritableRaster( src.getWidth(), src.getHeight() ),
      destCM.isAlphaPremultiplied(),
      null );
  }

  @Override
  public final Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
    return (Point2D) srcPt.clone();
  }

  @Override
  public final RenderingHints getRenderingHints() {
    return null;
  }
}
