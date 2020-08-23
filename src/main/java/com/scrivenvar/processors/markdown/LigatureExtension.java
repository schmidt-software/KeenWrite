package com.scrivenvar.processors.markdown;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.scrivenvar.processors.text.TextReplacementFactory.replace;

/**
 * Responsible for substituting multi-codepoint glyphs with single codepoint
 * glyphs. The text is adorned with ligatures prior to rendering as HTML.
 * This requires a font that supports ligatures.
 * <p>
 * TODO: I18N https://github.com/DaveJarvis/scrivenvar/issues/81
 * </p>
 */
public class LigatureExtension
    implements HtmlRenderer.HtmlRendererExtension {
  private final static Map<String, String> LIGATURES_SHORT = Map.of(
//      "ae", "\u00E6",
//      "oe", "\u0153",
      "AE", "\u00C6",
      "OE", "\u0152",
      "ff", "\uFB00",
      "fi", "\uFB01",
      "fl", "\uFB02",
      "ft", "\uFB05"
  );

  private final static Map<String, String> LIGATURES_LONG = Map.of(
      "ffi", "\uFB03",
      "ffl", "\uFB04"
  );

  private static class LigatureRenderer implements NodeRenderer {

    private final TextCollectingVisitor mVisitor = new TextCollectingVisitor();

    @SuppressWarnings("unused")
    public LigatureRenderer( final DataHolder options ) {
    }

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
      return Set.of( new NodeRenderingHandler<>(
          Text.class, LigatureRenderer.this::render ) );
    }

    private <N extends Node> void render(
        @NotNull final N n,
        @NotNull final NodeRendererContext nodeRendererContext,
        @NotNull final HtmlWriter html ) {
      final var text = mVisitor.collectAndGetText( n );
      html.text( replace( replace( text, LIGATURES_LONG ), LIGATURES_SHORT ) );
    }
  }

  public static class Factory implements NodeRendererFactory {
    @NotNull
    @Override
    public NodeRenderer apply( @NotNull DataHolder options ) {
      return new LigatureRenderer( options );
    }
  }

  private LigatureExtension() {
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }

  @Override
  public void extend( @NotNull final HtmlRenderer.Builder builder,
                      @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new Factory() );
    }
  }

  public static LigatureExtension create() {
    return new LigatureExtension();
  }
}
