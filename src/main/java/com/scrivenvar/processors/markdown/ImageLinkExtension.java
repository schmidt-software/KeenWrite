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

import com.scrivenvar.Services;
import com.scrivenvar.preferences.UserPreferences;
import com.scrivenvar.service.Options;
import com.scrivenvar.service.events.Notifier;
import com.scrivenvar.util.ProtocolResolver;
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
import org.renjin.repackaged.guava.base.Splitter;

import java.io.File;
import java.nio.file.Path;

import static java.lang.String.format;

/**
 * Responsible for ensuring that images can be rendered relative to a path.
 * This allows images to be located virtually anywhere.
 *
 * @author White Magic Software, Ltd.
 */
public class ImageLinkExtension implements HtmlRenderer.HtmlRendererExtension {
  private final static Options sOptions = Services.load( Options.class );
  private final static Notifier sNotifier = Services.load( Notifier.class );

  /**
   * Creates an extension capable of using a relative path to embed images.
   *
   * @param path The {@link Path} to the file being edited; the parent path
   *             is the starting location of the relative image directory.
   * @return The new {@link ImageLinkExtension}, never {@code null}.
   */
  public static ImageLinkExtension create( final Path path ) {
    return new ImageLinkExtension( path );
  }

  private class Factory extends IndependentLinkResolverFactory {
    @Override
    public @NotNull LinkResolver apply(
        @NotNull final LinkResolverBasicContext context ) {
      return new ImageLinkResolver();
    }
  }

  private class ImageLinkResolver implements LinkResolver {
    private final UserPreferences mUserPref = getUserPreferences();
    private final String mImagePrefix =
        mUserPref.getImagesDirectory().toString();
    private final String mImageSuffixes = mUserPref.getImagesOrder();

    public ImageLinkResolver() {
    }

    // you can also set/clear/modify attributes through
    // ResolvedLink.getAttributes() and
    // ResolvedLink.getNonNullAttributes()
    @NotNull
    @Override
    public ResolvedLink resolveLink(
        @NotNull final Node node,
        @NotNull final LinkResolverBasicContext context,
        @NotNull final ResolvedLink link ) {
      return node instanceof Image ? resolve( link ) : link;
    }

    @NotNull
    private ResolvedLink resolve( @NotNull final ResolvedLink link ) {
      String url = link.getUrl();

      try {
        final String imageFile = format( "%s/%s", getImagePrefix(), url );
        final String suffixes = getImageSuffixes();
        final String editDir = getEditDirectory();

        for( final String ext : Splitter.on( ' ' ).split( suffixes ) ) {
          final String imagePath = format(
              "%s/%s.%s", editDir, imageFile, ext );
          final File file = new File( imagePath );

          if( file.exists() ) {
            url = file.toString();
            break;
          }
        }

        final String protocol = ProtocolResolver.getProtocol( url );
        if( "file".equals( protocol ) ) {
          url = "file://" + url;
        }

        return link.withStatus( LinkStatus.VALID ).withUrl( url );
      } catch( final Exception e ) {
        getNotifier().notify( e );
      }

      return link;
    }

    private String getImagePrefix() {
      return mImagePrefix;
    }

    private String getImageSuffixes() {
      return mImageSuffixes;
    }

    private String getEditDirectory() {
      return mPath.getParent().toString();
    }
  }

  private final Path mPath;

  private ImageLinkExtension( final Path path ) {
    mPath = path;
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

  private UserPreferences getUserPreferences() {
    return getOptions().getUserPreferences();
  }

  private Options getOptions() {
    return sOptions;
  }

  private Notifier getNotifier() {
    return sNotifier;
  }
}
