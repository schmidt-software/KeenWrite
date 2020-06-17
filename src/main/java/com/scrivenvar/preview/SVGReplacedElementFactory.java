/*
 * {{{ header & license
 * Copyright (c) 2006 Patrick Wright
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.scrivenvar.preview;

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

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static com.scrivenvar.util.ProtocolResolver.getProtocol;

public class SVGReplacedElementFactory
    implements ReplacedElementFactory {

  private final static Notifier sNotifier = Services.load( Notifier.class );

  /**
   * SVG filename extension.
   */
  private static final String SVG_FILE = "svg";
  private static final String HTML_IMAGE = "img";
  private static final String HTML_IMAGE_SRC = "src";

  public ReplacedElement createReplacedElement(
      LayoutContext c,
      BlockBox box,
      UserAgentCallback uac,
      int cssWidth,
      int cssHeight ) {

    final Element e = box.getElement();

    if( e == null ) {
      return null;
    }

    final String nodeName = e.getNodeName();
    ReplacedElement result = null;

    if( HTML_IMAGE.equals( nodeName ) ) {
      final String src = e.getAttribute( HTML_IMAGE_SRC );
      final String ext = FilenameUtils.getExtension( src );

      if( SVG_FILE.equalsIgnoreCase( ext ) ) {
        try {
          final URL url = getUrl( src );
          final int width = box.getContentWidth();
          final Image image = SVGRasterizer.rasterize( url, width );

          final int w = image.getWidth( null );
          final int h = image.getHeight( null );

          result = new ImageReplacedElement( image, w, h );
        } catch( final Exception ex ) {
          getNotifier().notify( ex );
        }
      }
    }

    return result;
  }

  @Override
  public void reset() {
  }

  @Override
  public void remove( Element e ) {
  }

  @Override
  public void setFormSubmissionListener( FormSubmissionListener listener ) {
  }

  private URL getUrl( final String src ) throws MalformedURLException {
    return "file".equals( getProtocol( src ) )
        ? new File( src ).toURI().toURL()
        : new URL( src );
  }

  private Notifier getNotifier() {
    return sNotifier;
  }
}
