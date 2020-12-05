/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;

/**
 * Responsible for substituting multi-codepoint glyphs with single codepoint
 * glyphs. The text is adorned with ligatures prior to rendering as HTML.
 * This requires a font that supports ligatures.
 * <p>
 * TODO: #81 -- I18N
 * </p>
 */
public class LigatureExtension implements HtmlRendererExtension {
  /**
   * Retain insertion order so that ligature substitution uses longer ligatures
   * ahead of shorter ligatures. The word "ruffian" should use the "ffi"
   * ligature, not the "ff" ligature.
   */
  private static final Map<String, String> LIGATURES = new LinkedHashMap<>();

  static {
    // Common
    LIGATURES.put( "ffi", "\uFB03" );
    LIGATURES.put( "ffl", "\uFB04" );
    LIGATURES.put( "ff", "\uFB00" );
    LIGATURES.put( "fi", "\uFB01" );
    LIGATURES.put( "fl", "\uFB02" );
    LIGATURES.put( "ft", "\uFB05" );

    // Discretionary

    // Antiquated
//    LIGATURES.put( "AE", "\u00C6" );
//    LIGATURES.put( "OE", "\u0152" );
//    LIGATURES.put( "ae", "\u00E6" );
//    LIGATURES.put( "oe", "\u0153" );
  }

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

    /**
     * This will pick the fastest string replacement algorithm based on the
     * text length. The insertion order of the {@link #LIGATURES} is
     * important to give precedence to longer ligatures.
     *
     * @param textNode The text node containing text to replace with ligatures.
     * @param context  Not used.
     * @param html     Where to write the text adorned with ligatures.
     */
    private void render(
        @NotNull final Text textNode,
        @NotNull final NodeRendererContext context,
        @NotNull final HtmlWriter html ) {
      final var text = mVisitor.collectAndGetText( textNode );
      html.text( replace( text, LIGATURES ) );
    }
  }

  private static class Factory implements NodeRendererFactory {
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
  public void extend( @NotNull final Builder builder,
                      @NotNull final String rendererType ) {
    if( "HTML".equalsIgnoreCase( rendererType ) ) {
      builder.nodeRendererFactory( new Factory() );
    }
  }

  public static LigatureExtension create() {
    return new LigatureExtension();
  }
}
