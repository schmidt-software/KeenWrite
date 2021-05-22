/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.keenwrite.ui.adapters.ReplacedElementAdapter;
import com.keenwrite.util.BoundedCache;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.keenwrite.preview.SvgReplacedElementFactory.HTML_IMAGE;
import static com.keenwrite.preview.SvgReplacedElementFactory.HTML_IMAGE_SRC;
import static com.keenwrite.processors.markdown.extensions.tex.TexNode.HTML_TEX;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

/**
 * Responsible for running one or more factories to perform post-processing on
 * the HTML document prior to displaying it.
 */
public final class ChainedReplacedElementFactory
  extends ReplacedElementAdapter {
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
    assert factories != null;
    assert factories.length > 0;
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
        source, k -> {
          final var r = f.createReplacedElement( c, box, uac, width, height );
          return r instanceof final ImageReplacedElement ire
            ? createImageElement( box, ire )
            : r;
        }
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

  /**
   * Creates a new image that maintains its aspect ratio while fitting into
   * the given {@link BlockBox}. If the image is too big, it is scaled down.
   *
   * @param box The bounding region the image must fit into.
   * @param ire The image to resize.
   * @return An image that is scaled down to fit, if needed.
   */
  private SmoothImageReplacedElement createImageElement(
    final BlockBox box, final ImageReplacedElement ire ) {
    return new SmoothImageReplacedElement(
      ire.getImage(), min( ire.getIntrinsicWidth(), box.getWidth() ), -1 );
  }
}
