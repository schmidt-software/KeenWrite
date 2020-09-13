package com.scrivenvar.processors.markdown;

import com.vladsch.flexmark.ast.BlockQuote;
import com.vladsch.flexmark.ast.ListBlock;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.html.MutableAttributes;
import org.jetbrains.annotations.NotNull;

import static com.scrivenvar.Constants.PARAGRAPH_ID_PREFIX;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import static com.vladsch.flexmark.html.renderer.CoreNodeRenderer.CODE_CONTENT;

/**
 * Responsible for giving most block-level elements a unique identifier
 * attribute. The identifier is used to coordinate scrolling.
 */
public class BlockExtension implements HtmlRendererExtension {
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
      // Blockquotes are troublesome because they can interleave blank lines
      // without having an equivalent blank line in the source document. That
      // is, in Markdown the > symbol on a line by itself will generate a blank
      // line in the resulting document; however, a > symbol in the text editor
      // does not count as a blank line. Resolving this issue is tricky.
      //
      // The CODE_CONTENT represents <code> embedded inside <pre>; both elements
      // enter this method as FencedCodeBlock, but only the <pre> must be
      // uniquely identified (because they are the same line in Markdown).
      //
      if( node instanceof Block &&
          !(node instanceof BlockQuote) &&
          !(node instanceof ListBlock) &&
          (part != CODE_CONTENT) ) {
        attributes.addValue( "id", PARAGRAPH_ID_PREFIX + mCount++ );
      }
    }
  }

  private BlockExtension() {
  }

  @Override
  public void extend( final Builder builder,
                      @NotNull final String rendererType ) {
    builder.attributeProviderFactory( IdAttributeProvider.createFactory() );
  }

  public static BlockExtension create() {
    return new BlockExtension();
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }
}
