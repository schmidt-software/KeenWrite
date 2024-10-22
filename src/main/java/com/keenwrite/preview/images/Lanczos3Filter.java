/*
 * Copyright 2013, Morten Nobel-Joergensen
 *
 * License: The BSD 3-Clause License
 * http://opensource.org/licenses/BSD-3-Clause
 */
package com.keenwrite.preview.images;

public final class Lanczos3Filter implements ResampleFilter {
  private static final float PI_FLOAT = (float) Math.PI;

  private float sincModified( float value ) {
    return (float) Math.sin( value ) / value;
  }

  public float apply( float value ) {
    if( value == 0 ) {
      return 1.0f;
    }

    if( value < 0.0f ) {
      value = -value;
    }

    if( value < 3.0f ) {
      value *= PI_FLOAT;
      return sincModified( value ) * sincModified( value / 3.0f );
    }

    return 0.0f;
  }

  public float getSamplingRadius() {
    return 3.0f;
  }
}
