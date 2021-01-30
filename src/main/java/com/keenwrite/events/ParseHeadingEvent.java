/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.events;

import com.keenwrite.processors.Processor;

/**
 * Collates information about a document heading that has been parsed, after
 * all pertinent {@link Processor}s applied.
 */
public class ParseHeadingEvent implements AppEvent {
  private static final int NEW_OUTLINE_LEVEL = -1;

  /**
   * The heading text, which may be {@code null} upon creating a new outline.
   */
  private final String mText;

  /**
   * The heading level, which will be set to {@link #NEW_OUTLINE_LEVEL} if this
   * event indicates that the existing outline should be cleared anew.
   */
  private final int mLevel;

  private ParseHeadingEvent( final String text, final int level ) {
    mText = text;
    mLevel = level;
  }

  /**
   * Call to indicate a new outline is to be created.
   */
  public static void fireNewOutlineEvent() {
    new ParseHeadingEvent( "", NEW_OUTLINE_LEVEL ).fire();
  }

  /**
   * Call to indicate that a new heading must be added to the document outline.
   *
   * @param text  The heading text (parsed and processed).
   * @param level A value between 1 and 6.
   */
  public static void fireNewHeadingEvent( final String text, final int level ) {
    assert text != null;
    assert 1 <= level && level <= 6;
    new ParseHeadingEvent( text, level ).fire();
  }

  public boolean isNewOutline() {
    return getLevel() == NEW_OUTLINE_LEVEL;
  }

  public boolean isSibling( final ParseHeadingEvent event ) {
    return event.getLevel() == getLevel();
  }

  public boolean isChild( final ParseHeadingEvent event ) {
    return event.getLevel() > getLevel();
  }

  public boolean isParent( final ParseHeadingEvent event ) {
    return event.getLevel() < getLevel();
  }

  private int getLevel() {
    return mLevel;
  }

  /**
   * Returns the text description for the heading.
   *
   * @return The post-parsed and processed heading text from the document.
   */
  @Override
  public String toString() {
    return mText;
  }
}
