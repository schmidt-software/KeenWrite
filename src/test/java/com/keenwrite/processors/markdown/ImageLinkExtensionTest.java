/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.AwaitFxExtension;
import com.keenwrite.Caret;
import com.keenwrite.preferences.Workspace;
import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.ImageLinkExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.keenwrite.ExportFormat.NONE;
import static com.keenwrite.constants.Constants.DOCUMENT_DEFAULT;
import static java.lang.String.format;
import static javafx.application.Platform.runLater;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

/**
 * Responsible for testing that linked images render into HTML according to
 * the {@link ImageLinkExtension} rules.
 */
@ExtendWith( {ApplicationExtension.class, AwaitFxExtension.class} )
@SuppressWarnings( "SameParameterValue" )
public class ImageLinkExtensionTest {
  private static final Workspace sWorkspace = new Workspace(
    getResource( "workspace.xml" ) );

  private static final Map<String, String> IMAGES = new HashMap<>();

  private static final String URI_WEB = "placekitten.com/200/200";
  private static final String URI_DIRNAME = "images";
  private static final String URI_FILENAME = "kitten";

  /**
   * Path to use for testing image file name resolution. Note that resources use
   * forward slashes, regardless of OS.
   */
  private static final String URI_PATH = URI_DIRNAME + '/' + URI_FILENAME;

  /**
   * Extension for the first existing image that matches the preferred image
   * extension order.
   */
  private static final String URI_IMAGE_EXT = ".png";

  /**
   * Relative path to an image that exists.
   */
  private static final String URI_IMAGE = URI_PATH + URI_IMAGE_EXT;

  static {
    addUri( URI_PATH + ".png" );
    addUri( URI_PATH + ".jpg" );
    addUri( URI_PATH, getResource( URI_PATH + URI_IMAGE_EXT ) );
    addUri( "//" + URI_WEB );
    addUri( "http://" + URI_WEB );
    addUri( "https://" + URI_WEB );
  }

  private HtmlPreview mPreview;

  @Start
  @SuppressWarnings( "unused" )
  private void start( final Stage stage ) {
    mPreview = new HtmlPreview( sWorkspace );
  }

  private static void addUri( final String actualExpected ) {
    addUri( actualExpected, actualExpected );
  }

  private static void addUri( final String actual, final String expected ) {
    IMAGES.put( toMd( actual ), toHtml( expected ) );
  }

  private static String toMd( final String resource ) {
    return format( "![Tooltip](%s 'Title')", resource );
  }

  private static String toHtml( final String url ) {
    return format(
      "<p><img src=\"%s\" alt=\"Tooltip\" title=\"Title\" /></p>\n", url );
  }

  /**
   * Test that the key URIs present in the {@link #IMAGES} map are rendered
   * as the value URIs present in the same map.
   */
  @Test
  void test_ImageLookup_RelativePathWithExtension_ResolvedSuccessfully() {
    final var resource = getResourcePath( URI_IMAGE );
    final var imagePath = new File( URI_IMAGE ).toPath();
    final var subpaths = resource.getNameCount() - imagePath.getNameCount();
    final var subpath = resource.subpath( 0, subpaths );

    // The root component isn't considered part of the path, so add it back.
    final var documentPath = Path.of(
      resource.getRoot().resolve( subpath ).toString(),
      DOCUMENT_DEFAULT.getName() );
    final var context = createProcessorContext( documentPath );
    final var extension = ImageLinkExtension.create( context );
    final var extensions = List.of( extension );
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var parser = pBuilder.extensions( extensions ).build();
    final var renderer = hBuilder.extensions( extensions ).build();

    assertNotNull( parser );
    assertNotNull( renderer );

    for( final var entry : IMAGES.entrySet() ) {
      final var key = entry.getKey();
      final var node = parser.parse( key );
      final var expectedHtml = entry.getValue();
      final var actualHtml = new StringBuilder( 128 );

      runLater( () -> actualHtml.append( renderer.render( node ) ) );

      waitForFxEvents();
      assertEquals( expectedHtml, actualHtml.toString() );
    }
  }

  /**
   * Creates a new {@link ProcessorContext} for the given file name path.
   *
   * @param documentPath Fully qualified path to the file name.
   * @return A context used for creating new {@link Processor} instances.
   */
  private ProcessorContext createProcessorContext( final Path documentPath ) {
    return new ProcessorContext(
      mPreview,
      new SimpleObjectProperty<>(),
      documentPath,
      null,
      NONE,
      sWorkspace,
      Caret.builder().build()
    );
  }

  private static URL toUrl( final String path ) {
    final var clazz = ImageLinkExtensionTest.class;
    final var packagePath = clazz.getPackageName().replace( '.', '/' );
    final var resourcePath = '/' + packagePath + '/' + path;
    return clazz.getResource( resourcePath );
  }

  private static URI toUri( final String path ) {
    try {
      return toUrl( path ).toURI();
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  private static Path getResourcePath( final String path ) {
    return Paths.get( toUri( path ) );
  }

  private static String getResource( final String path ) {
    return toUri( path ).toString();
  }
}
