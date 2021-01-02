/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.io.HttpMediaType;
import com.keenwrite.io.MediaType;
import com.keenwrite.ui.adapters.ReplacedElementAdapter;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.file.Paths;

import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.io.MediaType.*;
import static com.keenwrite.preview.MathRenderer.MATH_RENDERER;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.preview.SvgRasterizer.rasterize;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.HTML_TEX;
import static com.keenwrite.util.ProtocolScheme.getProtocol;

/**
 * Responsible for running {@link SvgRasterizer} on SVG images detected within
 * a document to transform them into rasterized versions.
 */
public class SvgReplacedElementFactory extends ReplacedElementAdapter {

  public static final String HTML_IMAGE = "img";
  public static final String HTML_IMAGE_SRC = "src";

  private static final ImageReplacedElement BROKEN_IMAGE =
    createImageReplacedElement( BROKEN_IMAGE_PLACEHOLDER );

  @Override
  public ReplacedElement createReplacedElement(
    final LayoutContext c,
    final BlockBox box,
    final UserAgentCallback uac,
    final int cssWidth,
    final int cssHeight ) {
    final var e = box.getElement();

    ImageReplacedElement image = null;

    try {
      BufferedImage raster = null;

      switch( e.getNodeName() ) {
        case HTML_IMAGE -> {
          final var source = e.getAttribute( HTML_IMAGE_SRC );
          URI uri = null;

          if( getProtocol( source ).isHttp() ) {
            var mediaType = MediaType.valueFrom( source );

            if( isSvg( mediaType ) || mediaType == UNDEFINED ) {
              uri = new URI( source );

              // Attempt to rasterize SVG depending on URL resource content.
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
          raster = rasterize( MATH_RENDERER.render( e.getTextContent() ) );
      }

      if( raster != null ) {
        image = createImageReplacedElement( raster );
      }
    } catch( final Exception ex ) {
      image = BROKEN_IMAGE;
      clue( ex );
    }

    return image;
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

  private static ImageReplacedElement createImageReplacedElement(
    final BufferedImage bi ) {
    return new ImageReplacedElement( bi, bi.getWidth(), bi.getHeight() );
  }

  private static boolean isSvg( final MediaType mediaType ) {
    return mediaType == TEXT_PLAIN || mediaType == IMAGE_SVG_XML;
  }
}
