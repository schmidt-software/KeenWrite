/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import com.whitemagicsoftware.keenquotes.Converter;

import static com.keenwrite.events.StatusEvent.clue;
import static com.whitemagicsoftware.keenquotes.Converter.CHARS;
import static com.whitemagicsoftware.keenquotes.ParserFactory.ParserType.PARSER_XML;

/**
 * Responsible for curling straight quotes. This class will curl as many
 * straight quotes within HTML elements as possible. First, any straight
 * quotes within preformatted elements are converted into reserved Unicode
 * characters. That is, <code>'</code> becomes code point U+FFFE and
 * <code>"</code> becomes code point U+FFFF. After the substitution is made,
 * all block-level elements are curled, then the code points are restored.
 * </p>
 */
public class Typographer {

  private final static Converter sConverter =
    new Converter( lex -> clue( lex.toString() ), CHARS, PARSER_XML );

  private Typographer() {
  }

  public static String curl( final String xhtml ) {
    return sConverter.apply( xhtml );
  }
}
