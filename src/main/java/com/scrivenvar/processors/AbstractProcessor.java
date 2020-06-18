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

/**
 * Responsible for transforming a document through a variety of chained
 * handlers. If there are conditions where this handler should not process the
 * entire chain, create a second handler, or split the chain into reusable
 * sub-chains.
 *
 * @author White Magic Software, Ltd.
 * @param <T> The type of object to process.
 */
public abstract class AbstractProcessor<T> implements Processor<T> {

  /**
   * Used while processing the entire chain; null to signify no more links.
   */
  private final Processor<T> mNext;

  /**
   * Constructs a succession without a successor (i.e., next is null).
   */
  protected AbstractProcessor() {
    this( null );
  }

  /**
   * Constructs a new default handler with a given successor.
   *
   * @param successor Use null to indicate last link in the chain.
   */
  public AbstractProcessor( final Processor<T> successor ) {
    mNext = successor;
  }

  /**
   * Processes links in the chain while there are successors and valid data to
   * process.
   *
   * @param t The object to process.
   */
  @Override
  public synchronized void processChain( T t ) {
    Processor<T> handler = this;

    while( handler != null && t != null ) {
      t = handler.processLink( t );
      handler = handler.next();
    }
  }

  @Override
  public Processor<T> next() {
    return mNext;
  }
}
