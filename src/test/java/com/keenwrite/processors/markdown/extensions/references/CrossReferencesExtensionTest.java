/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings( "SpellCheckingInspection" )
public class CrossReferencesExtensionTest {
  @ParameterizedTest
  @MethodSource( "testDocuments" )
  public void test_References_Documents_Html(
    final String input,
    final String expected ) {
    final var extension = new CrossReferenceExtension();
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var extensions = List.of( extension );

    pBuilder.extensions( extensions );
    hBuilder.extensions( extensions );

    final var parser = pBuilder.build();
    final var renderer = hBuilder.build();

    final var document = parser.parse( input );
    final var actual = renderer.render( document );

    assertEquals( expected, actual );
  }

  private static Stream<Arguments> testDocuments() {
    return Stream.of(
      Arguments.of(
        """
          {#fig:cats} [@fig:cats]
          {#table:dogs} [@table:dogs]
          {#life:dolphins} [@life:dolphins]
          """,
        """
          <p><a data-type="fig" name="cats" /> <a data-type="fig" href="#cats" />
          <a data-type="table" name="dogs" /> <a data-type="table" href="#dogs" />
          <a data-type="life" name="dolphins" /> <a data-type="life" href="#dolphins" /></p>
          """ ),
      Arguments.of(
        """
          Lorem ipsum dolor sit amet, consectetur adipiscing elit.
          {#fig:cats} Sed do eiusmod tempor incididunt ut
          labore et dolore magna aliqua. Ut enim ad minim veniam,
          quis nostrud exercitation ullamco laboris nisi ut aliquip
          ex ea commodo consequat. [@fig:cats]
          """,
        """
          <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit.
          <a data-type="fig" name="cats" /> Sed do eiusmod tempor incididunt ut
          labore et dolore magna aliqua. Ut enim ad minim veniam,
          quis nostrud exercitation ullamco laboris nisi ut aliquip
          ex ea commodo consequat. <a data-type="fig" href="#cats" /></p>
          """
      ),
      Arguments.of(
        """
          {#日本:w0mbatß}
          [@日本:w0mbatß]
          [@日本:w0mbatß]
          [@日本:w0mbatß]
          """,
        """
          <p><a data-type="日本" name="w0mbatß" />
          <a data-type="日本" href="#w0mbatß" />
          <a data-type="日本" href="#w0mbatß" />
          <a data-type="日本" href="#w0mbatß" /></p>
          """
      ),
      Arguments.of(
        """
          {#note:advancement}
                  
          Advancement isn't measured by the ingenuity of inventions, but
          by humanity's ability to anticipate and forfend dire aftermaths
          *before* using them.
                  
          [@note:advancement]
                  
          To what end?
          """,
        """
          <p><a data-type="note" name="advancement" /></p>
          <p>Advancement isn't measured by the ingenuity of inventions, but
          by humanity's ability to anticipate and forfend dire aftermaths
          <em>before</em> using them.</p>
          <p><a data-type="note" href="#advancement" /></p>
          <p>To what end?</p>
          """
      )
    );
  }
}
