package com.keenwrite.processors.markdown;

import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.MutableAttributes;
import org.jetbrains.annotations.NotNull;

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
    private int mCount;

    private static AttributeProviderFactory createFactory() {
      return new IndependentAttributeProviderFactory() {
        @Override
        public @NotNull AttributeProvider apply(
            @NotNull final LinkResolverContext context ) {
          return new IdAttributeProvider();
        }
      };
    }

    @Override
    public void setAttributes( @NotNull Node node,
                               @NotNull AttributablePart part,
                               @NotNull MutableAttributes attributes ) {
    }
  }

  private CaretExtension() {
  }

  @Override
  public void extend( final Builder builder,
                      @NotNull final String rendererType ) {
    builder.attributeProviderFactory( IdAttributeProvider.createFactory() );
  }

  public static CaretExtension create() {
    return new CaretExtension();
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }
}
