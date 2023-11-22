/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.flexmark;

import com.keenwrite.processors.markdown.extensions.fences.FencedDivExtension;
import com.keenwrite.processors.markdown.extensions.references.CrossReferenceExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that basic styles for conversion exports as expected.
 */
public class ParserTest {

  @ParameterizedTest
  @MethodSource( "markdownParameters" )
  void test_Conversion_Markdown_Html( final String md, final String expected ) {
    final var extensions = createExtensions();
    final var options = new MutableDataSet();
    final var parser = Parser
      .builder( options )
      .extensions( extensions )
      .build();
    final var renderer = HtmlRenderer
      .builder( options )
      .extensions( extensions )
      .build();

    final var document = parser.parse( md );
    final var actual = renderer.render( document );

    assertEquals( expected, actual );
  }

  private List<Extension> createExtensions() {
    final var extensions = new ArrayList<Extension>();

    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( FencedDivExtension.create() );
    extensions.add( CrossReferenceExtension.create() );

    return extensions;
  }

  private static Stream<Arguments> markdownParameters() {
    return Stream.of(
      Arguments.of(
        "*emphasis* _emphasis_ **strong**",
        "<p><em>emphasis</em> <em>emphasis</em> <strong>strong</strong></p>\n"
      ),
      Arguments.of(
        "the \uD83D\uDC4D emoji",
        "<p>the \uD83D\uDC4D emoji</p>\n"
      )
    );
  }
}
