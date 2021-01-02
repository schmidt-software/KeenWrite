/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Responsible for iterating over a list either forwards or backwards. When
 * the iterator reaches the last element in the list, the next element will
 * be the first. When the iterator reaches the first element in the list,
 * the previous element will be the last.
 * <p>
 * Due to the ability to move forwards and backwards through the list, rather
 * than force client classes to track the list index independently, this
 * iterator provides an accessor to the index. The index is zero-based.
 * </p>
 *
 * @param <T> The type of list to be cycled.
 */
public class CyclicIterator<T> implements ListIterator<T> {
  private final List<T> mList;

  /**
   * Initialize to an invalid index so that the first calls to either
   * {@link #previous()} or {@link #next()} will return the starting or ending
   * element.
   */
  private int mIndex = -1;

  /**
   * Creates an iterator that cycles indefinitely through the given list.
   *
   * @param list The list to cycle through indefinitely.
   */
  public CyclicIterator( final List<T> list ) {
    mList = list;
  }

  /**
   * @return {@code true} if there is at least one element.
   */
  @Override
  public boolean hasNext() {
    return !mList.isEmpty();
  }

  /**
   * @return {@code true} if there is at least one element.
   */
  @Override
  public boolean hasPrevious() {
    return !mList.isEmpty();
  }

  @Override
  public int nextIndex() {
    return computeIndex( +1 );
  }

  @Override
  public int previousIndex() {
    return computeIndex( -1 );
  }

  @Override
  public void remove() {
    mList.remove( mIndex );
  }

  @Override
  public void set( final T t ) {
    mList.set( mIndex, t );
  }

  @Override
  public void add( final T t ) {
    mList.add( mIndex, t );
  }

  /**
   * Returns the next item in the list, which will cycle to the first
   * item as necessary.
   *
   * @return The next item in the list, cycling to the start if needed.
   */
  @Override
  public T next() {
    return cycle( +1 );
  }

  /**
   * Returns the previous item in the list, which will cycle to the last
   * item as necessary.
   *
   * @return The previous item in the list, cycling to the end if needed.
   */
  @Override
  public T previous() {
    return cycle( -1 );
  }

  /**
   * Cycles to the next or previous element, depending on the direction value.
   *
   * @param direction Use -1 for previous, +1 for next.
   * @return The next or previous item in the list.
   */
  private T cycle( final int direction ) {
    try {
      return mList.get( mIndex = computeIndex( direction ) );
    } catch( final Exception ex ) {
      throw new NoSuchElementException( ex );
    }
  }

  /**
   * Returns the index of the value retrieved from the most recent call to
   * either {@link #previous()} or {@link #next()}.
   *
   * @return The list item index or -1 if no calls have been made to retrieve
   * an item from the list.
   */
  public int getIndex() {
    return mIndex;
  }

  private int computeIndex( final int direction ) {
    final var i = mIndex + direction;
    final var size = mList.size();
    final var result = i < 0
        ? size - 1
        : size == 0 ? 0 : i % size;

    // Ensure the invariant holds.
    assert 0 <= result && result < size || size == 0 && result <= 0;

    return result;
  }
}
