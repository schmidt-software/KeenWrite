/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.common;

import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class MarkdownCustomBlockParserFactory
  implements CustomBlockParserFactory {
  /**
   * Subclasses must return a new {@link BlockParserFactory} instance.
   *
   * @param options Passed into the new instance constructor.
   * @return The new {@link BlockParserFactory} instance.
   */
  protected abstract BlockParserFactory createBlockParserFactory(
    DataHolder options );

  @NotNull
  @Override
  public BlockParserFactory apply( @NotNull final DataHolder options ) {
    return createBlockParserFactory( options );
  }

  @Override
  public @Nullable Set<Class<?>> getAfterDependents() {
    return null;
  }

  @Override
  public @Nullable Set<Class<?>> getBeforeDependents() {
    return null;
  }

  @Override
  public boolean affectsGlobalScope() {
    return false;
  }
}
