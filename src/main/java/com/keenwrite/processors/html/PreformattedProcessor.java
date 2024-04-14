/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.html;

import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.Processor;

/**
 * This is the default processor used when an unknown file name extension is
 * encountered. It processes the text by enclosing it in an HTML {@code <pre>}
 * element.
 */
public final class PreformattedProcessor extends ExecutorProcessor<String> {

  /**
   * Passes the link to the super constructor.
   *
   * @param successor The next processor in the chain to use for text
   *                  processing.
   */
  public PreformattedProcessor( final Processor<String> successor ) {
    super( successor );
  }

  /**
   * Returns the given string, modified with "pre" tags.
   *
   * @param t The string to return, enclosed in "pre" tags.
   * @return The value of t wrapped in "pre" tags.
   */
  @Override
  public String apply( final String t ) {
    return STR."<pre>\{t}</pre>";
  }
}
