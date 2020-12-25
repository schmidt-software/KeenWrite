/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

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
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.io.MediaType.IMAGE_SVG_XML;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.preview.SvgRasterizer.rasterize;
import static com.keenwrite.processors.markdown.tex.TexNode.HTML_TEX;

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

  /**
   * A bounded cache that removes the oldest image if the maximum number of
   * cached images has been reached. This constrains the number of images
   * loaded into memory.
   */
  private final Map<String, BufferedImage> mImageCache =
    new BoundedCache<>( 150 );

  @Override
  public ReplacedElement createReplacedElement(
    final LayoutContext c,
    final BlockBox box,
    final UserAgentCallback uac,
    final int cssWidth,
    final int cssHeight ) {
    final var e = box.getElement();
    BufferedImage image = null;

    if( e != null ) {
      switch( e.getNodeName() ) {
        case HTML_IMAGE -> {
          final var src = e.getAttribute( HTML_IMAGE_SRC );

          if( MediaType.valueOf( new File( src ) ) == IMAGE_SVG_XML ) {
            try {
              final var baseUri = getBaseUri( e );
              final var uri = new URI( baseUri ).getPath();
              final var path = Paths.get( uri, src );

              image = getCachedImage(
                src, svg -> rasterize( path, box.getContentWidth() ) );
            } catch( final Exception ex ) {
              image = BROKEN_IMAGE_PLACEHOLDER;
              clue( ex );
            }
          }
        }
        case HTML_TEX -> {
          // Convert the TeX element to a raster graphic if not yet cached.
          final var src = e.getTextContent();
          image = getCachedImage(
            src, __ -> rasterize( getInstance().render( src ) )
          );
        }
      }
    }

    if( image != null ) {
      final var w = image.getWidth( null );
      final var h = image.getHeight( null );

      return new ImageReplacedElement( image, w, h );
    }

    return null;
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

  /**
   * Returns an image associated with a string; the string's pre-computed
   * hash code is returned as the string value, making this operation very
   * quick to return the corresponding {@link BufferedImage}.
   *
   * @param src        The source used for the key into the image cache.
   * @param rasterizer {@link Function} to call to rasterize an image.
   * @return The image that corresponds to the given source string.
   */
  private BufferedImage getCachedImage(
    final String src, final Function<String, BufferedImage> rasterizer ) {
    return mImageCache.computeIfAbsent( src, __ -> rasterizer.apply( src ) );
  }
}
