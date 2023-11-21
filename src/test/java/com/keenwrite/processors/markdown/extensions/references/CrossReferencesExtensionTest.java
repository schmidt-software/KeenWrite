/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CrossReferencesExtensionTest {
  private Parser mParser;
  private HtmlRenderer mRenderer;

  @BeforeEach
  public void setup() {
    final var extension = new CrossReferenceExtension();
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var extensions = List.of( extension );

    pBuilder.extensions( extensions );
    hBuilder.extensions( extensions );

    mParser = pBuilder.build();
    mRenderer = hBuilder.build();
  }

  @Test
  public void test_References_SingularReferences_ReferencesAndPointers() {
    final var document = mParser.parse(
      """
        {#fig:cats} [@fig:cats]
        {#table:dogs} [@table:dogs]
        {#life:dolphins} [@life:dolphins]
        """
    );

    final String expected =
      """
        <p><a data-type="fig" name="cats" /> <a data-type="fig" href="#cats" />
        <a data-type="table" name="dogs" /> <a data-type="table" href="#dogs" />
        <a data-type="life" name="dolphins" /> <a data-type="life" href="#dolphins" /></p>
        """;
    final var actual = mRenderer.render( document );

    assertEquals( expected, actual );
  }

  @Test
  @SuppressWarnings( "SpellCheckingInspection" )
  public void test_References_DocumentReferences_ReferencesAndPointers() {
    final var document = mParser.parse(
      """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        {#fig:cats} Sed do eiusmod tempor incididunt ut
        labore et dolore magna aliqua. Ut enim ad minim veniam,
        quis nostrud exercitation ullamco laboris nisi ut aliquip
        ex ea commodo consequat. [@fig:cats]
        """
    );

    final var expected =
      """
        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.
        <a data-type="fig" name="cats" /> Sed do eiusmod tempor incididunt ut
        labore et dolore magna aliqua. Ut enim ad minim veniam,
        quis nostrud exercitation ullamco laboris nisi ut aliquip
        ex ea commodo consequat. <a data-type="fig" href="#cats" /></p>
        """;

    final var actual = mRenderer.render( document );

    assertEquals( expected, actual );
  }
}
