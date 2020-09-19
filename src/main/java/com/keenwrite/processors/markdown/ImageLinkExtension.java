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
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;
import org.renjin.repackaged.guava.base.Splitter;

import java.io.File;
import java.nio.file.Path;

import static com.keenwrite.StatusBarNotifier.alert;
import static com.keenwrite.util.ProtocolResolver.getProtocol;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Responsible for ensuring that images can be rendered relative to a path.
 * This allows images to be located virtually anywhere.
 */
public class ImageLinkExtension implements HtmlRendererExtension {

  /**
   * Creates an extension capable of using a relative path to embed images.
   *
   * @param path The {@link Path} to the file being edited; the parent path
   *             is the starting location of the relative image directory.
   * @return The new {@link ImageLinkExtension}, never {@code null}.
   */
  public static ImageLinkExtension create( @NotNull final Path path ) {
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
      var url = link.getUrl();
      final var protocol = getProtocol( url );

      try {
        if( protocol.isHttp() ) {
          return valid( link, url );
        }
      } catch( final Exception ignored ) {
        // Try to resolve the image path, dynamically.
      }

      try {
        final Path imagePrefix = getImagePrefix().toPath();

        // Path to the file being edited.
        Path editPath = getEditPath();

        // If there is no parent path to the file, it means the file has not
        // been saved. Default to using the value from the user's preferences.
        // The user's preferences will be defaulted to a the application's
        // starting directory.
        editPath = editPath == null
            ? imagePrefix
            : Path.of( editPath.toString(), imagePrefix.toString() );

        final var urlExt = getExtension( url );
        url = removeExtension( url );

        final var suffixes = urlExt + ' ' + getImageExtensions();
        final var imagePathPrefix = Path.of( editPath.toString(), url );
        var suffix = ".*";
        boolean missing = true;

        // Iterate over the user's preferred image file type extensions.
        for( final String ext : Splitter.on( ' ' ).split( suffixes ) ) {
          final String imagePath = format( "%s.%s", imagePathPrefix, ext );
          final File file = new File( imagePath );

          if( file.exists() ) {
            url = file.toString();
            missing = false;
            break;
          }
          else if( !urlExt.isBlank() ) {
            // The file is missing because the user specified a prefix.
            suffix = urlExt;
            break;
          }
        }

        if( missing ) {
          throw new MissingFileException( imagePathPrefix + suffix );
        }

        if( protocol.isFile() ) {
          url = "file://" + url;
        }

        return valid( link, url );
      } catch( final Exception ex ) {
        alert( ex );
      }

      return link;
    }

    private ResolvedLink valid( final ResolvedLink link, final String url ) {
      return link.withStatus( LinkStatus.VALID ).withUrl( url );
    }

    private File getImagePrefix() {
      return mImagesUserPrefix;
    }

    private String getImageExtensions() {
      return mImageExtensions;
    }

    private Path getEditPath() {
      return mPath.getParent();
    }
  }

  private final Path mPath;

  private ImageLinkExtension( @NotNull final Path path ) {
    mPath = path;
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
