package com.keenwrite.preview.images;

import java.awt.image.BufferedImage;

/**
 * Unused. Needs to extract image data from {@link BufferedImage} and create
 * down-sampled version.
 */
public class Lanczos3 {
  static double sinc( double x ) {
    x *= Math.PI;

    if( x < 0.01f && x > -0.01f ) {
      return 1.0f + x * x * (-1.0f / 6.0f + x * x * 1.0f / 120.0f);
    }

    return Math.sin( x ) / x;
  }

  static float clip( double t ) {
    final float eps = .0000125f;

    if( Math.abs( t ) < eps ) { return 0.0f; }

    return (float) t;
  }

  static float lancos( float t ) {
    if( t < 0.0f ) { t = -t; }

    if( t < 3.0f ) { return clip( sinc( t ) * sinc( t / 3.0f ) ); }
    else { return 0.0f; }
  }

  static float lancos3_resample_x(
    int[][] arr, int src_w, int src_h, int y, int x, float xscale ) {
    float s = 0;
    float coef_sum = 0.0f;
    float coef;
    float pix;
    int i;

    int l, r;
    float c;
    float hw;

    // For the reduction of the situation hw is equivalent to expanding the
    // number of pixels in the field, if you do not do this, the final
    // reduction of the image effect is not much different from the recent
    // field interpolation method, the effect is equivalent to the first
    // low-pass filtering, and then interpolate
    if( xscale > 1.0f ) { hw = 3.0f; }
    else { hw = 3.0f / xscale; }

    c = (float) x / xscale;
    l = (int) Math.floor( c - hw );
    r = (int) Math.ceil( c + hw );

    if( y < 0 ) { y = 0; }
    if( y >= src_h ) { y = src_h - 1; }
    if( xscale > 1.0f ) { xscale = 1.0f; }
    for( i = l; i <= r; i++ ) {
      x = Math.max( i, 0 );
      if( i >= src_w ) { x = src_w - 1; }
      pix = arr[ y ][ x ];
      coef = lancos( (c - i) * xscale );
      s += pix * coef;
      coef_sum += coef;
    }
    s /= coef_sum;
    return s;
  }

  static class uint8_2d {
    int[][] arr;
    int rows;
    int cols;

    public uint8_2d( final int h1, final int w1 ) {
      arr = new int[ h1 ][ w1 ];
      rows = h1;
      cols = w1;
    }
  }

  void img_resize_using_lancos3( uint8_2d src, uint8_2d dst ) {
    if( src == null || dst == null ) { return; }

    int src_rows, src_cols;
    int dst_rows, dst_cols;
    int i, j;
    int[][] src_arr;
    int[][] dst_arr;
    float xratio;
    float yratio;
    int val;
    int k;
    float hw;

    src_arr = src.arr;
    dst_arr = dst.arr;
    src_rows = src.rows;
    src_cols = src.cols;
    dst_rows = dst.rows;
    dst_cols = dst.cols;

    xratio = (float) dst_cols / (float) src_cols;
    yratio = (float) dst_rows / (float) src_rows;

    float scale;

    if( yratio > 1.0f ) {
      hw = 3.0f;
      scale = 1.0f;
    }
    else {
      hw = 3.0f / yratio;
      scale = yratio;
    }

    for( i = 0; i < dst_rows; i++ ) {
      for( j = 0; j < dst_cols; j++ ) {
        int t, b;
        float c;

        float s = 0;
        float coef_sum = 0.0f;
        float coef;
        float pix;

        c = (float) i / yratio;
        t = (int) Math.floor( c - hw );
        b = (int) Math.ceil( c + hw );
        // Interpolate in the x direction first, then interpolate in the y
        // direction.
        for( k = t; k <= b; k++ ) {
          pix = lancos3_resample_x( src_arr, src_cols, src_rows, k, j, xratio );
          coef = lancos( (c - k) * scale );
          coef_sum += coef;
          pix *= coef;
          s += pix;
        }
        val = (int) (s / coef_sum);
        if( val < 0 ) { val = 0; }
        if( val > 255 ) { val = 255; }
        dst_arr[ i ][ j ] = val;
      }
    }
  }

  BufferedImage test_lancos3_resize( BufferedImage img, float factor ) {
    assert img != null;

    uint8_2d r = null;
    uint8_2d g = null;
    uint8_2d b = null;

    BufferedImage out = null;
    // TODO: Split buffered image into RGB components.
    //split_img_data( img, r, g, b );

    int w, h;
    int w1, h1;
    w = img.getWidth();
    h = img.getHeight();

    // TODO: Maintain aspect ratio.
    w1 = (int) (factor * w);
    h1 = (int) (factor * h);

    uint8_2d r1 = new uint8_2d( h1, w1 );
    uint8_2d g1 = new uint8_2d( h1, w1 );
    uint8_2d b1 = new uint8_2d( h1, w1 );

    img_resize_using_lancos3( r, r1 );
    img_resize_using_lancos3( g, g1 );
    img_resize_using_lancos3( b, b1 );

    // TODO: Combine rescaled image into RGB components.
    //merge_img_data( r1, g1, b1, out);

    return out;
  }
}
