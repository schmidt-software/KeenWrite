/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.keenwrite.processors.markdown.extensions.captions.CaptionExtension;
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
import static com.keenwrite.processors.ProcessorContext.Mutator;
import static com.keenwrite.processors.ProcessorContext.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings( "SpellCheckingInspection" )
public class CaptionsAndCrossReferencesExtensionTest {
  @ParameterizedTest
  @MethodSource( "testDocuments" )
  public void test_References_Documents_Html(
    final String input, final String expected
  ) {
    final var pBuilder = Parser.builder();
    final var hBuilder = HtmlRenderer.builder();
    final var extensions = createExtensions();
    final var parser = pBuilder.extensions( extensions ).build();
    final var renderer = hBuilder.extensions( extensions ).build();

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
          {#ocean:whale-01} [@ocean:whale-02]
          """,
        """
          <p><a class="name" data-type="fig" name="cats" /> <a class="href" data-type="fig" href="#cats" />
          <a class="name" data-type="table" name="dogs" /> <a class="href" data-type="table" href="#dogs" />
          <a class="name" data-type="ocean" name="whale-01" /> <a class="href" data-type="ocean" href="#whale-02" /></p>
          """
      ),
      args(
        """
          {#日本:w0mbatß}
          [@日本:w0mbatß]
          """,
        """
          <p><a class="name" data-type="日本" name="w0mbatß" />
          <a class="href" data-type="日本" href="#w0mbatß" /></p>
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
          <a class="name" data-type="fig" name="cats" /> Sed do eiusmod tempor incididunt ut
          labore et dolore magna aliqua. Ut enim ad minim veniam,
          quis nostrud exercitation ullamco laboris nisi ut aliquip
          ex ea commodo consequat. <a class="href" data-type="fig" href="#cats" /></p>
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
          <p><a class="name" data-type="note" name="advancement" /> Advancement isn't
          measured by the ingenuity of inventions, but by humanity's ability
          to anticipate and forfend dire aftermaths <em>before</em> using them.</p>
          <p><a class="href" data-type="note" href="#advancement" /></p>
          <p>To what end?</p>
          """
      ),
      args(
        """
          $E=mc^2$ {#eq:label}
          """,
        """
          <p><tex>$E=mc^2$</tex> <a class="name" data-type="eq" name="label" /></p>
          """
      ),
      args(
        """
          $$E=mc^2$$ {#eq:label}
          """,
        """
          <p><tex>$$E=mc^2$$</tex> <a class="name" data-type="eq" name="label" /></p>
          """
      ),
      args(
        """
          $$E=mc^2$$

          :: Caption {#eqn:energy}
          """,
        """
          <p><span class="caption">Caption </span><a class="name" data-type="eqn" name="energy" /></p>
          <p><tex>$$E=mc^2$$</tex></p>
          """
      ),
      args(
        """
          ``` haskell
          main :: IO ()
          ```

          :: Source code caption {#listing:haskell1}
          """,
        """
          <p><span class="caption">Source code caption </span><a class="name" data-type="listing" name="haskell1" /></p>
          <pre><code class="language-haskell">main :: IO ()
          </code></pre>
          """
      ),
      args(
        """
          ::: warning
          Do not eat processed **sugar**.

          Seriously.
          :::

          :: Caption {#warning:sugar}
          """,
        """
          <p><span class="caption">Caption </span><a class="name" data-type="warning" name="sugar" /></p><div class="warning">
          <p>Do not eat processed <strong>sugar</strong>.</p>
          <p>Seriously.</p>
          </div>
          """
      ),
      args(
        """
          ![alt text](tunnel)

          :: Caption {#fig:label}
          """,
        """
          <p><span class="caption">Caption </span><a class="name" data-type="fig" name="label" /></p>
          <p><img src="tunnel" alt="alt text" /></p>
          """
      ),
      args(
        """
          ![kitteh](kitten)

          :: Caption **bold** {#fig:label} *italics*
          """,
        """
          <p><span class="caption">Caption <strong>bold</strong>  <em>italics</em></span><a class="name" data-type="fig" name="label" /></p>
          <p><img src="kitten" alt="kitteh" /></p>
          """
      ),
      args(
        """
          > I'd like to be the lucky devil who gets to burn with you.
          >
          > Well, I'm no angel, my wings have been clipped;
          >
          > I've traded my halo for horns and a whip.

          :: Meschiya Lake - Lucky Devil {#lyrics:blues}
          """,
        """
          <p><span class="caption">Meschiya Lake - Lucky Devil </span><a class="name" data-type="lyrics" name="blues" /></p>
          <blockquote>
          <p>I'd like to be the lucky devil who gets to burn with you.</p>
          <p>Well, I'm no angel, my wings have been clipped;</p>
          <p>I've traded my halo for horns and a whip.</p>
          </blockquote>
          """
      ),
      args(
        """
          | a | b | c |
          |---|---|---|
          | 1 | 2 | 3 |
          | 4 | 5 | 6 |

          :: Caption {#tbl:label}
          """,
        """
          <p><span class="caption">Caption </span><a class="name" data-type="tbl" name="label" /></p>
          <table>
          <thead>
          <tr><th>a</th><th>b</th><th>c</th></tr>
          </thead>
          <tbody>
          <tr><td>1</td><td>2</td><td>3</td></tr>
          <tr><td>4</td><td>5</td><td>6</td></tr>
          </tbody>
          </table>
          """
      ),
      args(
        """
          ``` diagram-plantuml
          @startuml
          Alice -> Bob: Request
          Bob --> Alice: Response
          @enduml
          ```

          :: Diagram {#dia:seq1}
          """,
        """
          <p><span class="caption">Diagram </span><a class="name" data-type="dia" name="seq1" /></p>
          <pre><code class="language-diagram-plantuml">@startuml
          Alice -&gt; Bob: Request
          Bob --&gt; Alice: Response
          @enduml
          </code></pre>
          """
      ),
      args(
        """
          ::: lyrics
          Weather hit, meltin' road.
          Our mama's gone, six feet cold.
          Gas on down to future town,
          Make prophecy take hold.

          Warnin' sign, cent'ry old:
          When buyin' coal, air is sold.
          Aim our toil, ten figure oil;
          Trade life on Earth for gold.
          :::
          """,
        """
          <div class="lyrics">
          <p>Weather hit, meltin' road.
          Our mama's gone, six feet cold.
          Gas on down to future town,
          Make prophecy take hold.</p>
          <p>Warnin' sign, cent'ry old:
          When buyin' coal, air is sold.
          Aim our toil, ten figure oil;
          Trade life on Earth for gold.</p>
          </div>
          """
      )
    );
  }

  private static Arguments args( final String in, final String out ) {
    return Arguments.of( in, out );
  }

  private List<ParserExtension> createExtensions() {
    final var extensions = new LinkedList<ParserExtension>();
    final var context = builder()
      .with( Mutator::setExportFormat, XHTML_TEX )
      .build();

    extensions.add( TexExtension.create( s -> s, context ) );
    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( FencedDivExtension.create() );
    extensions.add( CrossReferenceExtension.create() );
    extensions.add( CaptionExtension.create() );

    return extensions;
  }
}
