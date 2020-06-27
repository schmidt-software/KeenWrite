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

import com.scrivenvar.Services;
import com.scrivenvar.service.events.Notifier;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.batik.transcoder.image.ImageTranscoder.KEY_BACKGROUND_COLOR;
import static org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName;

public class SVGRasterizer {
  private final static Notifier NOTIFIER = Services.load( Notifier.class );

  private final static SAXSVGDocumentFactory mFactory =
      new SAXSVGDocumentFactory( getXMLParserClassName() );

  public final static Map<Object, Object> RENDERING_HINTS = Map.of(
      KEY_ANTIALIASING,
      VALUE_ANTIALIAS_ON,
      KEY_ALPHA_INTERPOLATION,
      VALUE_ALPHA_INTERPOLATION_QUALITY,
      KEY_COLOR_RENDERING,
      VALUE_COLOR_RENDER_QUALITY,
      KEY_DITHERING,
      VALUE_DITHER_DISABLE,
      KEY_FRACTIONALMETRICS,
      VALUE_FRACTIONALMETRICS_ON,
      KEY_INTERPOLATION,
      VALUE_INTERPOLATION_BICUBIC,
      KEY_RENDERING,
      VALUE_RENDER_QUALITY,
      KEY_STROKE_CONTROL,
      VALUE_STROKE_PURE,
      KEY_TEXT_ANTIALIASING,
      VALUE_TEXT_ANTIALIAS_ON
  );

  public final static Image BROKEN_IMAGE_PLACEHOLDER;

  static {
    // A FontAwesome camera icon, cleft asunder.
    final String BROKEN_IMAGE_SVG = "<svg height='79pt' viewBox='0 0 100 79' " +
        "width='100pt' xmlns='http://www.w3.org/2000/svg'><g " +
        "fill='#454545'><path d='m32.175781 46.207031c1.316407 6.023438 6" +
        ".628907 10.4375 12.847657 10.675781zm0 0'/><path d='m27.167969 40" +
        ".105469-1.195313.949219.96875.804687c.050782-.59375.125-1.175781" +
        ".226563-1.753906zm0 0'/><path d='m42.394531 3.949219-10.054687" +
        ".875c-3.105469.269531-5.71875 2.414062-6.546875 5.382812l-1.464844 5" +
        ".222657-13.660156 1.183593c-4.113281.355469-7.160157 3.949219-6" +
        ".800781 8.023438l3.910156 44.269531c.363281 4.070312 3.992187 7" +
        ".085938 8.105468 6.730469l46.832032-4.058594-12.457032-10.347656c-" +
        ".992187.253906-2.007812.453125-3.0625.542969-10.277343.890624-19" +
        ".359374-6.65625-20.261718-16.832032-.089844-1.042968-.070313-2" +
        ".070312.007812-3.082031l-.96875-.804687 1.195313-.949219c1.4375-8" +
        ".042969 8.160156-14.476563 16.765625-15.222657.835937-.074218 1" +
        ".660156-.070312 2.476562-.035156l3.726563-2.953125zm0 0'/><path " +
        "d='m40.9375 46.152344 11.859375 11.742187c.570313.070313 1.144531" +
        ".121094 1.730469.121094 7.558594 0 13.714844-6.09375 13.714844-13" +
        ".578125 0-7.480469-6.15625-13.578125-13.714844-13.578125s-13.714844 " +
        "6.097656-13.714844 13.578125c0 .582031.050781 1.152344.125 1" +
        ".714844zm0 0'/><path d='m57.953125 3.363281 4.472656 19-4.183593 2" +
        ".269531c9.007812 1.988282 15.382812 10.316407 14.554687 19.664063-" +
        ".804687 9.132813-8.207031 16.128906-17.148437 16.824219l10.453124 12" +
        ".335937 17.75 1.539063c4.113282.355468 7.742188-2.660156 8.101563-6" +
        ".734375l3.910156-44.265625c.363281-4.074219-2.683593-7.667969-6" +
        ".796875-8.023438l-13.660156-1.183594-1.480469-5.226562c-.832031-2" +
        ".96875-3.441406-5.113281-6.546875-5.382812zm0 0'/></g></svg>";

    // The width and height cannot be embedded in the SVG above because the
    // path element values are relative to the viewBox dimensions.
    final int w = 150;
    final int h = 150;
    Image image;

    try( final StringReader reader = new StringReader( BROKEN_IMAGE_SVG ) ) {
      final String parser = XMLResourceDescriptor.getXMLParserClassName();
      final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory( parser );
      final Document document = factory.createDocument( "", reader );

      image = rasterize( document, w );
    } catch( final Exception e ) {
      image = new BufferedImage( w, h, TYPE_INT_RGB );
      final var graphics = (Graphics2D) image.getGraphics();
      graphics.setRenderingHints( RENDERING_HINTS );

      // Fall back to a (\) symbol.
      graphics.setColor( new Color( 204, 204, 204 ) );
      graphics.fillRect( 0, 0, w, h );
      graphics.setColor( new Color( 255, 204, 204 ) );
      graphics.setStroke( new BasicStroke( 4 ) );
      graphics.drawOval( w / 4, h / 4, w / 2, h / 2 );
      graphics.drawLine( w / 4 + (int) (w / 4 / Math.PI),
                         h / 4 + (int) (w / 4 / Math.PI),
                         w / 2 + w / 4 - (int) (w / 4 / Math.PI),
                         h / 2 + h / 4 - (int) (w / 4 / Math.PI) );
    }

    BROKEN_IMAGE_PLACEHOLDER = image;
  }

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

    public Image getImage() {
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

  /**
   * Rasterizes the vector graphic file at the given URL. If any exception
   * happens, a red circle is returned instead.
   *
   * @param url   The URL to a vector graphic file, which must include the
   *              protocol scheme (such as file:// or https://).
   * @param width The number of pixels wide to render the image. The aspect
   *              ratio is maintained.
   * @return Either the rasterized image upon success or a red circle.
   */
  public static Image rasterize( final String url, final int width ) {
    try {
      return rasterize( new URL( url ), width );
    } catch( final Exception e ) {
      NOTIFIER.notify( e );
      return BROKEN_IMAGE_PLACEHOLDER;
    }
  }

  /**
   * Converts an SVG drawing into a rasterized image that can be drawn on
   * a graphics context.
   *
   * @param url   The path to the image (can be web address).
   * @param width Scale the image width to this size (aspect ratio is
   *              maintained).
   * @return The vector graphic transcoded into a raster image format.
   * @throws IOException         Could not read the vector graphic.
   * @throws TranscoderException Could not convert the vector graphic to an
   *                             instance of {@link Image}.
   */
  public static Image rasterize( final URL url, final int width )
      throws IOException, TranscoderException {
    return rasterize(
        mFactory.createDocument( url.toString() ), width );
  }

  public static Image rasterize(
      final Document svg, final int width ) throws TranscoderException {
    final var transcoder = new BufferedImageTranscoder();
    final var input = new TranscoderInput( svg );

    transcoder.addTranscodingHint( KEY_BACKGROUND_COLOR, WHITE );
    transcoder.addTranscodingHint( KEY_WIDTH, (float) width );
    transcoder.transcode( input, null );

    return transcoder.getImage();
  }
}
