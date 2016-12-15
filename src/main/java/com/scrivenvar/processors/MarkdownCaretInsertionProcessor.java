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

import static com.scrivenvar.Constants.CARET_POSITION_MD;
import static java.lang.Character.isLetter;
import static java.lang.Math.min;

/**
 * Responsible for inserting the magic CARET POSITION into the markdown so that,
 * upon rendering into HTML, the HTML pane can scroll to the correct position
 * (relative to the caret position in the editor).
 *
 * @author White Magic Software, Ltd.
 */
public class MarkdownCaretInsertionProcessor extends AbstractProcessor<String> {

  private final int caretPosition;

  /**
   * Constructs a processor capable of inserting a caret marker into Markdown.
   *
   * @param processor The next processor in the chain.
   * @param position The caret's current position in the text, cannot be null.
   */
  public MarkdownCaretInsertionProcessor(
    final Processor<String> processor, final int position ) {
    super( processor );
    this.caretPosition = position;
  }

  /**
   * Changes the text to insert a "caret" at the caret position. This will
   * insert the unique key of Constants.MD_CARET_POSITION into the document.
   *
   * @param t The document text to process.
   *
   * @return The document text with the Markdown caret text inserted at the
   * caret position (given at construction time).
   */
  @Override
  public String processLink( final String t ) {
    final int length = t.length();
    int offset = min( getCaretPosition(), length );

    // TODO: Ensure that the caret position is outside of an element, 
    // so that a caret inserted in the image doesn't corrupt it. Such as:
    //
    // ![Screenshot](images/scr|eenshot.png)
    //
    // 1. Scan back to the previous EOL, which will be the MD AST start point.
    // 2. Scan forward until EOF or EOL, which will be the MD AST ending point.
    // 3. Convert the text between start and end into MD AST.
    // 4. Find the nearest text node to the caret.
    // 5. Insert the CARET_POSITION_MD value in the text at that offsset.

    // Insert the caret at the closest non-Markdown delimiter (i.e., the 
    // closest character from the caret position forward).
    while( offset < length && !isLetter( t.charAt( offset ) ) ) {
      offset++;
    }

    // Insert the caret position into the Markdown text, but don't interfere
    // with the Markdown iteself.
    return new StringBuilder( t ).replace(
      offset, offset, CARET_POSITION_MD ).toString();
  }

  /**
   * Returns the editor's caret position.
   *
   * @return Where the user has positioned the caret.
   */
  private int getCaretPosition() {
    return this.caretPosition;
  }
}
