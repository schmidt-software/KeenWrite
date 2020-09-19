/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.processors;

import java.util.function.UnaryOperator;

/**
 * Responsible for processing documents from one known format to another.
 * Processes the given content providing a transformation from one document
 * format into another. For example, this could convert from XML to text using
 * an XSLT processor, or from markdown to HTML.
 *
 * @param <T> The type of processor to create.
 */
public interface Processor<T> extends UnaryOperator<T> {

  /**
   * Removes the given processor from the chain, returning a new immutable
   * chain equivalent to this chain, but without the given processor.
   *
   * @param processor The {@link Processor} to remove from the chain.
   * @return A delegating processor chain starting from this processor
   * onwards with the given processor removed from the chain.
   */
  Processor<T> remove( Class<? extends Processor<T>> processor );

  /**
   * Adds a document processor to call after this processor finishes processing
   * the document given to the process method.
   *
   * @return The processor that should transform the document after this
   * instance has finished processing, or {@code null} if this is the last
   * processor in the chain.
   */
  default Processor<T> next() {
    return null;
  }
}
