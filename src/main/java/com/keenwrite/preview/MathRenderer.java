/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.whitemagicsoftware.tex.*;
import com.whitemagicsoftware.tex.graphics.SvgDomGraphics2D;
import org.w3c.dom.Document;

import java.util.function.Supplier;

import static com.keenwrite.StatusBarNotifier.clue;

/**
 * Responsible for rendering formulas as scalable vector graphics (SVG).
 */
public class MathRenderer {

  /**
   * Singleton instance for rendering math symbols.
   */
  public static final MathRenderer MATH_RENDERER = new MathRenderer();

  /**
   * Default font size in points.
   */
  private static final float FONT_SIZE = 20f;

  private final TeXFont mTeXFont = createDefaultTeXFont( FONT_SIZE );
  private final TeXEnvironment mEnvironment = createTeXEnvironment( mTeXFont );
  private final SvgDomGraphics2D mGraphics = createSvgDomGraphics2D();

  private MathRenderer() {
    mGraphics.scale( FONT_SIZE, FONT_SIZE );
  }

  /**
   * This method only takes a few seconds to generate
   *
   * @param equation A mathematical expression to render.
   * @return The given string with all formulas transformed into SVG format.
   */
  public Document render( final String equation ) {
    final var formula = new TeXFormula( equation );
    final var box = formula.createBox( mEnvironment );
    final var l = new TeXLayout( box, FONT_SIZE );

    mGraphics.initialize( l.getWidth(), l.getHeight() );
    box.draw( mGraphics, l.getX(), l.getY() );
    return mGraphics.toDom();
  }

  @SuppressWarnings("SameParameterValue")
  private TeXFont createDefaultTeXFont( final float fontSize ) {
    return create( () -> new DefaultTeXFont( fontSize ) );
  }

  private TeXEnvironment createTeXEnvironment( final TeXFont texFont ) {
    return create( () -> new TeXEnvironment( texFont ) );
  }

  private SvgDomGraphics2D createSvgDomGraphics2D() {
    return create( SvgDomGraphics2D::new );
  }

  /**
   * Tries to instantiate a given object, returning {@code null} on failure.
   * The failure message is bubbled up to to the user interface.
   *
   * @param supplier Creates an instance.
   * @param <T>      The type of instance being created.
   * @return An instance of the parameterized type or {@code null} upon error.
   */
  private <T> T create( final Supplier<T> supplier ) {
    try {
      return supplier.get();
    } catch( final Exception ex ) {
      clue( ex );
      return null;
    }
  }
}
