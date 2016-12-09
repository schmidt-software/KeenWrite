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

import static com.scrivenvar.Constants.MD_CARET_POSITION;
import static java.lang.Character.isLetterOrDigit;
import org.fxmisc.richtext.model.TextEditingArea;

/**
 * Responsible for inserting the magic CARET POSITION into the markdown so
 * that, upon rendering into HTML, the HTML pane can scroll to the correct
 * position (relative to the caret position in the editor).
 *
 * @author White Magic Software, Ltd.
 */
public class MarkdownCaretInsertionProcessor extends AbstractProcessor<String> {

  private TextEditingArea editor;

  /**
   * Constructs a processor capable of inserting a caret marker into Markdown.
   *
   * @param processor The next processor in the chain.
   * @param editor The editor that has a caret with a position in the text.
   */
  public MarkdownCaretInsertionProcessor(
    final Processor<String> processor, final TextEditingArea editor ) {
    super( processor );
    setEditor( editor );
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
    int offset = getCaretPosition();
    final int length = t.length();

    // Insert the caret at the closest non-Markdown delimiter (i.e., the 
    // closest character from the caret position forward).
    while( offset < length && !isLetterOrDigit( t.charAt( offset ) ) ) {
      offset++;
    }

    // Insert the caret position into the Markdown text, but don't interfere
    // with the Markdown iteself.
    return new StringBuilder( t ).replace(
      offset, offset, MD_CARET_POSITION ).toString();
  }

  /**
   * Returns the editor's caret position.
   *
   * @return Where the user has positioned the caret.
   */
  private int getCaretPosition() {
    return getEditor().getCaretPosition();
  }

  /**
   * Returns the editor that has a caret position.
   *
   * @return An editor with a caret position.
   */
  private TextEditingArea getEditor() {
    return this.editor;
  }

  private void setEditor( final TextEditingArea editor ) {
    this.editor = editor;
  }
}
