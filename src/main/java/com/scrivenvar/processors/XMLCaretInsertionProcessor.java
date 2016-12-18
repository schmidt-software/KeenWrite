/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.processors;

import com.scrivenvar.FileEditorTab;
import com.ximpleware.VTDGen;
import static com.ximpleware.VTDGen.TOKEN_CHARACTER_DATA;
import com.ximpleware.VTDNav;

/**
 * Inserts a caret position indicator into the document.
 *
 * @author White Magic Software, Ltd.
 */
public class XMLCaretInsertionProcessor extends AbstractProcessor<String> {

  private FileEditorTab tab;

  /**
   *
   * @param processor Next link in the processing chain.
   * @param tab
   */
  public XMLCaretInsertionProcessor( final Processor<String> processor, final FileEditorTab tab ) {
    super( processor );
    setFileEditorTab( tab );
  }

  /**
   * Inserts a caret at a valid position within the XML document.
   *
   * @param t The string into which caret position marker text is inserted.
   *
   * @return t with a caret position marker included, or t if no place to insert
   * could be found.
   */
  @Override
  public String processLink( final String t ) {
    int insertOffset = -1;

    if( t.length() > 0 ) {

      try {
        final VTDGen vg = new VTDGen();

        vg.setDoc( t.getBytes() );
        vg.parse( true );

        final VTDNav vn = vg.getNav();

        final int caretOffset = getCaretPosition();
        final int tokens = vn.getTokenCount();

        int currTextTokenIndex = 0;
        int prevTextTokenIndex = currTextTokenIndex;
        int currTokenOffset = 0;

        boolean found = false;

        // To find the insertion spot even faster, the algorithm could
        // use a binary search or interpolation search algorithm. This
        // would reduce the worst-case iterations to O(log n) from O(n).
        while( currTextTokenIndex < tokens && !found ) {
          final int prevTokenOffset = currTokenOffset;
          final int currTokenType = vn.getTokenType( currTextTokenIndex );

          if( currTokenType == TOKEN_CHARACTER_DATA ) {
            currTokenOffset = vn.getTokenOffset( currTextTokenIndex );

            if( currTokenOffset > caretOffset ) {
              found = true;

              final int prevTokenLength = vn.getTokenLength( prevTextTokenIndex );

              // If the caret falls within the limits of the previous token, then
              // insert the caret position marker at the caret offset.
              if( isBetween( caretOffset, prevTokenOffset, prevTokenOffset + prevTokenLength ) ) {
                insertOffset = caretOffset;
              } else {
                // The caret position is outside the previous token's text
                // boundaries, but the current text token is far away. The
                // cursor should be positioned into the closer text token.
                // For now, the cursor is positioned at the start of the
                // current text token.
                insertOffset = currTokenOffset;
              }

              continue;
            }

            prevTextTokenIndex = currTextTokenIndex;
          }

          currTextTokenIndex++;
        }

      } catch( final Exception ex ) {
        ex.printStackTrace();
      }
    }

    
    /*
    System.out.println( "-- CARET --------------------------------" );
    System.out.println( "offset: " + caretOffset );
    System.out.println( "-- BETWEEN PREV TOKEN --------------------" );
    System.out.println( "index  : " + prevTextTokenIndex );
    System.out.println( "type   : " + prevTokenType );
    System.out.println( "offset : " + prevTokenOffset );
    System.out.println( "length : " + prevTokenLength );
    System.out.println( "offset + length: " + (prevTokenOffset + prevTokenLength - 1) );
    System.out.println( "text   : '" + prevToken.trim() + "'" );
    System.out.println( "-- CURR TOKEN ---------------------------" );
    System.out.println( "index  : " + currTextTokenIndex );
    System.out.println( "type   : " + currTokenType );
    System.out.println( "offset : " + currTokenOffset );
    System.out.println( "length : " + currTokenLength );
    System.out.println( "between: " + currBetween );
    System.out.println( "text   : '" + currToken.trim() + "'" );
     */


    if( insertOffset > 0 ) {
      // Insert the caret at the given offset.
      // TODO: Create and use CaretInsertion superclass.
      System.out.println( "insert offset: " + insertOffset );
      System.out.println( "caret offset : " + getCaretPosition() );
    }

    return t;
  }
  
  

  private int getCaretPosition() {
    return getFileEditorTab().getCaretPosition();
  }

  private void setFileEditorTab( final FileEditorTab tab ) {
    this.tab = tab;
  }

  private FileEditorTab getFileEditorTab() {
    return this.tab;
  }

  private boolean isBetween( int i, int min, int max ) {
    return i >= min && i <= max;
  }
}
