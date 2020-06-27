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
    // @formatter:off
    final String BROKEN_IMAGE_SVG = "<svg xmlns='http://www.w3.org/2000/svg' " +
        "viewBox='0 0 150 150' width='150pt' height='150pt' " +
        "fill='#454545'><path d='m41.738281 87.738281c2.480469 11.433594 12" +
        ".464844 19.8125 24.15625 20.269531zm0 0'/><path d='m32.328125 76" +
        ".152344-2.25 1.800781 1.824219 1.527344c.089844-1.125.230468-2.230469" +
        ".425781-3.328125zm0 0'/><path d='m60.953125 7.5-18.90625 1.65625c-5" +
        ".835937.511719-10.746094 4.585938-12.308594 10.222656l-2.75 9" +
        ".917969-25.683593 2.246094c-7.734376.675781-13.460938 7.5-12.785157 15" +
        ".234375l7.355469 84.054687c.675781 7.734375 7.5 13.457031 15.234375 12" +
        ".78125l88.042969-7.703125-23.417969-19.648437c-1.867187.480469-3" +
        ".777344.855469-5.757813 1.027343-19.324218 1.691407-36.398437-12" +
        ".636718-38.089843-31.957031-.171875-1.980469-.136719-3.929687.015625-5" +
        ".851562l-1.824219-1.527344 2.25-1.800781c2.703125-15.277344 15" +
        ".339844-27.492188 31.519531-28.90625 1.570313-.140625 3.121094-.132813" +
        " 4.65625-.066406l7-5.605469zm0 0'/><path d='m58.210938 87.628906 22" +
        ".296874 22.300782c1.070313.132812 2.148438.226562 3.253907.226562 14" +
        ".210937 0 25.78125-11.570312 25.78125-25.78125 0-14.207031-11" +
        ".570313-25.78125-25.78125-25.78125-14.207031 0-25.78125 11.574219-25" +
        ".78125 25.78125 0 1.105469.09375 2.1875.230469 3.253906zm0 0'/><path " +
        "d='m90.203125 6.382812 8.410156 36.082032-7.867187 4.304687c16.9375 3" +
        ".773438 28.917968 19.589844 27.363281 37.339844-1.515625 17.339844-15" +
        ".433594 30.621094-32.238281 31.945313l19.652344 23.421874 33.367187 2" +
        ".917969c7.734375.675781 14.558594-5.050781 15.234375-12.785156l7" +
        ".351562-84.050781c.679688-7.734375-5.046874-14.558594-12.78125-15" +
        ".234375l-25.683593-2.246094-2.78125-9.921875c-1.5625-5.632812-6" +
        ".472657-9.710938-12.308594-10.222656zm0 0'/></svg>";
    // @formatter:on

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
