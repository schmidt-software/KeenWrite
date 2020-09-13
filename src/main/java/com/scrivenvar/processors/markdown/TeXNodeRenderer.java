package com.scrivenvar.processors.markdown;

import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class TeXNodeRenderer implements NodeRenderer {

  public static class Factory implements NodeRendererFactory {
    @NotNull
    @Override
    public NodeRenderer apply( @NotNull DataHolder options ) {
      return new TeXNodeRenderer();
    }
  }

  @Override
  public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
    final Set<NodeRenderingHandler<?>> set = new HashSet<>();
    set.add( new NodeRenderingHandler<>(
        TeXNode.class, this::render ) );

    return set;
  }

  private void render( final TeXNode node,
                       final NodeRendererContext context,
                       final HtmlWriter html ) {
    html.tag( "tex" );
    html.raw( node.getText() );
    html.closeTag( "tex" );
  }
}
