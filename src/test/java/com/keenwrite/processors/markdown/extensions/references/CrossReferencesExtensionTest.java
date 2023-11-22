/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.fences.FencedDivExtension;
import com.keenwrite.processors.markdown.extensions.tex.TexExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.Parser.ParserExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.keenwrite.ExportFormat.XHTML_TEX;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings( "SpellCheckingInspection" )
public class CrossReferencesExtensionTest {
  @ParameterizedTest
  @MethodSource( "testDocuments" )
  public void test_References_Documents_Html(
    final String input, final String expected
  ) {
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var extensions = createExtensions();

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
      args(
        """
          {#fig:cats} [@fig:cats]
          {#table:dogs} [@table:dogs]
          {#life:dolphins} [@life:dolphins]
          """,
        """
          <p><a data-type="fig" name="cats" /> <a data-type="fig" href="#cats" />
          <a data-type="table" name="dogs" /> <a data-type="table" href="#dogs" />
          <a data-type="life" name="dolphins" /> <a data-type="life" href="#dolphins" /></p>
          """
      ),
      args(
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
      args(
        """
          {#日本:w0mbatß}
          [@日本:w0mbatß]
          """,
        """
          <p><a data-type="日本" name="w0mbatß" />
          <a data-type="日本" href="#w0mbatß" /></p>
          """
      ),
      args(
        """
          {#note:advancement} Advancement isn't
          measured by the ingenuity of inventions, but by humanity's ability
          to anticipate and forfend dire aftermaths *before* using them.

          [@note:advancement]

          To what end?
          """,
        """
          <p><a data-type="note" name="advancement" /> Advancement isn't
          measured by the ingenuity of inventions, but by humanity's ability
          to anticipate and forfend dire aftermaths <em>before</em> using them.</p>
          <p><a data-type="note" href="#advancement" /></p>
          <p>To what end?</p>
          """
      ),
      args(
        """
          ![alt text](tunnel)

          : Caption {#fig:label}
          """,
        """
          <p><img src="tunnel" alt="alt text" /><a data-type="fig" name="label" /></p>
          """
      ),
      args(
        """
          $E=mc^2$ {#eq:label}
          """,
        """
          <p><tex>$E=mc^2$</tex> <a data-type="eq" name="label" /></p>
          """
      ),
      args(
        """
          $$E=mc^2$$ {#eq:label}
          """,
        """
          <p><tex>$$E=mc^2$$</tex> <a data-type="eq" name="label" /></p>
          """
      ),
      args(
        """
          $$E=mc^2$$

          : Caption {#eqn:energy}
          """,
        """
          <p><tex>$$E=mc^2$$</tex> <a data-type="eqn" name="energy" /></p>
          """
      ),
      args(
        """
          ``` haskell
          main :: IO ()
          ```

          : Source code caption {#listing:haskell1}
          """,
        """
          <pre><code class="language-haskell">main :: IO ()
          </code></pre>
          <p>: Source code caption <a data-type="listing" name="haskell1" /></p>
          """
      ),
      args(
        """
          > I'd like to be the lucky devil who gets to burn with you.
          >
          > Well, I'm no angel, my wings have been clipped;
          >
          > I've traded my halo for horns and a whip.

          : Meschiya Lake - Lucky Devil {#lyrics:blues}
          """,
        """
          <blockquote>
          <p>I'd like to be the lucky devil who gets to burn with you.</p>
          <p>Well, I'm no angel, my wings have been clipped;</p>
          <p>I've traded my halo for horns and a whip.</p>
          </blockquote>
          <p>: Meschiya Lake - Lucky Devil <a data-type="lyrics" name="blues" /></p>
          """
      ),
      args(
        """
          | a | b | c |
          |---|---|---|
          | 1 | 2 | 3 |
          | 4 | 5 | 6 |

          : Caption {#tbl:label}
          """,
        """
          <table>
          <thead>
          <tr><th>a</th><th>b</th><th>c</th></tr>
          </thead>
          <tbody>
          <tr><td>1</td><td>2</td><td>3</td></tr>
          <tr><td>4</td><td>5</td><td>6</td></tr>
          </tbody>
          </table>
          <dd>
          Caption <a data-type="tbl" name="label" />
          </dd>
          """
      ),
      args(
        """
          This is a paragraph of text.

          : Defintion list, not a caption. {@note:advancement}
          """,
        """
          <dl>
          <dt>This is a paragraph of text.</dt>
          <dd>
          <p>Caption title. <a data-type="null" name="null" /></p>
          </dd>
          </dl>
          """
      )
    );
  }

  private static Arguments args( final String in, final String out ) {
    return Arguments.of( in, out );
  }

  private List<ParserExtension> createExtensions() {
    final var extensions = new LinkedList<ParserExtension>();
    final var context = ProcessorContext
      .builder()
      .with( ProcessorContext.Mutator::setExportFormat, XHTML_TEX )
      .build();

    extensions.add( TexExtension.create( s -> s, context ) );
    extensions.add( CrossReferenceExtension.create() );
    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( FencedDivExtension.create() );

    return extensions;
  }
}
