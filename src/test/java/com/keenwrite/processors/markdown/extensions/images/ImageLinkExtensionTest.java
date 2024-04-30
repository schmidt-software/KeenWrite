/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.images;

import com.keenwrite.editors.common.Caret;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.keenwrite.ExportFormat.XHTML_TEX;
import static com.keenwrite.constants.Constants.DOCUMENT_DEFAULT;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Responsible for testing that linked images render into HTML according to
 * the {@link ImageLinkExtension} rules.
 */
@SuppressWarnings( "SameParameterValue" )
public class ImageLinkExtensionTest {
  private static final String UIR_DIR = "images";
  private static final String URI_FILE = "kitten";
  private static final String URI_PATH = UIR_DIR + '/' + URI_FILE;
  private static final String PATH_KITTEN_PNG = STR."\{URI_PATH}.png";
  private static final String PATH_KITTEN_JPG = STR."\{URI_PATH}.jpg";

  /** Web server that doles out images. */
  private static final String PLACEHOLDER = "loremflickr.com";

  private static final Map<String, String> IMAGES = new LinkedHashMap<>();

  static {
    add( PATH_KITTEN_PNG, URI_PATH );
    add( PATH_KITTEN_PNG, URI_FILE );
    add( PATH_KITTEN_PNG, PATH_KITTEN_PNG );
    add( PATH_KITTEN_JPG, PATH_KITTEN_JPG );
    add( STR."//\{PLACEHOLDER}/200/200", STR."//\{PLACEHOLDER}/200/200" );
    add( STR."ftp://\{PLACEHOLDER}/200/200",  STR."ftp://\{PLACEHOLDER}/200/200" );
    add( STR."http://\{PLACEHOLDER}/200/200", STR."http://\{PLACEHOLDER}/200/200" );
    add( STR."https://\{PLACEHOLDER}/200/200", STR."https://\{PLACEHOLDER}/200/200" );
  }

  private static void add( final String expected, final String actual ) {
    IMAGES.put( toMd( actual ), toHtml( expected ) );
  }

  private static String toMd( final String resource ) {
    return format( "![Tooltip](%s 'Title')", resource );
  }

  private static String toHtml( final String url ) {
    return format(
      "<p><img src=\"%s\" alt=\"Tooltip\" title=\"Title\" /></p>%n", url );
  }

  /**
   * Test that the key URIs present in the {@link #IMAGES} map are rendered
   * as the value URIs present in the same map.
   */
  @Test
  void test_ImageLookup_RelativePathWithExtension_ResolvedSuccessfully()
    throws IOException {
    final var resource = getResourcePath( PATH_KITTEN_PNG );
    final var imagePath = new File( PATH_KITTEN_PNG ).toPath();
    final var subpaths = resource.getNameCount() - imagePath.getNameCount();
    final var subpath = resource.subpath( 0, subpaths );

    final var root = resource.getRoot();
    assertNotNull( root );

    final var resolved = root.resolve( subpath );
    final var doc = resolved.toString();

    // The root component isn't considered part of the path, so add it back.
    final var documentPath = Path.of( doc, DOCUMENT_DEFAULT.getName() );
    final var imagesDir = Path.of( "images" );
    final var context = createProcessorContext( documentPath, imagesDir );
    final var extension = ImageLinkExtension.create( context );
    final var extensions = List.of( extension );
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var parser = pBuilder.extensions( extensions ).build();
    final var renderer = hBuilder.extensions( extensions ).build();
    final var imageFile = resolved.resolve( imagePath ).toFile();
    imageFile.deleteOnExit();

    assertTrue( imageFile.createNewFile() );
    assertNotNull( parser );
    assertNotNull( renderer );

    for( final var entry : IMAGES.entrySet() ) {
      final var key = entry.getKey();
      final var node = parser.parse( key );
      final var expectedHtml = entry.getValue();
      final var actualHtml = renderer.render( node );

      assertEquals( expectedHtml, actualHtml );
    }

    assertTrue( imageFile.delete() );
  }

  /**
   * Creates a new {@link ProcessorContext} for the given file name path.
   *
   * @param inputPath Fully qualified path to the file name.
   * @return A context used for creating new {@link Processor} instances.
   */
  private ProcessorContext createProcessorContext(
    final Path inputPath, final Path imagesDir ) {
    return ProcessorContext
      .builder()
      .with( ProcessorContext.Mutator::setSourcePath, inputPath )
      .with( ProcessorContext.Mutator::setExportFormat, XHTML_TEX )
      .with( ProcessorContext.Mutator::setCaret, () -> Caret.builder().build() )
      .with( ProcessorContext.Mutator::setImageDir, imagesDir::toFile )
      .build();
  }

  private static URI toUri( final String path ) {
    try {
      return Path.of( path ).toUri();
    } catch( final Exception ex ) {
      throw new RuntimeException( ex );
    }
  }

  private static Path getResourcePath( final String path ) {
    return Paths.get( toUri( path ) );
  }
}
