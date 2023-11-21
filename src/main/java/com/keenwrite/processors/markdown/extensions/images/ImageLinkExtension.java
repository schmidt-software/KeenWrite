/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.images;

import com.keenwrite.ExportFormat;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.HtmlRendererAdapter;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.html.IndependentLinkResolverFactory;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.renderer.LinkResolverBasicContext;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.Node;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.SysFile.toFile;
import static com.keenwrite.util.ProtocolScheme.getProtocol;
import static com.vladsch.flexmark.html.HtmlRenderer.Builder;
import static com.vladsch.flexmark.html.renderer.LinkStatus.VALID;

/**
 * Responsible for ensuring that images can be rendered relative to a path.
 * This allows images to be located virtually anywhere.
 */
public class ImageLinkExtension extends HtmlRendererAdapter {

  private final ProcessorContext mContext;

  private ImageLinkExtension( @NotNull final ProcessorContext context ) {
    mContext = context;
  }

  /**
   * Creates an extension capable of using a relative path to embed images.
   *
   * @param context Contains the base directory to search in for images.
   * @return The new {@link ImageLinkExtension}, not {@code null}.
   */
  public static ImageLinkExtension create(
    @NotNull final ProcessorContext context ) {
    return new ImageLinkExtension( context );
  }

  @Override
  public void extend( @NotNull final Builder builder,
                      @NotNull final String rendererType ) {
    builder.linkResolverFactory( new ResolverFactory() );
  }

  private final class ResolverFactory extends IndependentLinkResolverFactory {
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
      return node instanceof Image ? forImage( link, node ) : link;
    }

    /**
     * Algorithm:
     * <ol>
     *   <li>Accept remote URLs as valid links.</li>
     *   <li>Accept existing readable files as valid links.</li>
     *   <li>Accept non-{@link ExportFormat#NONE} exports as valid links.</li>
     *   <li>Append the images dir to the edited file's dir (baseDir).</li>
     *   <li>Search for images by extension.</li>
     * </ol>
     *
     * @param link The link URL to resolve.
     * @param node The document node containing the URL.
     * @return The {@link ResolvedLink} instance used to render the link.
     */
    private ResolvedLink forImage( final ResolvedLink link, final Node node ) {
      final var url = link.getUrl();
      final var protocolScheme = getProtocol( url );

      return protocolScheme.isRemote()
        ? valid( link, url )
        : resolveImageFile( link, node, url );
    }

    private ResolvedLink resolveImageFile(
      final ResolvedLink link,
      final Node node,
      final String url ) {
      final var userPath = new File( url );

      // If the user specified a fully qualified path name, use it verbatim.
      return readable( userPath )
        ? valid( link, url )
        : resolveUnqualifiedImageFile( link, node, url );
    }

    private ResolvedLink resolveUnqualifiedImageFile(
      final ResolvedLink link,
      final Node node,
      final String url ) {
      final var baseDir = getBaseDir();
      final var fqfn = baseDir.resolve( Path.of( url ) );

      // If the image can be found relative to the base directory, then
      // use the link as is when resolving the path.
      return readable( toFile( fqfn ) )
        ? valid( link, url )
        : resolveExtensionlessImageFile( link, node, url );
    }

    private ResolvedLink resolveExtensionlessImageFile(
      final ResolvedLink link,
      final Node node,
      final String url
    ) {
      final var imagePath = new File( url );
      final var file = resolveImageExtension( imagePath );

      return file.isPresent() && readable( file.get() )
        ? valid( link, file.get().toString() )
        : resolveRelativeImageFile( link, node, url );
    }

    private ResolvedLink resolveRelativeImageFile(
      final ResolvedLink link,
      final Node node,
      final String url ) {
      final var baseDir = getBaseDir();

      try {
        // Compute the path to the image file. The base directory should
        // be an absolute path to the file being edited, without an extension.
        final var imagesDir = getImageDir();
        final var baseImagesDir = baseDir.resolve( imagesDir );
        final var imagePath = baseImagesDir.resolve( url );
        final var file = resolveImageExtension( toFile( imagePath ) );

        if( file.isPresent() ) {
          final var resolved = imagesDir.resolve( file.get().toPath() );
          final var relative = baseDir.relativize( resolved );
          return valid( link, relative.toString() );
        }

        clue( "Main.status.error.file.missing.near",
              imagePath + ".*", node.getLineNumber()
        );
      } catch( final Exception ex ) {
        clue( ex );
      }

      return link;
    }

    private Optional<File> resolveImageExtension( final File imagePath ) {
      for( final var ext : getImageOrder() ) {
        final var file = new File( imagePath.toString() + '.' + ext );

        if( readable( file ) ) {
          return Optional.of( file );
        }
      }

      return Optional.empty();
    }

    private ResolvedLink valid( final ResolvedLink link, final String url ) {
      return link.withStatus( VALID ).withUrl( url );
    }

    private Path getImageDir() {
      return mContext.getImageDir();
    }

    private Iterable<String> getImageOrder() {
      return mContext.getImageOrder();
    }

    private Path getBaseDir() {
      return mContext.getBaseDir();
    }
  }

  private static boolean readable( final File file ) {
    return file.isFile() && file.canRead();
  }
}
