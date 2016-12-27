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

import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import static com.ximpleware.VTDGen.TOKEN_CHARACTER_DATA;
import com.ximpleware.VTDNav;
import java.text.ParseException;
import javafx.beans.value.ObservableValue;

/**
 * Inserts a caret position indicator into the document.
 *
 * @author White Magic Software, Ltd.
 */
public class XMLCaretInsertionProcessor extends CaretInsertionProcessor {

  private VTDGen parser;

  /**
   * Constructs a processor capable of inserting a caret marker into XML.
   *
   * @param processor The next processor in the chain.
   * @param position The caret's current position in the text, cannot be null.
   */
  public XMLCaretInsertionProcessor(
    final Processor<String> processor,
    final ObservableValue<Integer> position ) {
    super( processor, position );
  }

  /**
   * Inserts a caret at a valid position within the XML document.
   *
   * @param text The string into which caret position marker text is inserted.
   *
   * @return The text with a caret position marker included, or the original
   * text if no insertion point could be found.
   */
  @Override
  public String processLink( final String text ) {
    final int caret = getCaretPosition();
    int insertOffset = -1;

    if( text.length() > 0 ) {
      try {
        final VTDNav vn = getNavigator( text );
        final int tokens = vn.getTokenCount();

        int currTokenIndex = 0;
        int prevTokenIndex = currTokenIndex;
        int currOffset = 0;

        // To find the insertion spot even faster, the algorithm could
        // use a binary search or interpolation search algorithm. This
        // would reduce the worst-case iterations to O(log n) from O(n).
        while( currTokenIndex < tokens ) {
          if( vn.getTokenType( currTokenIndex ) == TOKEN_CHARACTER_DATA ) {
            final int prevOffset = currOffset;
            currOffset = vn.getTokenOffset( currTokenIndex );

            if( currOffset > caret ) {
              final int prevLength = vn.getTokenLength( prevTokenIndex );

              // If the caret falls within the limits of the previous token,
              // theninsert the caret position marker at the caret offset.
              if( isBetween( caret, prevOffset, prevOffset + prevLength ) ) {
                insertOffset = caret;
              } else {
                // The caret position is outside the previous token's text
                // boundaries, but not inside the current text token. The
                // caret should be positioned into the closer text token.
                // For now, the cursor is positioned at the start of the
                // current text token.
                insertOffset = currOffset;
              }

              break;
            }

            prevTokenIndex = currTokenIndex;
          }

          currTokenIndex++;
        }

      } catch( final Exception ex ) {
        throw new RuntimeException(
          new ParseException( ex.getMessage(), caret )
        );
      }
    }

    return inject( text, insertOffset );
  }

  /**
   * Parses the given XML document and returns a high-performance navigator
   * instance for scanning through the XML elements.
   *
   * @param xml The XML document to parse.
   *
   * @return A document navigator instance.
   */
  private VTDNav getNavigator( final String xml ) throws VTDException {
    final VTDGen vg = getParser();

    // TODO: Use the document's encoding...
    vg.setDoc( xml.getBytes() );
    vg.parse( true );
    return vg.getNav();
  }

  private synchronized VTDGen getParser() {
    if( this.parser == null ) {
      this.parser = createParser();
    }

    return this.parser;
  }

  /**
   * Creates a high-performance XML document parser.
   *
   * @return A new XML parser.
   */
  protected VTDGen createParser() {
    return new VTDGen();
  }
}
