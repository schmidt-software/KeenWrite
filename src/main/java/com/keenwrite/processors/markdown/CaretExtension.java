/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.Constants;
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
   * once: for the HTML element containing the {@link Constants#CARET_ID}.
   */
  public static class IdAttributeProvider implements AttributeProvider {
    private final Caret mCaret;

    public IdAttributeProvider( final Caret caret ) {
      mCaret = caret;
    }

    private static AttributeProviderFactory createFactory(
        final Caret caret ) {
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
      final var outside = mCaret.isAfterText() ? 1 : 0;
      final var began = curr.getStartOffset();
      final var ended = curr.getEndOffset() + outside;
      final var prev = curr.getPrevious();

      // If the caret is within the bounds of the current node or the
      // caret is within the bounds of the end of the previous node and
      // the start of the current node, then mark the current node with
      // a caret indicator.
      if( mCaret.isBetweenText( began, ended ) ||
          prev != null && mCaret.isBetweenText( prev.getEndOffset(), began ) ) {
        // This line empowers synchronizing the text editor with the preview.
        attributes.addValue( AttributeImpl.of( "id", CARET_ID ) );
      }
    }
  }

  private final Caret mCaret;

  private CaretExtension( final Caret caret ) {
    mCaret = caret;
  }

  @Override
  public void extend(
      final Builder builder, @NotNull final String rendererType ) {
    builder.attributeProviderFactory(
        IdAttributeProvider.createFactory( mCaret ) );
  }

  public static CaretExtension create( final Caret caret ) {
    return new CaretExtension( caret );
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }
}
