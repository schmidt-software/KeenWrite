/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package com.keenwrite.preview.images;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static java.awt.image.BufferedImage.*;

/**
 * @author Heinz Doerr
 * @author Morten Nobel-Joergensen
 */
public final class ImageUtils {
  @SuppressWarnings( "DuplicateBranchesInSwitch" )
  static int nrChannels( final BufferedImage img ) {
    return switch( img.getType() ) {
      case TYPE_3BYTE_BGR -> 3;
      case TYPE_4BYTE_ABGR -> 4;
      case TYPE_BYTE_GRAY -> 1;
      case TYPE_INT_BGR -> 3;
      case TYPE_INT_ARGB -> 4;
      case TYPE_INT_RGB -> 3;
      case TYPE_CUSTOM -> 4;
      case TYPE_4BYTE_ABGR_PRE -> 4;
      case TYPE_INT_ARGB_PRE -> 4;
      case TYPE_USHORT_555_RGB -> 3;
      case TYPE_USHORT_565_RGB -> 3;
      case TYPE_USHORT_GRAY -> 1;
      default -> 0;
    };
  }

  /**
   * returns one row (height == 1) of byte packed image data in BGR or AGBR form
   *
   * @param temp must be either null or a array with length of w*h
   */
  static void getPixelsBGR(
    BufferedImage img, int y, int w, byte[] array, int[] temp ) {
    final int x = 0;
    final int h = 1;

    assert array.length == temp.length * nrChannels( img );
    assert (temp.length == w);

    final Raster raster;
    switch( img.getType() ) {
      case TYPE_3BYTE_BGR, TYPE_4BYTE_ABGR,
        TYPE_4BYTE_ABGR_PRE, TYPE_BYTE_GRAY -> {
        raster = img.getRaster();
        //int ttype= raster.getTransferType();
        raster.getDataElements( x, y, w, h, array );
      }
      case TYPE_INT_BGR -> {
        raster = img.getRaster();
        raster.getDataElements( x, y, w, h, temp );
        ints2bytes( temp, array, 0, 1, 2 );  // bgr -->  bgr
      }
      case TYPE_INT_RGB -> {
        raster = img.getRaster();
        raster.getDataElements( x, y, w, h, temp );
        ints2bytes( temp, array, 2, 1, 0 );  // rgb -->  bgr
      }
      case TYPE_INT_ARGB, TYPE_INT_ARGB_PRE -> {
        raster = img.getRaster();
        raster.getDataElements( x, y, w, h, temp );
        ints2bytes( temp, array, 2, 1, 0, 3 );  // argb -->  abgr
      }
      case TYPE_CUSTOM -> {
        // loader, but else ???
        img.getRGB( x, y, w, h, temp, 0, w );
        ints2bytes( temp, array, 2, 1, 0, 3 );  // argb -->  abgr
      }
      default -> {
        img.getRGB( x, y, w, h, temp, 0, w );
        ints2bytes( temp, array, 2, 1, 0 );  // rgb -->  bgr
      }
    }
  }

  /**
   * converts and copies byte packed  BGR or ABGR into the img buffer,
   * the img type may vary (e.g. RGB or BGR, int or byte packed)
   * but the number of components (w/o alpha, w alpha, gray) must match
   * <p>
   * does not unmange the image for all (A)RGN and (A)BGR and gray imaged
   */
  public static void setBGRPixels( byte[] bgrPixels, BufferedImage img, int x,
                                   int y, int w, int h ) {
    int imageType = img.getType();
    WritableRaster raster = img.getRaster();

    if( imageType == TYPE_3BYTE_BGR ||
      imageType == TYPE_4BYTE_ABGR ||
      imageType == TYPE_4BYTE_ABGR_PRE ||
      imageType == TYPE_BYTE_GRAY ) {
      raster.setDataElements( x, y, w, h, bgrPixels );
    }
    else {
      int[] pixels;
      if( imageType == TYPE_INT_BGR ) {
        pixels = bytes2int( bgrPixels, 2, 1, 0 );  // bgr -->  bgr
      }
      else if( imageType == TYPE_INT_ARGB ||
        imageType == TYPE_INT_ARGB_PRE ) {
        pixels = bytes2int( bgrPixels, 3, 0, 1, 2 );  // abgr -->  argb
      }
      else {
        pixels = bytes2int( bgrPixels, 0, 1, 2 );  // bgr -->  rgb
      }
      if( w == 0 || h == 0 ) {
        return;
      }
      else if( pixels.length < w * h ) {
        throw new IllegalArgumentException( "pixels array must have a length" + " >= w*h" );
      }
      if( imageType == TYPE_INT_ARGB ||
        imageType == TYPE_INT_RGB ||
        imageType == TYPE_INT_ARGB_PRE ||
        imageType == TYPE_INT_BGR ) {
        raster.setDataElements( x, y, w, h, pixels );
      }
      else {
        // Unmanages the image
        img.setRGB( x, y, w, h, pixels, 0, w );
      }
    }
  }

  public static void ints2bytes( int[] in, byte[] out, int index1, int index2,
                                 int index3 ) {
    for( int i = 0; i < in.length; i++ ) {
      int index = i * 3;
      int value = in[ i ];
      out[ index + index1 ] = (byte) value;
      value = value >> 8;
      out[ index + index2 ] = (byte) value;
      value = value >> 8;
      out[ index + index3 ] = (byte) value;
    }
  }

  public static void ints2bytes( int[] in, byte[] out, int index1, int index2,
                                 int index3, int index4 ) {
    for( int i = 0; i < in.length; i++ ) {
      int index = i * 4;
      int value = in[ i ];
      out[ index + index1 ] = (byte) value;
      value = value >> 8;
      out[ index + index2 ] = (byte) value;
      value = value >> 8;
      out[ index + index3 ] = (byte) value;
      value = value >> 8;
      out[ index + index4 ] = (byte) value;
    }
  }

  public static int[] bytes2int( byte[] in, int index1, int index2,
                                 int index3 ) {
    int[] out = new int[ in.length / 3 ];
    for( int i = 0; i < out.length; i++ ) {
      int index = i * 3;
      int b1 = (in[ index + index1 ] & 0xff) << 16;
      int b2 = (in[ index + index2 ] & 0xff) << 8;
      int b3 = in[ index + index3 ] & 0xff;
      out[ i ] = b1 | b2 | b3;
    }
    return out;
  }

  public static int[] bytes2int( byte[] in, int index1, int index2, int index3,
                                 int index4 ) {
    int[] out = new int[ in.length / 4 ];
    for( int i = 0; i < out.length; i++ ) {
      int index = i * 4;
      int b1 = (in[ index + index1 ] & 0xff) << 24;
      int b2 = (in[ index + index2 ] & 0xff) << 16;
      int b3 = (in[ index + index3 ] & 0xff) << 8;
      int b4 = in[ index + index4 ] & 0xff;
      out[ i ] = b1 | b2 | b3 | b4;
    }
    return out;
  }

  public static BufferedImage convert( BufferedImage src, int bufImgType ) {
    BufferedImage img = new BufferedImage( src.getWidth(),
                                           src.getHeight(),
                                           bufImgType );
    Graphics2D g2d = img.createGraphics();
    g2d.drawImage( src, 0, 0, null );
    g2d.dispose();
    return img;
  }
}
