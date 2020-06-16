/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.processors.markdown;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.IndependentLinkResolverFactory;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for ensuring that images can be rendered relative to a path.
 * This allows images to be located virtually anywhere.
 *
 * @author White Magic Software, Ltd.
 */
public class ImageLinkExtension implements HtmlRenderer.HtmlRendererExtension {

  public static ImageLinkExtension create() {
    return new ImageLinkExtension();
  }

  private static class Factory extends IndependentLinkResolverFactory {
    @Override
    public @NotNull LinkResolver apply(
        @NotNull final LinkResolverBasicContext context ) {
      return new ImageLinkResolver();
    }
  }

  private static class ImageLinkResolver implements LinkResolver {
    public ImageLinkResolver() {
    }

    // you can also set/clear/modify attributes through
    // ResolvedLink.getAttributes() and
    // ResolvedLink.getNonNullAttributes()
    @NotNull
    public ResolvedLink resolveLink(
        @NotNull final Node node,
        @NotNull final LinkResolverBasicContext context,
        @NotNull final ResolvedLink link ) {
      return node instanceof Image ? resolve( (Image) node, link ) : link;
    }

    @NotNull
    private ResolvedLink resolve(
        @NotNull final Image image, @NotNull final ResolvedLink link ) {
      final String url = link.getUrl();

      try {
        //URI uri = new URI( url );

//        System.out.println( "image: " + image.toString() );
//        System.out.println( "Absolute: " + uri.isAbsolute() );
//        System.out.println( "Host: " + uri.getHost() );

        final String imageUrl = link.getUrl();

        return link.withStatus( LinkStatus.VALID )
                   .withUrl( imageUrl );

      } catch( final Exception e ) {
        System.out.println( "Bad URI: " + url );
      }

      return link;
    }
  }

  private ImageLinkExtension() {
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }

  @Override
  public void extend(
      final HtmlRenderer.Builder rendererBuilder,
      @NotNull final String rendererType ) {
    rendererBuilder.linkResolverFactory( new Factory() );
  }

}
