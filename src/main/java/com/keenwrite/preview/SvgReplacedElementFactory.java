/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.io.HttpMediaType;
import com.keenwrite.io.MediaType;
import com.keenwrite.util.BoundedCache;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.preview.SvgRasterizer.rasterize;
import static com.keenwrite.processors.markdown.tex.TexNode.HTML_TEX;
import static com.keenwrite.util.ProtocolScheme.getProtocol;

/**
 * Responsible for running {@link SvgRasterizer} on SVG images detected within
 * a document to transform them into rasterized versions.
 */
public class SvgReplacedElementFactory implements ReplacedElementFactory {

  /**
   * Implementation of the initialization-on-demand holder design pattern,
   * an for a lazy-loaded singleton. In all versions of Java, the idiom enables
   * a safe, highly concurrent lazy initialization of static fields with good
   * performance. The implementation relies upon the initialization phase of
   * execution within the Java Virtual Machine (JVM) as specified by the Java
   * Language Specification.
   */
  private static class Container {
    private static final MathRenderer INSTANCE = new MathRenderer();
  }

  /**
   * Returns the singleton instance for rendering math symbols.
   *
   * @return A non-null instance, loaded, configured, and ready to render math.
   */
  public static MathRenderer getInstance() {
    return Container.INSTANCE;
  }

  private static final String HTML_IMAGE = "img";
  private static final String HTML_IMAGE_SRC = "src";

  private static final ImageReplacedElement BROKEN_IMAGE =
    createImageReplacedElement( BROKEN_IMAGE_PLACEHOLDER );

  /**
   * A bounded cache that removes the oldest image if the maximum number of
   * cached images has been reached. This constrains the number of images
   * loaded into memory.
   */
  private final Map<String, ImageReplacedElement> mImageCache =
    new BoundedCache<>( 150 );

  @Override
  public ReplacedElement createReplacedElement(
    final LayoutContext c,
    final BlockBox box,
    final UserAgentCallback uac,
    final int cssWidth,
    final int cssHeight ) {
    final var e = box.getElement();

    // Exit early for the speeds.
    if( e == null ) {
      return null;
    }

    // If the source image is cached, don't bother fetching. This optimization
    // avoids making multiple HTTP requests for the same URI.
    final var node = e.getNodeName();
    final var source = switch( node ) {
      case HTML_IMAGE -> e.getAttribute( HTML_IMAGE_SRC );
      case HTML_TEX -> e.getTextContent();
      default -> "";
    };

    // Non-image HTML elements shall not pass.
    if( source.isBlank() ) {
      return null;
    }

    final var image = new ImageReplacedElement[ 1 ];
    getCachedImage( source ).ifPresentOrElse(
      ( i ) -> image[ 0 ] = i,
      () -> {
        try {
          BufferedImage raster = null;

          switch( node ) {
            case HTML_IMAGE -> {
              URI uri = null;

              if( getProtocol( source ).isHttp() ) {
                var mediaType = MediaType.valueFrom( source );

                if( isSvg( mediaType ) || mediaType == UNDEFINED ) {
                  // Attempt to rasterize SVG depending on URL resource content.
                  uri = new URI( source );

                  // Attempt rasterization for SVG or plain text formats.
                  if( !isSvg( HttpMediaType.valueFrom( uri ) ) ) {
                    uri = null;
                  }
                }
              }
              else if( isSvg( MediaType.valueFrom( source ) ) ) {
                // Attempt to rasterize based on file name.
                final var base = new URI( getBaseUri( e ) ).getPath();
                uri = Paths.get( base, source ).toUri();
              }

              if( uri != null ) {
                raster = rasterize( uri, box.getContentWidth() );
              }
            }
            case HTML_TEX ->
              // Convert the TeX element to a raster graphic.
              raster = rasterize( getInstance().render( source ) );
          }

          if( raster != null ) {
            image[ 0 ] = putCachedImage( source, raster );
          }
        } catch( final Exception ex ) {
          image[ 0 ] = BROKEN_IMAGE;
          clue( ex );
        }
      }
    );

    return image[ 0 ];
  }

  private String getBaseUri( final Element e ) {
    try {
      final var doc = e.getOwnerDocument();
      final var html = doc.getDocumentElement();
      final var head = html.getFirstChild();
      final var children = head.getChildNodes();

      for( int i = children.getLength() - 1; i >= 0; i-- ) {
        final var child = children.item( i );
        final var name = child.getLocalName();

        if( "base".equalsIgnoreCase( name ) ) {
          final var attrs = child.getAttributes();
          final var item = attrs.getNamedItem( "href" );

          return item.getNodeValue();
        }
      }
    } catch( final Exception ex ) {
      clue( ex );
    }

    return "";
  }

  @Override
  public void reset() {
  }

  @Override
  public void remove( final Element e ) {
  }

  @Override
  public void setFormSubmissionListener( FormSubmissionListener listener ) {
  }

  private ImageReplacedElement putCachedImage(
    final String source, final BufferedImage image ) {
    assert source != null;
    assert image != null;

    final var result = createImageReplacedElement( image );
    mImageCache.put( source, result );
    return result;
  }

  private Optional<ImageReplacedElement> getCachedImage( final String source ) {
    return Optional.ofNullable( mImageCache.get( source ) );
  }

  private static ImageReplacedElement createImageReplacedElement(
    final BufferedImage bi ) {
    return new ImageReplacedElement( bi, bi.getWidth(), bi.getHeight() );
  }

  private static boolean isSvg( final MediaType mediaType ) {
    return mediaType == TEXT_PLAIN || mediaType == IMAGE_SVG_XML;
  }
}
