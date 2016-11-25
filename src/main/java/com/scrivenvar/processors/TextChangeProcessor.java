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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Responsible for forwarding change events to the document process chain. This
 * class isolates knowledge of the change events from the other processors.
 *
 * @author White Magic Software, Ltd.
 */
public class TextChangeProcessor extends AbstractProcessor<String>
  implements ChangeListener<String> {

  /**
   * Constructs a new text processor that listens for changes to text and then
   * injects them into the processing chain.
   *
   * @param successor Usually the HTML Preview Processor.
   */
  public TextChangeProcessor( final Processor<String> successor ) {
    super( successor );
  }

  /**
   * Called when the text editor changes.
   *
   * @param observable Unused.
   * @param oldValue The value before being changed (unused).
   * @param newValue The value after being changed (passed to processChain).
   */
  @Override
  public void changed(
    final ObservableValue<? extends String> observable,
    final String oldValue,
    final String newValue ) {
    processChain( newValue );
  }

  /**
   * Performs no processing.
   *
   * @param t Returned value.
   *
   * @return t, without any processing.
   */
  @Override
  public String processLink( String t ) {
    return t;
  }
}
