/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.processors.markdown;

import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.AttributeImpl;
import com.vladsch.flexmark.util.html.MutableAttributes;
import org.jetbrains.annotations.NotNull;

import static com.keenwrite.Constants.CARET_ID;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;

/**
 * Responsible for giving most block-level elements a unique identifier
 * attribute. The identifier is used to coordinate scrolling.
 */
public class CaretExtension implements HtmlRendererExtension {

  /**
   * Responsible for creating the id attribute. This class is instantiated
   * each time the document is rendered, thereby resetting the count to zero.
   */
  public static class IdAttributeProvider implements AttributeProvider {
    private final CaretPosition mCaret;

    public IdAttributeProvider( final CaretPosition caret ) {
      mCaret = caret;
    }

    private static AttributeProviderFactory createFactory(
        final CaretPosition caret ) {
      return new IndependentAttributeProviderFactory() {
        @Override
        public @NotNull AttributeProvider apply(
            @NotNull final LinkResolverContext context ) {
          return new IdAttributeProvider( caret );
        }
      };
    }

    @Override
    public void setAttributes( @NotNull Node curr,
                               @NotNull AttributablePart part,
                               @NotNull MutableAttributes attributes ) {
      final var began = curr.getStartOffset();
      final var ended = curr.getEndOffset();
      final var prev = curr.getPrevious();

      // If the caret is within the bounds of the current node or the
      // caret is within the bounds of the end of the previous node and
      // the start of the current node, then mark the current node with
      // a caret indicator.
      if( mCaret.isBetweenText( began, ended ) ||
          prev != null && mCaret.isBetweenText( prev.getEndOffset(), began ) ) {
        attributes.addValue( AttributeImpl.of( "id", CARET_ID ) );
      }
    }
  }

  private final CaretPosition mCaret;

  private CaretExtension( final CaretPosition caret ) {
    mCaret = caret;
  }

  @Override
  public void extend(
      final Builder builder, @NotNull final String rendererType ) {
    builder.attributeProviderFactory(
        IdAttributeProvider.createFactory( mCaret ) );
  }

  public static CaretExtension create( final CaretPosition caret ) {
    return new CaretExtension( caret );
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }
}
