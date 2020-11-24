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
    // Start processing using the first processor after the executor.
    Optional<Processor<T>> handler = next();
    final var result = new MutableReference( data );

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

  /**
   * A minor micro-optimization, since the processors cannot be run in parallel,
   * avoid using an {@link AtomicReference} during processor execution. This
   * is about twice as fast for the first four processor links in the chain.
   */
  private final class MutableReference {
    private T mObject;

    MutableReference( final T object ) {
      set( object );
    }

    void set( final T object ) {
      mObject = object;
    }

    T get() {
      return mObject;
    }
  }
}
