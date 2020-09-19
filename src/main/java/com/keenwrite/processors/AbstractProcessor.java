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

/**
 * Responsible for transforming a document through a variety of chained
 * handlers. If there are conditions where this handler should not process the
 * entire chain, create a second handler, or split the chain into reusable
 * sub-chains.
 *
 * @param <T> The type of object to process.
 */
public abstract class AbstractProcessor<T> implements Processor<T> {

  /**
   * Used while processing the entire chain; null to signify no more links.
   */
  private final Processor<T> mNext;

  /**
   * Constructs a new default handler with no successor.
   */
  protected AbstractProcessor() {
    this( null );
  }

  /**
   * Constructs a new default handler with a given successor.
   *
   * @param successor The next processor in the chain.
   */
  public AbstractProcessor( final Processor<T> successor ) {
    mNext = successor;
  }

  @Override
  public Processor<T> next() {
    return mNext;
  }

  /**
   * This algorithm is incorrect, but works for the one use case of removing
   * the ending HTML Preview Processor from the end of the processor chain.
   * The processor chain is immutable so this creates a succession of
   * delegators that wrap each processor in the chain, except for the one
   * to be removed.
   * <p>
   * An alternative is to update the {@link ProcessorFactory} with the ability
   * to create a processor chain devoid of an {@link HtmlPreviewProcessor}.
   * </p>
   *
   * @param removal The {@link Processor} to remove from the chain.
   * @return A delegating processor chain starting from this processor
   * onwards with the given processor removed from the chain.
   */
  @Override
  public Processor<T> remove( final Class<? extends Processor<T>> removal ) {
    Processor<T> p = this;
    final ProcessorDelegator<T> head = new ProcessorDelegator<>( p );
    ProcessorDelegator<T> result = head;

    while( p != null ) {
      final Processor<T> next = p.next();

      if( next != null && next.getClass() != removal ) {
        final var delegator = new ProcessorDelegator<>( next );

        result.setNext( delegator );
        result = delegator;
      }

      p = p.next();
    }

    return head;
  }

  private static final class ProcessorDelegator<T>
      extends AbstractProcessor<T> {
    private final Processor<T> mDelegate;
    private Processor<T> mNext;

    public ProcessorDelegator( final Processor<T> delegate ) {
      super( delegate );

      assert delegate != null;

      mDelegate = delegate;
    }

    @Override
    public T apply( T t ) {
      return mDelegate.apply( t );
    }

    protected void setNext( final Processor<T> next ) {
      mNext = next;
    }

    @Override
    public Processor<T> next() {
      return mNext;
    }
  }
}
