package com.keenwrite.processors;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Responsible for transforming data through a variety of chained handlers.
 *
 * @param <T> The data type to process.
 */
public class ExecutorProcessor<T> implements Processor<T> {

  /**
   * The next link in the processing chain.
   */
  private final Processor<T> mNext;

  protected ExecutorProcessor() {
    this( null );
  }

  /**
   * Constructs a new processor having a given successor.
   *
   * @param successor The next processor in the chain.
   */
  public ExecutorProcessor( final Processor<T> successor ) {
    mNext = successor;
  }

  /**
   * Calls every link in the chain to process the given data.
   *
   * @param data The data to transform.
   * @return The data after processing by every link in the chain.
   */
  @Override
  public T apply( final T data ) {
    // Avoid infinite recursion.
    Optional<Processor<T>> handler = next();
    final var result = new AtomicReference<>( data );

    while( handler.isPresent() ) {
      handler = handler.flatMap( p -> {
        result.set( p.apply( result.get() ) );
        return p.next();
      } );
    }

    return result.get();
  }

  @Override
  public Optional<Processor<T>> next() {
    return Optional.ofNullable( mNext );
  }
}
