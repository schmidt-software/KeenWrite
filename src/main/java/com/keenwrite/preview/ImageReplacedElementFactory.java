/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.preview;

import com.keenwrite.io.MediaType;
import com.keenwrite.ui.adapters.ReplacedElementAdapter;
import io.sf.carte.echosvg.transcoder.TranscoderException;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.io.downloads.DownloadManager.open;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.preview.SvgRasterizer.rasterize;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.HTML_TEX;
import static com.keenwrite.util.ProtocolScheme.getProtocol;

/**
 * Responsible for running {@link SvgRasterizer} on SVG images detected within
 * a document to transform them into rasterized versions. This will fall back
 * to loading rasterized images from a file if not detected as SVG.
 */
public final class ImageReplacedElementFactory extends ReplacedElementAdapter {

  public static final String HTML_IMAGE = "img";
  public static final String HTML_IMAGE_SRC = "src";

  private static final ImageReplacedElement BROKEN_IMAGE =
    createElement( BROKEN_IMAGE_PLACEHOLDER );

  @Override
  public ReplacedElement createReplacedElement(
    final LayoutContext c,
    final BlockBox box,
    final UserAgentCallback uac,
    final int cssWidth,
    final int cssHeight ) {
    final var e = box.getElement();

    try {
      final BufferedImage raster =
        switch( e.getNodeName() ) {
          case HTML_IMAGE -> createHtmlImage( box, e, uac );
          case HTML_TEX -> createTexImage( e );
          default -> null;
        };

      return createElement( raster );
    } catch( final Exception ex ) {
      clue( "Main.status.image.request.error.rasterize", ex );
    }

    return BROKEN_IMAGE;
  }

  /**
   * Convert an HTML element to a raster graphic.
   */
  private static BufferedImage createHtmlImage(
    final BlockBox box,
    final Element e,
    final UserAgentCallback uac )
    throws TranscoderException, URISyntaxException, IOException {
    final var source = e.getAttribute( HTML_IMAGE_SRC );
    final var mediaType = MediaType.fromFilename( source );

    URI uri = null;
    BufferedImage raster = null;

    final var w = box.getContentWidth();

    if( getProtocol( source ).isRemote() ) {
      try( final var response = open( source );
           final var stream = response.getInputStream() ) {

        // Rasterize SVG from URL resource.
        raster = response.isSvg()
          ? rasterize( stream, w )
          : ImageIO.read( stream );

        clue( "Main.status.image.request.fetch", source );
      }
    }
    else if( mediaType.isSvg() ) {
      uri = resolve( source, uac, e );
    }

    if( uri != null && w > 0 ) {
      raster = rasterize( uri, w );
    }

    // Not an SVG, attempt to read a local rasterized image.
    if( raster == null && mediaType.isImage() ) {
      uri = resolve( source, uac, e );
      final var path = Path.of( uri.getPath() );

      try( final var stream = Files.newInputStream( path ) ) {
        raster = ImageIO.read( stream );
      }
    }

    return raster;
  }

  private static URI resolve(
    final String source,
    final UserAgentCallback uac,
    final Element e )
    throws URISyntaxException {
    // Attempt to rasterize based on file name.
    final var baseUri = new URI( uac.getBaseURL() );
    final var path = baseUri.resolve( source ).normalize();

    if( path.isAbsolute() ) {
      return path;
    }
    else {
      final var base = new URI( e.getBaseURI() ).getPath();
      return Path.of( base, source ).toUri();
    }
  }

  /**
   * Convert the TeX element to a raster graphic.
   */
  private BufferedImage createTexImage( final Element e )
    throws TranscoderException, ParseException {
    return rasterize( MathRenderer.toString( e.getTextContent() ) );
  }

  private static ImageReplacedElement createElement( final BufferedImage bi ) {
    return bi == null
      ? BROKEN_IMAGE
      : new ImageReplacedElement( bi, bi.getWidth(), bi.getHeight() );
  }
}
