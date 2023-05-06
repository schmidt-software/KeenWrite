/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.collections;

import java.util.*;

import static java.lang.Math.min;

/**
 * Responsible for maintaining a circular queue where newly added items will
 * overwrite existing items.
 * <p>
 * <strong>Warning:</strong> This class is not thread-safe.
 * </p>
 *
 * @param <E> The type of elements to store in this collection.
 */
@SuppressWarnings( "unchecked" )
public class CircularQueue<E>
  extends AbstractCollection<E> implements Queue<E> {

  /**
   * Simplifies the code by reusing an existing list implementation.
   * Initialized with {@code null} values at construction time.
   */
  private final Object[] mElements;

  /**
   * Maximum number of elements allowed in the collection before old elements
   * are overwritten. Set at construction time.
   */
  private final int mCapacity;

  /**
   * Insertion position when a new element is added. Starts at zero.
   */
  private int mProducer;

  /**
   * Retrieval position when the oldest element is removed. Starts at zero.
   */
  private int mConsumer;

  /**
   * The number of elements in the collection. This cannot delegate to the
   * {@link #mElements} list. Starts at zero.
   */
  private int mSize;

  /**
   * Creates a new circular queue that has a limited number of elements that
   * may be added before newly added elements will overwrite the oldest
   * elements that were added previously.
   * <p>
   * <strong>Warning:</strong> Client classes must take care not to exceed
   * memory limits imposed by the Java Virtual Machine.
   *
   * @param capacity Maximum number elements allowed in the list, must be
   *                 greater than one.
   */
  public CircularQueue( final int capacity ) {
    assert capacity > 1;

    mCapacity = capacity;
    mElements = new Object[ capacity ];
  }

  /**
   * Adds an element to the end of the collection. This overwrites the oldest
   * element in the collection when the queue is full. The number of elements,
   * reflected by the return value of {@link #size()} will not exceed the
   * capacity.
   *
   * @param element The item to insert into the collection, must not be
   *                {@code null}.
   * @return {@code true} Non-{@code null} items are always added.
   * @throws NullPointerException if the given element is {@code null}.
   *                              The iterator requires a consecutive
   *                              non-{@code null} range (no gaps).
   */
  @Override
  public boolean add( final E element ) {
    if( element == null ) {
      throw new NullPointerException();
    }

    mElements[ mProducer++ ] = element;
    mProducer %= mCapacity;
    mSize = min( mSize + 1, mCapacity );

    return true;
  }

  /**
   * Delegates to {@link #add(E)}.
   */
  @Override
  public boolean offer( final E element ) {
    return add( element );
  }

  /**
   * Removes the oldest element that was added to the collection.  The number
   * of elements reflected by the return value of {@link #size()} will not
   * drop below zero.
   *
   * @return The oldest element.
   * @throws NoSuchElementException The collection is empty.
   */
  @Override
  public E remove() {
    if( isEmpty() ) {
      throw new NoSuchElementException();
    }

    final E element = (E) mElements[ mConsumer ];

    mElements[ mConsumer++ ] = null;
    mConsumer %= mCapacity;
    mSize--;

    return element;
  }

  /**
   * Delegates to {@link #remove()}, but does not throw an exception.
   *
   * @return The oldest element.
   */
  @Override
  public E poll() {
    return isEmpty() ? null : remove();
  }

  /**
   * Returns the oldest element that was added to the collection.
   *
   * @return The oldest element.
   * @throws NoSuchElementException The collection is empty.
   */
  @Override
  public E element() {
    if( isEmpty() ) {
      throw new NoSuchElementException();
    }

    return (E) mElements[ mConsumer ];
  }

  /**
   * Delegates to {@link #element()}, but does not throw an exception.
   *
   * @return The oldest element.
   */
  @Override
  public E peek() {
    return isEmpty() ? null : element();
  }

  /**
   * Answers how many elements are currently in the collection.
   *
   * @return The number of elements that have been added to but not removed
   * from the collection.
   */
  @Override
  public int size() {
    return mSize;
  }

  /**
   * Returns a facility to visit each of the elements in the
   * {@link CircularQueue}. This will start iterating at the oldest element
   * and stop when there are no more elements.
   * <p>
   * The iterator is not thread-safe; concurrent modifications to the number
   * of elements in the {@link CircularQueue} will result in undefined
   * behaviour.
   *
   * @return A new {@link Iterator} instance capable of visiting each element.
   */
  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      private int mIndex = mConsumer;
      private boolean mFirst = true;

      @Override
      public boolean hasNext() {
        return (mFirst || mIndex != mConsumer) && mElements[ mIndex ] != null;
      }

      @Override
      public E next() {
        try {
          final var element = mElements[ mIndex++ ];
          mIndex %= mCapacity;
          mFirst = false;

          return (E) element;
        } catch( final IndexOutOfBoundsException ex ) {
          throw new NoSuchElementException( "No such element at: " + mIndex );
        }
      }
    };
  }

  @Override
  public String toString() {
    return Arrays.toString( mElements );
  }
}
