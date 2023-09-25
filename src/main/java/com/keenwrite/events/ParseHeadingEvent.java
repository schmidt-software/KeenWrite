/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.events;

import com.keenwrite.processors.Processor;

/**
 * Collates information about a document heading that has been parsed, after
 * all pertinent {@link Processor}s applied.
 */
public class ParseHeadingEvent implements AppEvent {
  private static final int NEW_OUTLINE_LEVEL = 0;

  /**
   * The heading text, which may be {@code null} upon creating a new outline.
   */
  private final String mText;

  /**
   * The heading level, which will be set to {@link #NEW_OUTLINE_LEVEL} if this
   * event indicates that the existing outline should be cleared anew.
   */
  private final int mLevel;

  /**
   * Offset into the text where the heading is found.
   */
  private final int mOffset;

  private ParseHeadingEvent(
    final int level, final String text, final int offset ) {
    mText = text;
    mLevel = level;
    mOffset = offset;
  }

  /**
   * Call to indicate a new outline is to be created.
   */
  public static void fireNewOutlineEvent() {
    new ParseHeadingEvent( NEW_OUTLINE_LEVEL, "Document", 0 ).publish();
  }

  /**
   * Call to indicate that a new heading must be added to the document outline.
   *
   * @param text   The heading text (parsed and processed).
   * @param level  A value between 1 and 6.
   * @param offset Absolute offset into document where heading is found.
   */
  public static void fire(
    final int level, final String text, final int offset ) {
    assert text != null;
    assert 1 <= level && level <= 6;
    assert 0 <= offset;
    new ParseHeadingEvent( level, text, offset ).publish();
  }

  public boolean isNewOutline() {
    return getLevel() == NEW_OUTLINE_LEVEL;
  }

  public int getLevel() {
    return mLevel;
  }

  /**
   * Returns the text description for the heading.
   *
   * @return The post-parsed and processed heading text from the document.
   */
  public String getText() {
    return mText;
  }

  /**
   * Returns an offset into the document where the heading is found.
   *
   * @return A zero-based document offset.
   */
  public int getOffset() {
    return mOffset;
  }

  @Override
  public String toString() {
    return getText();
  }
}
