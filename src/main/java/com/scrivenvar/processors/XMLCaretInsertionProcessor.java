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
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import static com.ximpleware.VTDGen.TOKEN_CHARACTER_DATA;
import com.ximpleware.VTDNav;
import java.text.ParseException;

/**
 * Inserts a caret position indicator into the document.
 *
 * @author White Magic Software, Ltd.
 */
public class XMLCaretInsertionProcessor extends CaretInsertionProcessor {

  private FileEditorTab tab;

  /**
   * Constructs a processor capable of inserting a caret marker into XML.
   *
   * @param processor The next processor in the chain.
   * @param position The caret's current position in the text, cannot be null.
   */
  public XMLCaretInsertionProcessor(
    final Processor<String> processor, final int position ) {
    super( processor, position );
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
    final int caretOffset = getCaretPosition();
    int insertOffset = -1;

    if( t.length() > 0 ) {

      try {
        final VTDNav vn = getNavigator( t );

        final int tokens = vn.getTokenCount();

        int currTokenIndex = 0;
        int prevTokenIndex = currTokenIndex;
        int currTokenOffset = 0;

        boolean found = false;

        // To find the insertion spot even faster, the algorithm could
        // use a binary search or interpolation search algorithm. This
        // would reduce the worst-case iterations to O(log n) from O(n).
        while( currTokenIndex < tokens && !found ) {
          if( vn.getTokenType( currTokenIndex ) == TOKEN_CHARACTER_DATA ) {
            final int prevTokenOffset = currTokenOffset;
            currTokenOffset = vn.getTokenOffset( currTokenIndex );

            if( currTokenOffset > caretOffset ) {
              found = true;

              final int prevTokenLength = vn.getTokenLength( prevTokenIndex );

              // If the caret falls within the limits of the previous token, then
              // insert the caret position marker at the caret offset.
              if( isBetween( caretOffset, prevTokenOffset, prevTokenOffset + prevTokenLength ) ) {
                insertOffset = caretOffset;
              } else {
                // The caret position is outside the previous token's text
                // boundaries, but not inside the current text token. The
                // caret should be positioned into the closer text token.
                // For now, the cursor is positioned at the start of the
                // current text token.
                insertOffset = currTokenOffset;
              }

              // Done.
              continue;
            }

            prevTokenIndex = currTokenIndex;
          }

          currTokenIndex++;
        }

      } catch( final Exception ex ) {
        throw new RuntimeException(
          new ParseException( ex.getMessage(), caretOffset )
        );
      }
    }

    return inject( t, insertOffset );
  }

  private boolean isBetween( int i, int min, int max ) {
    return i >= min && i <= max;
  }

  /**
   * Parses the given XML document and returns a high-performance navigator
   * instance for scanning through the XML elements.
   *
   * @param xml
   *
   * @return
   */
  private VTDNav getNavigator( final String xml ) throws VTDException {
    final VTDGen vg = getParser();

    vg.setDoc( xml.getBytes() );
    vg.parse( true );
    return vg.getNav();
  }

  private VTDGen getParser() {
    return new VTDGen();
  }
}
