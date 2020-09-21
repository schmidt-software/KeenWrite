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
package com.keenwrite.preview;

import com.keenwrite.exceptions.MissingFileException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.ImageResourceLoader;

import javax.imageio.ImageIO;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import static com.keenwrite.StatusBarNotifier.clue;
import static com.keenwrite.preview.SvgRasterizer.BROKEN_IMAGE_PLACEHOLDER;
import static com.keenwrite.util.ProtocolResolver.getProtocol;
import static java.lang.String.valueOf;
import static java.nio.file.Files.exists;
import static org.xhtmlrenderer.swing.AWTFSImage.createImage;

/**
 * Responsible for loading images. If the image cannot be found, a placeholder
 * is used instead.
 */
public class CustomImageLoader extends ImageResourceLoader {
  /**
   * Placeholder that's displayed when image cannot be found.
   */
  private FSImage mBrokenImage;

  private final IntegerProperty mWidthProperty = new SimpleIntegerProperty();

  /**
   * Gets an {@link IntegerProperty} that represents the maximum width an
   * image should be scaled.
   *
   * @return The maximum width for an image.
   */
  public IntegerProperty widthProperty() {
    return mWidthProperty;
  }

  /**
   * Gets an image resolved from the given URI. If the image cannot be found,
   * this will return a custom placeholder image indicating the reference
   * is broken.
   *
   * @param uri    Path to the image resource to load.
   * @param width  Ignored.
   * @param height Ignored.
   * @return The scaled image, or a placeholder image if the URI's content
   * could not be retrieved.
   */
  @Override
  public synchronized ImageResource get(
      final String uri, final int width, final int height ) {
    assert uri != null;
    assert width >= 0;
    assert height >= 0;

    try {
      final var protocol = getProtocol( uri );
      final ImageResource imageResource;

      if( protocol.isFile() ) {
        if( exists( Paths.get( new URI( uri ) ) ) ) {
          imageResource = super.get( uri, width, height );
        }
        else {
          throw new MissingFileException( uri );
        }
      }
      else if( protocol.isHttp() ) {
        // FlyingSaucer will silently swallow any images that fail to load.
        // Consequently, the following lines load the resource over HTTP and
        // translate errors into a broken image icon.
        final var url = new URL( uri );
        final var image = ImageIO.read( url );
        imageResource = new ImageResource( uri, createImage( image ) );
      }
      else {
        // Caught below to return a broken image; exception is swallowed.
        throw new UnsupportedOperationException( valueOf( protocol ) );
      }

      return scale( imageResource );
    } catch( final Exception e ) {
      clue( e );
      return new ImageResource( uri, getBrokenImage() );
    }
  }

  /**
   * Scales the image found at the given URI.
   *
   * @param ir {@link ImageResource} of image loaded successfully.
   * @return Resource representing the rendered image and path.
   */
  private ImageResource scale( final ImageResource ir ) {
    final var image = ir.getImage();
    final var imageWidth = image.getWidth();
    final var imageHeight = image.getHeight();

    int maxWidth = mWidthProperty.get();
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

  /**
   * Lazily initializes the broken image placeholder.
   *
   * @return The {@link FSImage} that represents a broken image icon.
   */
  private FSImage getBrokenImage() {
    final var image = mBrokenImage;

    if( image == null ) {
      mBrokenImage = createImage( BROKEN_IMAGE_PLACEHOLDER );
    }

    return mBrokenImage;
  }
}
