/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.keenwrite.preferences.UserPreferences.getInstance;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Responsible for testing that linked images render into HTML according to
 * the {@link ImageLinkExtension} rules.
 */
@ExtendWith(ApplicationExtension.class)
@SuppressWarnings("SameParameterValue")
public class ImageLinkExtensionTest {

  private final static Map<String, String> IMAGES = new HashMap<>();

  private final static String URI_WEB = "placekitten.com/200/200";
  private final static String URI_DIRNAME = "images";
  private final static String URI_FILENAME = "kitten";

  /**
   * Path to use for testing image filename resolution. Note that resources use
   * forward slashes, regardless of OS.
   */
  private final static String URI_PATH = URI_DIRNAME + '/' + URI_FILENAME;

  /**
   * Extension for the first existing image that matches the preferred image
   * extension order.
   */
  private final static String URI_IMAGE_EXT = ".png";

  /**
   * Relative path to an image that exists.
   */
  private final static String URI_IMAGE = URI_PATH + URI_IMAGE_EXT;

  static {
    addUri( URI_PATH + ".png" );
    addUri( URI_PATH + ".jpg" );
    addUri( URI_PATH, URI_PATH + URI_IMAGE_EXT );
    addUri( "//" + URI_WEB );
    addUri( "http://" + URI_WEB );
    addUri( "https://" + URI_WEB );
  }

  private static void addUri( final String uri ) {
    addUri( uri, uri );
  }

  private static void addUri( final String uriKey, final String uriValue ) {
    IMAGES.put( toMd( uriKey ), toHtml( uriValue ) );
  }

  private static String toMd( final String file ) {
    return format( "![Tooltip](%s 'Title')", file );
  }

  private static String toHtml( final String file ) {
    return format(
        "<p><img src=\"%s\" alt=\"Tooltip\" title=\"Title\" /></p>\n", file );
  }

  /**
   * Test that the key URIs present in the {@link #IMAGES} map are rendered
   * as the value URIs present in the same map.
   */
  @Test
  void test_LocalImage_RelativePathWithExtension_ResolvedSuccessfully()
      throws URISyntaxException {
    final var resource = getPathResource( URI_IMAGE );
    final var imagePath = new File( URI_IMAGE ).toPath();
    final var subpaths = resource.getNameCount() - imagePath.getNameCount();
    final var subpath = resource.subpath( 0, subpaths );

    // The root component isn't considered part of the path, so add it back.
    final var path = resource.getRoot().resolve( subpath );

    final var extension = ImageLinkExtension.create( path );
    final var extensions = List.of( extension );
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var parser = pBuilder.extensions( extensions ).build();
    final var renderer = hBuilder.extensions( extensions ).build();

    // Set a default (fallback) image directory search location.
    getInstance().imagesDirectoryProperty().setValue( new File( "." ) );

    for( final var entry : IMAGES.entrySet() ) {
      final var key = entry.getKey();
      final var node = parser.parse( key );
      final var expectedHtml = entry.getValue();
      final var actualHtml = renderer.render( node );

      assertEquals( expectedHtml, actualHtml );
    }
  }

  private Path getPathResource( final String path )
      throws URISyntaxException {
    final var url = getResource( path );
    assert url != null;

    final var uri = url.toURI();
    return Paths.get( uri );
  }

  private URL getResource( final String path ) {
    final var packagePath = getClass().getPackageName().replace( '.', '/' );
    final var resourcePath = '/' + packagePath + '/' + path;
    return getClass().getResource( resourcePath );
  }
}
