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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.ImageResourceLoader;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.scrivenvar.preview.SVGRasterizer.PLACEHOLDER_IMAGE;
import static org.xhtmlrenderer.swing.AWTFSImage.createImage;

/**
 * Responsible for loading images. If the image cannot be found, a placeholder
 * is used instead.
 */
public class CustomImageResourceLoader extends ImageResourceLoader {
  /**
   * Placeholder that's displayed when image cannot be found.
   */
  private static final FSImage FS_PLACEHOLDER_IMAGE =
      createImage( PLACEHOLDER_IMAGE );

  private final IntegerProperty mMaxWidthProperty = new SimpleIntegerProperty();

  public CustomImageResourceLoader() {
  }

  public IntegerProperty widthProperty() {
    return mMaxWidthProperty;
  }

  @Override
  public synchronized ImageResource get(
      final String uri, final int width, final int height ) {
    assert uri != null;
    assert width >= 0;
    assert height >= 0;

    boolean exists;

    try {
      exists = Files.exists( Paths.get( new URI( uri ) ) );
    } catch( final Exception e ) {
      exists = false;
    }

    return exists
        ? scale( uri, width, height )
        : new ImageResource( uri, FS_PLACEHOLDER_IMAGE );
  }

  /**
   * Scales the image found at the given uri.
   *
   * @param uri Path to the image file to load.
   * @param w   Unused (usually -1, which is useless).
   * @param h   Unused (ditto).
   * @return Resource representing the rendered image and path.
   */
  private ImageResource scale( final String uri, final int w, final int h ) {
    final var ir = super.get( uri, w, h );
    final var image = ir.getImage();
    final var imageWidth = image.getWidth();
    final var imageHeight = image.getHeight();

    int maxWidth = mMaxWidthProperty.get();
    int newWidth = imageWidth;
    int newHeight = imageHeight;

    // Maintain aspect ratio while shrinking image to view port bounds.
    if( imageWidth > maxWidth ) {
      newWidth = maxWidth;
      newHeight = (newWidth * imageHeight) / imageWidth;
    }

    image.scale( newWidth, newHeight );
    return ir;
  }
}
