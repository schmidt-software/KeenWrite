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
 * Responsible for processing documents from one known format to another.
 *
 * @author White Magic Software, Ltd.
 * @param <T> The type of processor to create.
 */
public interface Processor<T> {
  
  /**
   * Provided so that the chain can be invoked from any link using a given
   * value. This should be called automatically by a superclass so that
   * the links in the chain need only implement the processLink method.
   * 
   * @param t The value to pass along to each link in the chain.
   * @return The value after having been processed by each link.
   */
  public void processChain( T t );

  /**
   * Processes the given content providing a transformation from one document
   * format into another. For example, this could convert from XML to text using
   * an XSLT processor, or from markdown to HTML.
   *
   * @param t The type of object to process.
   *
   * @return The post-processed document, or null if processing should stop.
   */
  public T processLink( T t );

  /**
   * Adds a document processor to call after this processor finishes processing
   * the document given to the process method.
   *
   * @return The processor that should transform the document after this
   * instance has finished processing.
   */
  public Processor<T> next();
}
