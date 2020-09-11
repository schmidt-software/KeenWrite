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
package com.scrivenvar.graphics;

import com.scrivenvar.Services;
import com.scrivenvar.service.events.Notifier;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.scrivenvar.graphics.SvgRasterizer.*;

/**
 * Responsible for running {@link SvgRasterizer} on SVG images detected within
 * a document to transform them into rasterized versions.
 */
public class SvgReplacedElementFactory
    implements ReplacedElementFactory {

  private static final Notifier sNotifier = Services.load( Notifier.class );

  /**
   * SVG filename extension maps to an SVG image element.
   */
  private static final String SVG_FILE = "svg";
  private static final String HTML_SVG = "svg";

  private static final String HTML_IMAGE = "img";
  private static final String HTML_IMAGE_SRC = "src";

  /**
   * Constrain memory.
   */
  private static final int MAX_CACHED_IMAGES = 100;

  /**
   * Where to put cached image files.
   */
  private final Map<String, BufferedImage> mImageCache = new LinkedHashMap<>() {
    @Override
    protected boolean removeEldestEntry(
        final Map.Entry<String, BufferedImage> eldest ) {
      return size() > MAX_CACHED_IMAGES;
    }
  };

  @Override
  public ReplacedElement createReplacedElement(
      final LayoutContext c,
      final BlockBox box,
      final UserAgentCallback uac,
      final int cssWidth,
      final int cssHeight ) {
    BufferedImage image = null;
    final var e = box.getElement();

    if( e != null ) {
      final var nodeName = e.getNodeName();

      if( HTML_IMAGE.equals( nodeName ) ) {
        final var src = e.getAttribute( HTML_IMAGE_SRC );
        final var ext = FilenameUtils.getExtension( src );

        if( SVG_FILE.equalsIgnoreCase( ext ) ) {
          try {
            image = getImage( src, box.getContentWidth() );
          } catch( final Exception ex ) {
            getNotifier().notify( ex );
          }
        }
      }
      else if( HTML_SVG.equalsIgnoreCase( nodeName ) ) {
        // Convert the <svg> element to a raster graphic.
        try {
          //final int width = box.getContentWidth();
          final var svg = toSvg( e );
          image = rasterizeString( svg );
          ImageIO.write( image, "png", new File( "/tmp/svg.png" ) );
        } catch( final Exception ex ) {
          getNotifier().notify( ex );
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

  @Override
  public void reset() {
  }

  @Override
  public void remove( final Element e ) {
  }

  @Override
  public void setFormSubmissionListener( FormSubmissionListener listener ) {
  }

  private BufferedImage getImage( final String src, final int width ) {
    return mImageCache.computeIfAbsent( src, v -> rasterize( src, width ) );
  }

  private Notifier getNotifier() {
    return sNotifier;
  }
}
