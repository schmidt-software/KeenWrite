/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions;

import com.keenwrite.Caret;
import com.keenwrite.constants.Constants;
import com.keenwrite.processors.ProcessorContext;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.html.AttributeImpl;
import com.vladsch.flexmark.util.html.MutableAttributes;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.keenwrite.constants.Constants.CARET_ID;
import static com.keenwrite.processors.markdown.extensions.EmptyNode.EMPTY_NODE;
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
  public void extend( @NotNull final Builder builder,
                      @NotNull final String rendererType ) {
    builder.attributeProviderFactory(
      IdAttributeProvider.createFactory( mCaret ) );
  }

  /**
   * Responsible for creating the id attribute. This class is instantiated
   * once: for the HTML element containing the {@link Constants#CARET_ID}.
   */
  public static class IdAttributeProvider implements AttributeProvider {
    private final Supplier<Caret> mCaret;
    private boolean mAdded;

    public IdAttributeProvider( final Supplier<Caret> caret ) {
      mCaret = caret;
    }

    private static AttributeProviderFactory createFactory(
      final Supplier<Caret> caret ) {
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
      // Optimization: if a caret is inserted, don't try to find another.
      if( mAdded ) {
        return;
      }

      final var caret = mCaret.get();

      // If a table block has been earmarked with an empty node, it means
      // another extension has generated code from an external source. The
      // Markdown processor won't be able to determine the caret position
      // with any semblance of accuracy, so skip the element. This usually
      // happens with tables, but in theory any Markdown generated from an
      // external source (e.g., an R script) could produce text that has no
      // caret position that can be calculated.
      var table = curr;

      if( !(curr instanceof TableBlock) ) {
        table = curr.getAncestorOfType( TableBlock.class );
      }

      // The table was generated outside the document
      if( table != null && table.getLastChild() == EMPTY_NODE ) {
        return;
      }

      final var outside = caret.isAfterText() ? 1 : 0;
      final var began = curr.getStartOffset();
      final var ended = curr.getEndOffset() + outside;
      final var prev = curr.getPrevious();

      // If the caret is within the bounds of the current node or the
      // caret is within the bounds of the end of the previous node and
      // the start of the current node, then mark the current node with
      // a caret indicator.
      if( caret.isBetweenText( began, ended ) ||
        prev != null && caret.isBetweenText( prev.getEndOffset(), began ) ) {
        // This line empowers synchronizing the text editor with the preview.
        attributes.addValue( AttributeImpl.of( "id", CARET_ID ) );

        // We're done until the user moves the caret (micro-optimization)
        mAdded = true;
      }
    }
  }
}
