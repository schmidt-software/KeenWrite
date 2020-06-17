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
package com.scrivenvar.preview;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.*;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.batik.transcoder.image.ImageTranscoder.KEY_BACKGROUND_COLOR;
import static org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName;

public class SVGRasterizer {
  private final static SAXSVGDocumentFactory mFactory =
      new SAXSVGDocumentFactory( getXMLParserClassName() );

  private final static Map<Object, Object> RENDERING_HINTS = Map.of(
      KEY_ALPHA_INTERPOLATION,
      VALUE_ALPHA_INTERPOLATION_QUALITY,
      KEY_INTERPOLATION,
      VALUE_INTERPOLATION_BICUBIC,
      KEY_ANTIALIASING,
      VALUE_ANTIALIAS_ON,
      KEY_COLOR_RENDERING,
      VALUE_COLOR_RENDER_QUALITY,
      KEY_DITHERING,
      VALUE_DITHER_DISABLE,
      KEY_RENDERING,
      VALUE_RENDER_QUALITY,
      KEY_STROKE_CONTROL,
      VALUE_STROKE_PURE,
      KEY_FRACTIONALMETRICS,
      VALUE_FRACTIONALMETRICS_ON,
      KEY_TEXT_ANTIALIASING,
      VALUE_TEXT_ANTIALIAS_OFF
  );

  private static class BufferedImageTranscoder extends ImageTranscoder {
    private BufferedImage mImage;

    @Override
    public BufferedImage createImage( final int w, final int h ) {
      return new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
    }

    @Override
    public void writeImage(
        final BufferedImage image, final TranscoderOutput output ) {
      mImage = image;
    }

    public BufferedImage getBufferedImage() {
      return mImage;
    }

    @Override
    protected ImageRenderer createRenderer() {
      final ImageRenderer renderer = super.createRenderer();
      final RenderingHints hints = renderer.getRenderingHints();
      hints.putAll( RENDERING_HINTS );

      renderer.setRenderingHints( hints );

      return renderer;
    }
  }

  public static BufferedImage rasterize( final URL url, final int width )
      throws IOException, TranscoderException {
    return rasterize(
        (SVGDocument) mFactory.createDocument( url.toString() ), width );
  }

  public static BufferedImage rasterize(
      final SVGDocument svg, final int width ) throws TranscoderException {
    final var transcoder = new BufferedImageTranscoder();
    final var input = new TranscoderInput( svg );

    transcoder.addTranscodingHint( KEY_BACKGROUND_COLOR, WHITE );
    transcoder.addTranscodingHint( KEY_WIDTH, (float) width );
    transcoder.transcode( input, null );

    return transcoder.getBufferedImage();
  }
}
