/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.exceptions.MissingFileException;
import com.keenwrite.preferences.Workspace;
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
import static com.keenwrite.preferences.Workspace.KEY_IMAGES_DIR;
import static com.keenwrite.preferences.Workspace.KEY_IMAGES_ORDER;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
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
   * @param basePath  The directory to search for images, either directly or
   *                  through the images directory setting, not {@code null}.
   * @param workspace Contains user preferences for image directory and image
   *                  file name extension lookup order.
   * @return The new {@link ImageLinkExtension}, not {@code null}.
   */
  public static ImageLinkExtension create(
    @NotNull final Path basePath,
    @NotNull final Workspace workspace ) {
    return new ImageLinkExtension( basePath, workspace );
  }

  private final Path mBasePath;
  private final Workspace mWorkspace;

  private ImageLinkExtension(
    @NotNull final Path basePath, @NotNull final Workspace workspace ) {
    mBasePath = basePath;
    mWorkspace = workspace;
  }

  @Override
  public void rendererOptions( @NotNull final MutableDataHolder options ) {
  }

  @Override
  public void extend(
    @NotNull final Builder builder, @NotNull final String rendererType ) {
    builder.linkResolverFactory( new Factory() );
  }

  private class Factory extends IndependentLinkResolverFactory {
    @Override
    public @NotNull LinkResolver apply(
      @NotNull final LinkResolverBasicContext context ) {
      return new ImageLinkResolver();
    }
  }

  private class ImageLinkResolver implements LinkResolver {
    public ImageLinkResolver() {
    }

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

      // Determine the fully-qualified file name (fqfn).
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
      return mWorkspace.toFile( KEY_IMAGES_DIR ).toPath();
    }

    private String getImageExtensions() {
      return mWorkspace.toString( KEY_IMAGES_ORDER );
    }

    private Path getBasePath() {
      return mBasePath;
    }
  }
}
