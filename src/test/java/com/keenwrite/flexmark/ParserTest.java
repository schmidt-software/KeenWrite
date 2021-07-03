/* Copyright 2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.flexmark;

import com.keenwrite.processors.markdown.extensions.fences.FencedDivExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that basic styles for conversion exports as expected.
 */
public class ParserTest {

  @Test
  void test_Conversion_InlineStyles_ExportedAsHtml() {
    final var md = "*emphasis* _emphasis_ **strong**";

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
    final var expected =
      "<p><em>emphasis</em> <em>emphasis</em> <strong>strong</strong></p>\n";

    assertEquals( expected, actual );
  }

  private List<Extension> createExtensions() {
    final var extensions = new ArrayList<Extension>();

    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( FencedDivExtension.create() );

    return extensions;
  }
}
