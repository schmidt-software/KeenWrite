/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Path;
import java.text.NumberFormat;

import static com.keenwrite.StatusNotifier.clue;
import static com.keenwrite.preview.RenderingSettings.RENDERING_HINTS;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.NumberFormat.getIntegerInstance;
import static javax.xml.transform.OutputKeys.*;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName;

/**
 * Responsible for converting SVG images into rasterized PNG images.
 */
public final class SvgRasterizer {
  private static final SAXSVGDocumentFactory FACTORY_DOM =
      new SAXSVGDocumentFactory( getXMLParserClassName() );

  private static final TransformerFactory FACTORY_TRANSFORM =
      TransformerFactory.newInstance();

  private static final Transformer sTransformer;

  static {
    Transformer t;

    try {
      t = FACTORY_TRANSFORM.newTransformer();
      t.setOutputProperty( OMIT_XML_DECLARATION, "yes" );
      t.setOutputProperty( METHOD, "xml" );
      t.setOutputProperty( INDENT, "no" );
      t.setOutputProperty( ENCODING, UTF_8.name() );
    } catch( final TransformerConfigurationException e ) {
      t = null;
    }

    sTransformer = t;
  }

  private static final NumberFormat INT_FORMAT = getIntegerInstance();

  public static final BufferedImage BROKEN_IMAGE_PLACEHOLDER;

  /**
   * A FontAwesome camera icon, cleft asunder.
   */
  public static final String BROKEN_IMAGE_SVG =
      "<svg height='19pt' viewBox='0 0 25 19' width='25pt' xmlns='http://www" +
          ".w3.org/2000/svg'><g fill='#454545'><path d='m8.042969 11.085938c" +
          ".332031 1.445312 1.660156 2.503906 3.214843 2.558593zm0 0'/><path " +
          "d='m6.792969 9.621094-.300781.226562.242187.195313c.015625-.144531" +
          ".03125-.28125.058594-.421875zm0 0'/><path d='m10.597656.949219-2" +
          ".511718.207031c-.777344.066406-1.429688.582031-1.636719 1.292969l-" +
          ".367188 1.253906-3.414062.28125c-1.027344.085937-1.792969.949219-1" +
          ".699219 1.925781l.976562 10.621094c.089844.976562.996094 1.699219 " +
          "2.023438 1.613281l11.710938-.972656-3.117188-2.484375c-.246094" +
          ".0625-.5.109375-.765625.132812-2.566406.210938-4.835937-1.597656-5" +
          ".0625-4.039062-.023437-.25-.019531-.496094 0-.738281l-.242187-" +
          ".195313.300781-.226562c.359375-1.929688 2.039062-3.472656 4" +
          ".191406-3.652344.207031-.015625.414063-.015625.617187-.007812l" +
          ".933594-.707032zm0 0'/><path d='m10.234375 11.070312 2.964844 2" +
          ".820313c.144531.015625.285156.027344.433593.027344 1.890626 0 3" +
          ".429688-1.460938 3.429688-3.257813 0-1.792968-1.539062-3.257812-3" +
          ".429688-3.257812-1.890624 0-3.429687 1.464844-3.429687 3.257812 0 " +
          ".140625.011719.277344.03125.410156zm0 0'/><path d='m14.488281" +
          ".808594 1.117188 4.554687-1.042969.546875c2.25.476563 3.84375 2" +
          ".472656 3.636719 4.714844-.199219 2.191406-2.050781 3.871094-4" +
          ".285157 4.039062l2.609376 2.957032 4.4375.371094c1.03125.085937 1" +
          ".9375-.640626 2.027343-1.617188l.976563-10.617188c.089844-.980468-" +
          ".667969-1.839843-1.699219-1.925781l-3.414063-.285156-.371093-1" +
          ".253906c-.207031-.710938-.859375-1.226563-1.636719-1.289063zm0 " +
          "0'/></g></svg>";

