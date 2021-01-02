/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Responsible for processing documents from one known format to another.
 * Processes the given content providing a transformation from one document
 * format into another. For example, this could convert Markdown to HTML.
 *
 * @param <T> The data type to process.
 */
public interface Processor<T> extends UnaryOperator<T> {

  /**
   * Returns the next link in the processor chain.
   *
   * @return The processor intended to transform the data after this instance
   * has finished processing, or {@link Optional#empty} if this is the last
   * link in the chain.
   */
  default Optional<Processor<T>> next() {
    return Optional.empty();
  }
}
