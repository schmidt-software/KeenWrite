/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.css.parser.Parser;
import org.apache.batik.gvt.renderer.ImageRenderer;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.css.sac.CSSException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;

import static com.keenwrite.dom.DocumentParser.transform;
import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.preview.HighQualityRenderingHints.RENDERING_HINTS;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.text.NumberFormat.getIntegerInstance;
import static org.apache.batik.bridge.UnitProcessor.createContext;
import static org.apache.batik.bridge.UnitProcessor.svgHorizontalLengthToUserSpace;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.batik.transcoder.TranscodingHints.Key;
import static org.apache.batik.transcoder.image.ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER;
import static org.apache.batik.util.SVGConstants.SVG_WIDTH_ATTRIBUTE;
import static org.apache.batik.util.XMLResourceDescriptor.getXMLParserClassName;

/**
 * Responsible for converting SVG images into rasterized PNG images.
 */
public final class SvgRasterizer {
  /**
   * <a href="https://issues.apache.org/jira/browse/BATIK-1112">Bug fix</a>
   */
  public static final class InkscapeCssParser extends Parser {
    public void parseStyleDeclaration( final String source )
      throws CSSException, IOException {
      super.parseStyleDeclaration(
        source.replaceAll( "-inkscape-font-specification:[^;\"]*;", "" )
      );
    }
  }

  static {
    XMLResourceDescriptor.setCSSParserClassName(
      InkscapeCssParser.class.getName()
    );
  }

  private static final UserAgent USER_AGENT = new UserAgentAdapter();
  private static final BridgeContext BRIDGE_CONTEXT = new BridgeContext(
    USER_AGENT, new DocumentLoader( USER_AGENT )
  );

  private static final SAXSVGDocumentFactory FACTORY_DOM =
    new SAXSVGDocumentFactory( getXMLParserClassName() );

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
   * Rasterizes the given SVG input stream into an image at 96 DPI.
   *
   * @param svg The SVG data to rasterize, must be closed by caller.
   * @return The given input stream converted to a rasterized image.
   */
  public static BufferedImage rasterize( final InputStream svg )
    throws TranscoderException {
    return rasterize( svg, 96 );
  }

  /**
   * Rasterizes the given SVG input stream into an image.
   *
   * @param svg The SVG data to rasterize, must be closed by caller.
   * @param dpi Resolution to use when rasterizing (default is 96 DPI).
   * @return The given input stream converted to a rasterized image at the
   * given resolution.
   */
  public static BufferedImage rasterize(
    final InputStream svg, final float dpi ) throws TranscoderException {
    return rasterize(
      new TranscoderInput( svg ),
      KEY_PIXEL_UNIT_TO_MILLIMETER,
      1f / dpi * 25.4f
    );
  }

  /**
   * Rasterizes the given document into an image.
   *
   * @param svg   The SVG {@link Document} to rasterize.
   * @param width The rasterized image's width (in pixels).
   * @return The rasterized image.
   */
  public static BufferedImage rasterize(
    final Document svg, final int width ) throws TranscoderException {
    return rasterize(
      new TranscoderInput( svg ),
      KEY_WIDTH,
      fit( svg.getDocumentElement(), width )
    );
  }

  /**
   * Rasterizes the given vector graphic file using the width dimension
   * specified by the document's width attribute.
   *
   * @param document The {@link Document} containing a vector graphic.
   * @return A rasterized image as an instance of {@link BufferedImage}, or
   * {@link #BROKEN_IMAGE_PLACEHOLDER} if the graphic could not be rasterized.
   */
  public static BufferedImage rasterize( final Document document )
    throws ParseException, TranscoderException {
    final var root = document.getDocumentElement();
    final var width = root.getAttribute( SVG_WIDTH_ATTRIBUTE );
    return rasterize( document, INT_FORMAT.parse( width ).intValue() );
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
  public static BufferedImage rasterizeString( final String xml )
    throws ParseException, TranscoderException {
    final var document = toDocument( xml );
    final var root = document.getDocumentElement();
    final var width = root.getAttribute( SVG_WIDTH_ATTRIBUTE );
    return rasterizeString( xml, INT_FORMAT.parse( width ).intValue() );
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
  public static BufferedImage rasterizeString( final String svg, final int w )
    throws TranscoderException {
    return rasterize( toDocument( svg ), w );
  }

  /**
   * Given a document object model (DOM) {@link Element}, this will convert that
   * element to a string.
   *
   * @param root The DOM node to convert to a string.
   * @return The DOM node as an escaped, plain text string.
   */
  public static String toSvg( final Element root ) {
    try {
      return transform( root ).replaceAll( "xmlns=\"\" ", "" );
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

  /**
   * Creates a rasterized image of the given source document.
   *
   * @param input The source document to transcode.
   * @param key   Transcoding hint key.
   * @param width Transcoding hint value.
   * @return A new {@link BufferedImageTranscoder} instance with the given
   * transcoding hint applied.
   */
  private static BufferedImage rasterize(
    final TranscoderInput input, final Key key, final float width )
    throws TranscoderException {
    final var transcoder = new BufferedImageTranscoder();

    transcoder.addTranscodingHint( key, width );
    transcoder.transcode( input, null );

    return transcoder.getImage();
  }

  /**
   * Returns either the given element's SVG document width, or the display
   * width, whichever is smaller.
   *
   * @param root  The SVG document's root node.
   * @param width The display width (e.g., rendering canvas width).
   * @return The lower value of the document's width or the display width.
   */
  private static float fit( final Element root, final int width ) {
    final var w = root.getAttribute( SVG_WIDTH_ATTRIBUTE );

    return w == null || w.isBlank() ? width : fit( root, w, width );
  }

  /**
   * Returns the width in user space units (pixels?).
   *
   * @param root  The element containing the width attribute.
   * @param w     The element's width attribute value.
   * @param width The rendering canvas width.
   * @return Either the rendering canvas width or SVG document width,
   * whichever is smaller.
   */
  private static float fit(
    final Element root, final String w, final int width ) {
    final var usWidth = svgHorizontalLengthToUserSpace(
      w, SVG_WIDTH_ATTRIBUTE, createContext( BRIDGE_CONTEXT, root )
    );

    // If the image is too small, scale it to 1/4 the canvas width.
    return Math.min( usWidth < 5 ? width / 4.0f : usWidth, (float) width );
  }
}
