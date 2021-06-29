/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import com.whitemagicsoftware.keenquotes.Converter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static com.keenwrite.dom.DocumentParser.walk;
import static com.keenwrite.events.StatusEvent.clue;
import static com.whitemagicsoftware.keenquotes.Converter.CHARS;

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

  //@formatter:off
  private final static String UNTOUCHABLE =
    "//pre | //code | //tt | //tex | //kbd | //samp | //var | //l | //blockcode";
  //@formatter:on

  private final static char[] SINGLE = {'\'', '\uFFFE'};
  private final static char[] DOUBLE = {'"', '\uFFFF'};

  private final static Converter sConverter =
    new Converter( lex -> clue( lex.toString() ), CHARS );

  private Typographer() {
  }

  public static void curl( final Document xhtml ) {
    walk( xhtml, UNTOUCHABLE, node -> replace( node, true ) );
    walk(
      xhtml,
      "//*[normalize-space( text() ) != '']",
      node -> node.setTextContent( sConverter.apply( node.getTextContent() ) )
    );
    walk( xhtml, UNTOUCHABLE, node -> replace( node, false ) );
  }

  private static void replace( final Node node, final boolean encode ) {
    final var text = node.getTextContent();

    if( text != null ) {
      final var a = encode ? 0 : 1;
      final var b = encode ? 1 : 0;
      node.setTextContent( text.replace( SINGLE[ a ], SINGLE[ b ] ) );
      node.setTextContent( text.replace( DOUBLE[ a ], DOUBLE[ b ] ) );
    }
  }
}
