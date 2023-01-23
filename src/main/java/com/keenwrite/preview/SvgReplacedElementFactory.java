/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.io.MediaType;
import com.keenwrite.ui.adapters.ReplacedElementAdapter;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.nio.file.Path;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static com.keenwrite.preview.SvgRasterizer.*;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.HTML_TEX;
import static com.keenwrite.util.ProtocolScheme.getProtocol;

/**
 * Responsible for running {@link SvgRasterizer} on SVG images detected within
 * a document to transform them into rasterized versions.
 */
public final class SvgReplacedElementFactory extends ReplacedElementAdapter {

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
            try( final var response = open( source ) ) {
              if( response.isSvg() ) {
                // Rasterize SVG from URL resource.
                raster = rasterize(
                  response.getInputStream(),
                  box.getContentWidth()
                );
              }

              clue( "Main.status.image.request.fetch", source );
            }
          }
          else if( MediaType.fromFilename( source ).isSvg() ) {
            // Attempt to rasterize based on file name.
            final var path = Path.of( new URI( source ).getPath() );

            if( path.isAbsolute() ) {
              uri = path.toUri();
            }
            else {
              final var base = new URI( e.getBaseURI() ).getPath();
              uri = Path.of( base, source ).toUri();
            }
          }

          if( uri != null ) {
            raster = rasterize( uri, box.getContentWidth() );
          }
        }
        case HTML_TEX ->
          // Convert the TeX element to a raster graphic.
          raster = rasterizeImage( MathRenderer.toString( e.getTextContent() ), 2.5 );
      }

      if( raster != null ) {
        image = createImageReplacedElement( raster );
      }
    } catch( final Exception ex ) {
      image = BROKEN_IMAGE;
      clue( ex );
    }

    return image == null ? BROKEN_IMAGE : image;
  }

  private static ImageReplacedElement createImageReplacedElement(
    final BufferedImage bi ) {
    return new ImageReplacedElement( bi, bi.getWidth(), bi.getHeight() );
  }
}
