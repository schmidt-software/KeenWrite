/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.caret;

import com.keenwrite.editors.common.Caret;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.common.HtmlRendererAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.keenwrite.processors.markdown.extensions.caret.IdAttributeProvider.createFactory;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;

/**
 * Responsible for giving most block-level elements a unique identifier
 * attribute. The identifier is used to coordinate scrolling.
 */
public class CaretExtension extends HtmlRendererAdapter {

  private final Supplier<Caret> mCaret;

  private CaretExtension( final ProcessorContext context ) {
    mCaret = context.getCaret();
  }

  public static CaretExtension create( final ProcessorContext context ) {
    return new CaretExtension( context );
  }

  @Override
  public void extend(
    @NotNull final Builder builder,
    @NotNull final String rendererType ) {
    builder.attributeProviderFactory( createFactory( mCaret ) );
  }
}
