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
package com.scrivenvar.processors.math;

import com.scrivenvar.processors.AbstractProcessor;
import com.scrivenvar.processors.Processor;
import com.whitemagicsoftware.tex.*;
import com.whitemagicsoftware.tex.graphics.SvgGraphics2D;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Responsible for parsing equations in a document and rendering them as
 * scalable vector graphics (SVG).
 */
public class MathProcessor extends AbstractProcessor<String> {

  /**
   * Non-greedy match of math expressions.
   */
  private static final String REGEX = "(\\$(.*?)\\$)";

  /**
   * Compiled regular expression for matching math expression.
   */
  public static final Pattern REGEX_PATTERN = compile( REGEX );

  private static final int GROUP_DELIMITED = 2;

  private final float mSize = 20f;
  private final TeXFont mTeXFont = new DefaultTeXFont( mSize );
  private final TeXEnvironment mEnvironment = new TeXEnvironment( mTeXFont );
  private final SvgGraphics2D mGraphics = new SvgGraphics2D();

  public MathProcessor( final Processor<String> successor ) {
    super( successor );
    mGraphics.scale( mSize, mSize );
  }

  @Override
  public String apply( final String s ) {
    final var result = new StringBuilder( s.length() * 2 );

    int index = 0;

    final var matcher = REGEX_PATTERN.matcher( s );

    while( matcher.find() ) {
      final var equation = matcher.group( GROUP_DELIMITED );

      final var formula = new TeXFormula( equation );
      final var box = formula.createBox( mEnvironment );
      final var layout = new TeXLayout( box, mSize );

      mGraphics.setDimensions( layout.getWidth(), layout.getHeight() );
      box.draw( mGraphics, layout.getX(), layout.getY() );

      result.append( s, index, matcher.start() );
      result.append( mGraphics );
      index = matcher.end();
    }

    result.append( s, index, s.length() );

    return result.toString();
  }
}
