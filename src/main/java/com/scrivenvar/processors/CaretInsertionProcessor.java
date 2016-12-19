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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;

/**
 * Base class for inserting the magic CARET POSITION into the text so that, upon
 * previewing, the preview pane can scroll to the correct position (relative to
 * the caret position in the editor).
 *
 * @author White Magic Software, Ltd.
 */
public abstract class CaretInsertionProcessor extends AbstractProcessor<String> {

  private final IntegerProperty caretPosition = new SimpleIntegerProperty();

  public CaretInsertionProcessor(
    final Processor<String> processor,
    final ObservableValue<Integer> position ) {
    super( processor );
    this.caretPosition.bind( position );
  }

  /**
   * Inserts the caret position token into the text at an offset that won't
   * interfere with parsing the text itself, regardless of text format.
   *
   * @param text The text document to change.
   * @param i The caret position token insertion point to use, or -1 to
   * return the text without any injection.
   *
   * @return The given text with a caret position token inserted at the given
   * offset.
   */
  protected String inject( final String text, final int i ) {
    return i > 0 && i <= text.length()
      ? new StringBuilder( text ).replace( i, i, CARET_POSITION_MD ).toString()
      : text;
  }

  /**
   * Returns the editor's caret position.
   *
   * @return Where the user has positioned the caret.
   */
  protected int getCaretPosition() {
    return this.caretPosition.getValue();
  }
}