  static {
    // The width and height cannot be embedded in the SVG above because the
    // path element values are relative to the viewBox dimensions.
    final int w = 75;
    final int h = 75;
    BufferedImage image;

    try {
      image = rasterizeString( BROKEN_IMAGE_SVG, w );
    } catch( final Exception ex ) {
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

  /**
   * Responsible for creating a new {@link ImageRenderer} implementation that
   * can render a DOM as an SVG image.
   */
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

    public BufferedImage getImage() {
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
   * Rasterizes the given document into an image.
   *
   * @param svg   The SVG {@link Document} to rasterize.
   * @param width The rasterized image's width (in pixels).
   * @return The rasterized image.
   */
  public static BufferedImage rasterize( final Document svg, final int width ) {
    try {
      final var transcoder = new BufferedImageTranscoder();
      final var input = new TranscoderInput( svg );

      transcoder.addTranscodingHint( KEY_WIDTH, (float) width );
      transcoder.transcode( input, null );
      return transcoder.getImage();
    } catch( final Exception ex ) {
      clue( ex );
    }

    return BROKEN_IMAGE_PLACEHOLDER;
  }

  public static BufferedImage rasterize( final Document document ) {
    try {
      final var root = document.getDocumentElement();
      final var width = root.getAttribute( "width" );
      return rasterize( document, INT_FORMAT.parse( width ).intValue() );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return BROKEN_IMAGE_PLACEHOLDER;
  }

  /**
   * Rasterizes the vector graphic file at the given URI. If any exception
   * happens, a broken image icon is returned instead.
   *
   * @param path  The {@link Path} to a vector graphic file.
   * @param width Scale the image to the given width (px); aspect ratio is
   *              maintained.
   * @return A rasterized image as an instance of {@link BufferedImage}.
   */
  public static BufferedImage rasterize( final Path path, final int width ) {
    return rasterize( path.toUri(), width );
  }

  /**
   * Rasterizes the vector graphic file at the given URI. If any exception
   * happens, a broken image icon is returned instead.
   *
   * @param uri   The URI to a vector graphic file, which must include the
   *              protocol scheme (such as file:// or https://).
   * @param width Scale the image to the given width (px); aspect ratio is
   *              maintained.
   * @return A rasterized image as an instance of {@link BufferedImage}.
   */
  public static BufferedImage rasterize( final String uri, final int width ) {
    return rasterize( new File( uri ).toURI(), width );
  }

  /**
   * Converts an SVG drawing into a rasterized image that can be drawn on
   * a graphics context.
   *
   * @param uri   The path to the image (can be web address).
   * @param width Scale the image to the given width (px); aspect ratio is
   *              maintained.
   * @return The vector graphic transcoded into a raster image format.
   */
  public static BufferedImage rasterize( final URI uri, final int width ) {
    try {
      return rasterize( FACTORY_DOM.createDocument( uri.toString() ), width );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return BROKEN_IMAGE_PLACEHOLDER;
  }

  /**
   * Converts an SVG string into a rasterized image that can be drawn on
   * a graphics context. The dimensions are determined from the document.
   *
   * @param xml The SVG xml document.
   * @return The vector graphic transcoded into a raster image format.
   */
  public static BufferedImage rasterizeString( final String xml ) {
    try {
      final var document = toDocument( xml );
      final var root = document.getDocumentElement();
      final var width = root.getAttribute( "width" );
      return rasterizeString( xml, INT_FORMAT.parse( width ).intValue() );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return BROKEN_IMAGE_PLACEHOLDER;
  }

  /**
   * Converts an SVG string into a rasterized image that can be drawn on
   * a graphics context.
   *
   * @param svg The SVG xml document.
   * @param w   Scale the image width to this size (aspect ratio is
   *            maintained).
   * @return The vector graphic transcoded into a raster image format.
   */
  public static BufferedImage rasterizeString( final String svg, final int w ) {
    return rasterize( toDocument( svg ), w );
  }

  /**
   * Given a document object model (DOM) {@link Element}, this will convert that
   * element to a string.
   *
   * @param e The DOM node to convert to a string.
   * @return The DOM node as an escaped, plain text string.
   */
  public static String toSvg( final Element e ) {
    try( final var writer = new StringWriter() ) {
      sTransformer.transform( new DOMSource( e ), new StreamResult( writer ) );
      return writer.toString().replaceAll( "xmlns=\"\" ", "" );
    } catch( final Exception ex ) {
      clue( ex );
    }

    return BROKEN_IMAGE_SVG;
  }

  /**
   * Converts an SVG XML string into a new {@link Document} instance.
   *
   * @param xml The XML containing SVG elements.
   * @return The SVG contents parsed into a {@link Document} object model.
   */
  private static Document toDocument( final String xml ) {
    try( final var reader = new StringReader( xml ) ) {
      return FACTORY_DOM.createSVGDocument(
          "http://www.w3.org/2000/svg", reader );
    } catch( final Exception ex ) {
      throw new IllegalArgumentException( ex );
    }
  }
}
