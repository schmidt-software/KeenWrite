/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link CyclicIterator} class.
 */
public class CyclicIteratorTest {
  /**
   * Test that the {@link CyclicIterator} can move forwards and backwards
   * through a {@link List}.
   */
  @Test
  public void test_Directions_NextPreviousCycles_Success() {
    final var list = List.of( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 );
    final var iterator = createCyclicIterator( list );

    // Test forwards through the iterator.
    for( int i = 0; i < list.size(); i++ ) {
      assertTrue( iterator.hasNext() );
      assertEquals( i, iterator.next() );
    }

    // Loop to the first item.
    iterator.next();

    // Test backwards through the iterator.
    for( int i = list.size() - 1; i >= 0; i-- ) {
      assertTrue( iterator.hasPrevious() );
      assertEquals( i, iterator.previous() );
    }
  }

  /**
   * Test that the {@link CyclicIterator} returns the last element when
   * the very first API call is to {@link ListIterator#previous()}.
   */
  @Test
  public void test_Direction_FirstPrevious_ReturnsLastElement() {
    final var list = List.of( 1, 2, 3, 4, 5, 6, 7 );
    final var iterator = createCyclicIterator( list );

    assertEquals( iterator.previous(), list.get( list.size() - 1 ) );
  }

  @Test
  public void test_Empty_Next_Exception() {
    final var iterator = createCyclicIterator( List.of() );
    assertThrows( NoSuchElementException.class, iterator::next );
  }

  @Test
  public void test_Empty_Previous_Exception() {
    final var iterator = createCyclicIterator( List.of() );
    assertThrows( NoSuchElementException.class, iterator::previous );
  }

  private <T> CyclicIterator<T> createCyclicIterator( final List<T> list ) {
    return new CyclicIterator<>( list );
  }
}
