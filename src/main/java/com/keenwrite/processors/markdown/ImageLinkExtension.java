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

import com.keenwrite.exceptions.MissingFileException;
import com.keenwrite.preferences.UserPreferences;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.html.IndependentLinkResolverFactory;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
import org.renjin.repackaged.guava.base.Splitter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.util.ProtocolResolver.getProtocol;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import static com.vladsch.flexmark.html.renderer.LinkStatus.VALID;
import static java.lang.String.format;

/**
 * Responsible for ensuring that images can be rendered relative to a path.
 * This allows images to be located virtually anywhere.
 */
public class ImageLinkExtension implements HtmlRendererExtension {

  /**
   * Creates an extension capable of using a relative path to embed images.
   *
   * @param basePath The directory to search for images, either directly or
   *                 through the images directory setting, not {@code null}.
   * @return The new {@link ImageLinkExtension}, not {@code null}.
   */
  public static ImageLinkExtension create( @NotNull final Path basePath ) {
    return new ImageLinkExtension( basePath );
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
    private final File mImagesUserPrefix = mUserPref.getImagesDirectory();
    private final String mImageExtensions = mUserPref.getImagesOrder();

    public ImageLinkResolver() {
    }

    /**
     * You can also set/clear/modify attributes through
     * {@link ResolvedLink#getAttributes()} and
     * {@link ResolvedLink#getNonNullAttributes()}.
     */
    @NotNull
    @Override
    public ResolvedLink resolveLink(
        @NotNull final Node node,
        @NotNull final LinkResolverBasicContext context,
        @NotNull final ResolvedLink link ) {
      return node instanceof Image ? resolve( link ) : link;
    }

    private ResolvedLink resolve( final ResolvedLink link ) {
      var uri = link.getUrl();
      final var protocol = getProtocol( uri );

      if( protocol.isHttp() ) {
        return valid( link, uri );
      }

      // Determine the fully-qualified filename (fqfn).
      final var fqfn = Paths.get( getBasePath().toString(), uri ).toFile();

      if( fqfn.isFile() ) {
        return valid( link, uri );
      }

      // At this point either the image directory is qualified or needs to be
      // qualified using the image prefix, as set in the user preferences.

      try {
        final var imagePrefix = getImagePrefix();
        final var basePath = getBasePath().resolve( imagePrefix );

        final var imagePathPrefix = Path.of( basePath.toString(), uri );
        final var suffixes = getImageExtensions();
        boolean missing = true;

        // Iterate over the user's preferred image file type extensions.
        for( final var ext : Splitter.on( ' ' ).split( suffixes ) ) {
          final var imagePath = format( "%s.%s", imagePathPrefix, ext );
          final var file = new File( imagePath );

          if( file.exists() ) {
            uri += '.' + ext;
            final var path = Path.of( imagePrefix.toString(), uri );
            uri = path.normalize().toString();
            missing = false;
            break;
          }
        }

        if( missing ) {
          throw new MissingFileException( imagePathPrefix + ".*" );
        }

        return valid( link, uri );
      } catch( final Exception ex ) {
        clue( ex );
      }

      return link;
    }

    private ResolvedLink valid( final ResolvedLink link, final String url ) {
      return link.withStatus( VALID ).withUrl( url );
    }

    private Path getImagePrefix() {
      return mImagesUserPrefix.toPath();
    }

    private String getImageExtensions() {
      return mImageExtensions;
    }

    private Path getBasePath() {
      return mBasePath;
    }
  }

  private final Path mBasePath;

  private ImageLinkExtension( @NotNull final Path basePath ) {
    mBasePath = basePath;
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }

  @Override
  public void extend( @NotNull final Builder builder,
                      @NotNull final String rendererType ) {
    builder.linkResolverFactory( new Factory() );
  }

  private UserPreferences getUserPreferences() {
    return UserPreferences.getInstance();
  }
}
