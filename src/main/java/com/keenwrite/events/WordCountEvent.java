/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

/**
 * Collates information about the word count changing.
 */
public class WordCountEvent implements AppEvent {
  /**
   * Number of words in the document.
   */
  private final int mCount;

  private WordCountEvent( final int count ) {
    mCount = count;
  }

  /**
   * Publishes an event that indicates the number of words in the document.
   *
   * @param count The approximate number of words in the document.
   */
  public static void fireWordCountEvent( final int count ) {
    new WordCountEvent( count ).fire();
  }

  public int getCount() {
    return mCount;
  }
}
