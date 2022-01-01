package com.keenwrite.util;

import com.keenwrite.collections.CircularQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link CircularQueue} class.
 */
public class CircularQueueTest {

  /**
   * Exercises the circularity aspect of the {@link CircularQueue}.
   * Confirms that the elements added can be subsequently overwritten.
   * This also checks that peek and remove functionality work as expected.
   */
  @Test
  public void test_Add_ExceedMaxCapacity_FirstElementOverwritten() {
    final var CAPACITY = 5;
    final var OVERWRITE = 17;
    final var ELEMENTS = CAPACITY + OVERWRITE;
    final var queue = createQueue( CAPACITY, ELEMENTS );

    assertEquals( CAPACITY, queue.size() );

    for( int i = 0; i < CAPACITY; i++ ) {
      final var expected =
        ELEMENTS - ((((OVERWRITE - CAPACITY - 1) - i) % CAPACITY) + 1);

      assertEquals( expected, queue.peek() );
      assertEquals( expected, queue.remove() );
    }
  }

  /**
   * Tests iterating over all elements in the {@link CircularQueue}.
   */
  @Test
  public void test_Iterate_FullQueue_AllElementsNavigated() {
    final var CAPACITY = 101;
    final var queue = createQueue( CAPACITY, CAPACITY );
    int actualCount = 0;

    for( final var ignored : queue ) {
      actualCount++;
    }

    assertEquals( CAPACITY, actualCount );
  }

  /**
   * Tests iterating over {@link CircularQueue} where some elements,
   * starting at an arbitrary offset, have been removed.
   */
  @Test
  public void test_Iterate_PartialQueue_AllElementsNavigated() {
    final var CAPACITY = 31;
    final var OVERWRITE = CAPACITY / 2;
    final var queue = createQueue( CAPACITY, CAPACITY + OVERWRITE );
    var actualCount = 0;

    for( int i = 0; i < OVERWRITE; i++ ) {
      queue.remove();
    }

    for( final var ignored : queue ) {
      actualCount++;
    }

    assertEquals( CAPACITY - OVERWRITE, actualCount );
  }

  /**
   * Creates a new, pre-populated {@link CircularQueue} instance.
   *
   * @param capacity The maximum number of elements before overwriting.
   * @param count    The number of elements to pre-populate the queue.
   * @return A new {@link CircularQueue} pre-populated with ascending,
   * consecutive values.
   */
  private static CircularQueue<Integer> createQueue(
    final int capacity, final int count ) {
    final var queue = new CircularQueue<Integer>( capacity );

    for( int i = 0; i < count; i++ ) {
      queue.add( i );
    }

    return queue;
  }
}
