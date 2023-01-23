/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.dom.DocumentParser;
import com.whitemagicsoftware.keentype.lib.KeenType;
import org.w3c.dom.Document;

import static com.keenwrite.events.StatusEvent.clue;

/**
 * Responsible for rendering formulas as scalable vector graphics (SVG).
 */
public final class MathRenderer {

  private static KeenType sTypesetter;

  static {
    try {
      sTypesetter = new KeenType();
    } catch( final Exception e ) {
      clue( e );
    }
  }

  private MathRenderer() { }

  /**
   * This method only takes a few seconds to generate
   *
   * @param equation A mathematical expression to render.
   * @return The given string with all formulas transformed into SVG format.
   */
  public static Document toDocument( final String equation ) {
    return DocumentParser.parse( toString( equation ) );
  }

  public static String toString( final String equation ) {
    return sTypesetter.toSvg( "$" + equation + "$" );
  }
}
