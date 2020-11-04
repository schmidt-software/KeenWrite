package com.keenwrite.processors.markdown;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

import java.nio.file.Path;
import java.util.List;

@ExtendWith(ApplicationExtension.class)
public class ImageLinkExtensionTest {

  /**
   * Test that
   * {@code ![Tooltip](images/filename.svg 'Title')}
   * will produce
   * {@code <img src="images/filename.svg" alt="Tooltip" title="Title" />}
   */
  @Test
  void test_LocalImage_RelativePathWithExtension_ResolvedSuccessfully() {
    final var path = Path.of( "." );
    final var extension = ImageLinkExtension.create( path );
    final var extensions = List.of( extension);
    final var parser = Parser.builder().extensions( extensions ).build();
    final var renderer = HtmlRenderer.builder().extensions( extensions ).build();
    final var node = parser.parse( "![Tooltip](images/filename.svg 'Title')");
    final var html = renderer.render(node);

    System.out.println( html );
  }
}
