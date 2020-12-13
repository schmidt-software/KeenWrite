/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import java.util.List;
import java.util.ListIterator;

/**
 * Responsible for iterating over a list either forwards or backwards. When
 * the iterator reaches the last element in the list, the next element will
 * be the first. When the iterator reaches the first element in the list,
 * the previous element will be the last.
 */
public class CyclicIterator {
  /**
   * Returns an iterator that cycles indefinitely through the given list.
   *
   * @param list The list to cycle through indefinitely.
   * @param <T>  The type of list to be cycled.
   * @return A list iterator that can travel forwards and backwards throughout
   * time.
   */
  public static <T> ListIterator<T> of( final List<T> list ) {
    return new ListIterator<>() {
      // Assign an invalid index so that the first calls to either previous
      // or next will return the zeroth or final element.
      private int mIndex = -1;

      /**
       * @return {@code true}, always.
       */
      @Override
      public boolean hasNext() {
        return true;
      }

      /**
       * @return {@code true}, always.
       */
      @Override
      public boolean hasPrevious() {
        return true;
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
        list.remove( mIndex );
      }

      @Override
      public void set( final T t ) {
        list.set( mIndex, t );
      }

      @Override
      public void add( final T t ) {
        list.add( mIndex, t );
      }

      /**
       * Returns the next item in the list, which will cycle to the first
       * item as necessary.
       *
       * @return The next item in the list, cycling to the start if needed.
       */
      @Override
      public T next() {
        return list.get( mIndex = computeIndex( +1 ) );
      }

      /**
       * Returns the previous item in the list, which will cycle to the last
       * item as necessary.
       *
       * @return The previous item in the list, cycling to the end if needed.
       */
      @Override
      public T previous() {
        return list.get( mIndex = computeIndex( -1 ) );
      }

      private int computeIndex( final int direction ) {
        final var i = mIndex + direction;
        final var result = i < 0 ? list.size() - 1 : (i % list.size());

        // Ensure the invariant holds.
        assert 0 <= result && result < list.size();

        return result;
      }
    };
  }
}
