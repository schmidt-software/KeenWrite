/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.common;

import com.vladsch.flexmark.parser.Parser.ParserExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;

/**
 * Provides a default {@link #parserOptions(MutableDataHolder)} implementation.
 */
public interface MarkdownParserExtension extends ParserExtension {
  @Override
  default void parserOptions( final MutableDataHolder options ) {}
}
