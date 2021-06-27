/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.quotes;

import org.w3c.dom.Document;

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

  private Typographer() {
  }

  public static void curl( final Document doc ) {
//    final var converter = new Converter( out::println );
//    final var preformat = doc.select( "pre, code, tt, tex, kbd, samp, var" );
//
//    replace( preformat, '\'', '\uFFFE' );
//    replace( preformat, '\"', '\uFFFF' );
//
//    for( final var e : doc.getAllElements() ) {
//      System.out.println( e.ownText() );
//      for(final var node : e.textNodes()) {
//        node.text( converter.apply( node.toString() ) );
//      }
//    }
//
//    //Parser.unescapeEntities( str, true)
//
//    replace( preformat, '\uFFFE', '\'' );
//    replace( preformat, '\uFFFF', '\"' );
  }

//  private static void replace( final Elements el, final char src, final
//  char dst ) {
//    for( final var e : el ) {
//      for( final var node : e.textNodes() ) {
//        node.text( node.text().replace( src, dst ) );
//      }
//    }
//  }
}
