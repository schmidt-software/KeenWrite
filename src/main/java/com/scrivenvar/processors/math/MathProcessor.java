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
 * <p>
 * This class is not thread-safe due to the reuse of an internal buffer to
 * prevent memory reallocations on subsequent runs of the processor.
 * </p>
 */
public class MathProcessor extends AbstractProcessor<String> {

  /**
   * Non-greedy match of math expressions.
   */
  private static final String REGEX = "(\\$(.*?)\\$)";

  /**
   * Compiled regular expression for matching math expression.
   */
  private static final Pattern REGEX_PATTERN = compile( REGEX );

  private static final int GROUP_DELIMITED = 2;

  private static final float mSize = 20f;

  /**
   * Reduces number of memory reallocations as formulas are added or removed
   * on subsequent calls to {@link #apply(String)}. This only shaves a few
   * milliseconds off the total time.
   */
  private final StringBuilder mBuffer = new StringBuilder( 65535 );

  private final TeXFont mTeXFont = new DefaultTeXFont( mSize );
  private final TeXEnvironment mEnvironment = new TeXEnvironment( mTeXFont );
  private final SvgGraphics2D mGraphics = new SvgGraphics2D();

  public MathProcessor( final Processor<String> successor ) {
    super( successor );
    mGraphics.scale( mSize, mSize );
  }

  /**
   * This method only takes a few seconds to generate
   *
   * @param s The string containing zero or more math formula bracketed by
   *          dollar symbols.
   * @return The given string with all formulas transformed to SVG format.
   */
  @Override
  public String apply( final String s ) {
    final var matcher = REGEX_PATTERN.matcher( s );
    int index = 0;

    // Wipe out previous data, but don't deallocate previous memory.
    mBuffer.setLength( 0 );

    while( matcher.find() ) {
      final var equation = matcher.group( GROUP_DELIMITED );

      final var formula = new TeXFormula( equation );
      final var box = formula.createBox( mEnvironment );
      final var layout = new TeXLayout( box, mSize );

      mGraphics.setDimensions( layout.getWidth(), layout.getHeight() );
      box.draw( mGraphics, layout.getX(), layout.getY() );

      mBuffer.append( s, index, matcher.start() );
      mBuffer.append( mGraphics );
      index = matcher.end();
    }

    return mBuffer.append( s, index, s.length() ).toString();
  }
}
