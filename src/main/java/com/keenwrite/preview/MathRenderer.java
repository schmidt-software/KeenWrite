/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.preview;

import com.whitemagicsoftware.tex.*;
import com.whitemagicsoftware.tex.graphics.SvgDomGraphics2D;
import org.w3c.dom.Document;

import java.util.function.Supplier;

import static com.keenwrite.StatusBarNotifier.alert;

/**
 * Responsible for rendering formulas as scalable vector graphics (SVG).
 */
public class MathRenderer {

  /**
   * Default font size in points.
   */
  private static final float FONT_SIZE = 20f;

  private final TeXFont mTeXFont = createDefaultTeXFont( FONT_SIZE );
  private final TeXEnvironment mEnvironment = createTeXEnvironment( mTeXFont );
  private final SvgDomGraphics2D mGraphics = createSvgDomGraphics2D();

  public MathRenderer() {
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
      alert( ex );
      return null;
    }
  }
}
