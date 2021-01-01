/* Copyright 2006 Patrick Wright
 * Copyright 2007 Wisconsin Court System
 * Copyright 2020 White Magic Software, Ltd.
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
 */
package com.keenwrite.preview;

import com.keenwrite.ui.adapters.ReplacedElementAdapter;
import com.keenwrite.util.BoundedCache;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.keenwrite.preview.SvgReplacedElementFactory.HTML_IMAGE;
import static com.keenwrite.preview.SvgReplacedElementFactory.HTML_IMAGE_SRC;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.HTML_TEX;
import static java.util.Arrays.asList;

/**
 * Responsible for running one or more factories to perform post-processing on
 * the HTML document prior to displaying it.
 */
public class ChainedReplacedElementFactory extends ReplacedElementAdapter {
  /**
   * Retain insertion order so that client classes can control the order that
   * factories are used to resolve images.
   */
  private final Set<ReplacedElementFactory> mFactories = new LinkedHashSet<>();

  /**
   * A bounded cache that removes the oldest image if the maximum number of
   * cached images has been reached. This constrains the number of images
   * loaded into memory.
   */
  private final Map<String, ReplacedElement> mCache = new BoundedCache<>( 150 );

  public ChainedReplacedElementFactory(
    final ReplacedElementFactory... factories ) {
    mFactories.addAll( asList( factories ) );
  }

  @Override
  public ReplacedElement createReplacedElement(
    final LayoutContext c,
    final BlockBox box,
    final UserAgentCallback uac,
    final int width,
    final int height ) {
    for( final var f : mFactories ) {
      final var e = box.getElement();

      // Exit early for super-speed.
      if( e == null ) {
        break;
      }

      // If the source image is cached, don't bother fetching. This optimization
      // avoids making multiple HTTP requests for the same URI.
      final var node = e.getNodeName();
      final var source = switch( node ) {
        case HTML_IMAGE -> e.getAttribute( HTML_IMAGE_SRC );
        case HTML_TEX -> e.getTextContent();
        default -> "";
      };

      // HTML <img> or <tex> elements without source data shall not pass.
      if( source.isBlank() ) {
        break;
      }

      final var replaced = mCache.computeIfAbsent(
        source, k -> f.createReplacedElement( c, box, uac, width, height )
      );

      if( replaced != null ) {
        return replaced;
      }
    }

    return null;
  }

  @Override
  public void reset() {
    for( final var factory : mFactories ) {
      factory.reset();
    }
  }

  @Override
  public void remove( final Element element ) {
    for( final var factory : mFactories ) {
      factory.remove( element );
    }
  }

  public void addFactory( final ReplacedElementFactory factory ) {
    mFactories.add( factory );
  }

  public void clearCache() {
    mCache.clear();
  }
}
