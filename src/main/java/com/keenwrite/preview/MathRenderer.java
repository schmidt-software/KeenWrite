/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.dom.DocumentParser;
import com.whitemagicsoftware.keentype.lib.KeenType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.w3c.dom.Document;

import static com.keenwrite.events.StatusEvent.clue;

/**
 * Responsible for rendering formulas as scalable vector graphics (SVG).
 */
public final class MathRenderer {

  private static KeenType sTypesetter;

  static {
    try {
      sTypesetter = new KeenType( false );
    } catch( final Exception e ) {
      clue( e );
    }
  }

  private static final DoubleProperty sSize = new SimpleDoubleProperty( 2 );

  private MathRenderer() { }

  public static void bindSize( final DoubleProperty size ) {
    sSize.bind( size );
  }

  /**
   * Converts a TeX-based equation into an SVG document.
   *
   * @param equation A mathematical expression to render, without sigils.
   * @return The given string with all formulas transformed into SVG format.
   */
  public static Document toDocument( final String equation ) {
    return DocumentParser.parse( toString( equation ) );
  }

  /**
   * Converts a TeX-based equation into an SVG document.
   *
   * @param equation A mathematical expression to render, without sigils.
   * @return The given string with all formulas transformed into SVG format.
   */
  public static String toString( final String equation ) {
    return sTypesetter.toSvg( "$" + equation + "$", sSize.doubleValue() );
  }
}
