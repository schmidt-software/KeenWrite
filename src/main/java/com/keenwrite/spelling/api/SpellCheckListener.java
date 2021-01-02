/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.api;

import java.util.function.BiConsumer;

/**
 * Represents an operation that accepts two input arguments and returns no
 * result. Unlike most other functional interfaces, this class is expected to
 * operate via side-effects.
 * <p>
 * This is used instead of a {@link BiConsumer} to avoid autoboxing.
 * </p>
 */
@FunctionalInterface
public interface SpellCheckListener {

  /**
   * Performs an operation on the given arguments.
   *
   * @param text        The text associated with a beginning and ending offset.
   * @param beganOffset A starting offset, used as an index into a string.
   * @param endedOffset An ending offset, which should equal text.length() +
   *                    beganOffset.
   */
  void accept( String text, int beganOffset, int endedOffset );
}
