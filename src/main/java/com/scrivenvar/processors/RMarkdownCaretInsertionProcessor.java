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

import static com.scrivenvar.decorators.RVariableDecorator.PREFIX;
import static com.scrivenvar.decorators.RVariableDecorator.SUFFIX;
import static java.lang.Integer.max;
import javafx.beans.value.ObservableValue;

/**
 * Responsible for inserting a caret position token into an R document.
 *
 * @author White Magic Software, Ltd.
 */
public class RMarkdownCaretInsertionProcessor
  extends MarkdownCaretInsertionProcessor {

  /**
   * Constructs a processor capable of inserting a caret marker into Markdown.
   *
   * @param processor The next processor in the chain.
   * @param position The caret's current position in the text.
   */
  public RMarkdownCaretInsertionProcessor(
    final Processor<String> processor,
    final ObservableValue<Integer> position ) {
    super( processor, position );
  }

  /**
   * Changes the text to insert a "caret" at the caret position. This will
   * insert the unique key of Constants.MD_CARET_POSITION into the document.
   *
   * @param text The text document to process.
   *
   * @return The text with the caret position token inserted at the caret
   * position.
   */
  @Override
  public String processLink( final String text ) {
    int offset = getCaretPosition();

    // Search for inline R code from the start of the caret's paragraph.
    int index = text.lastIndexOf( NEWLINE, offset );

    if( index == INDEX_NOT_FOUND ) {
      index = 0;
    }

    // Scan for an inline R statement, either from the nearest paragraph or
    // the beginning of the file, whichever was found first.
    index = text.indexOf( PREFIX, index );

    // If there was no R prefix then insert at the caret's initial offset...
    if( index != INDEX_NOT_FOUND ) {
      // Otherwise, retain the starting index of the first R statement in the
      // paragraph.
      int rPrefix = index + 1;

      // Scan for inline R prefixes until the text is exhausted or indexed
      // beyond the caret position.
      while( index != INDEX_NOT_FOUND && index < offset ) {
        // Set rPrefix to the index that might precede the caret.
        rPrefix = index + 1;

        // If there are no more R prefixes, exit the loop and look for a
        // suffix starting from the rPrefix position.
        index = text.indexOf( PREFIX, rPrefix );
      }

      // Scan from the character after the R prefix up to any R suffix.
      final int rSuffix = max( text.indexOf( SUFFIX, rPrefix ), rPrefix );

      final boolean between = isBetween( offset, rPrefix, rSuffix );
      
      // Insert the caret marker at the start of the R statement.
      if( between ) {
        offset = rPrefix - 1;
      }
    }

    return inject( text, offset );
  }
}
