/*
 * Copyright 2020-2021 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.tex;

import com.whitemagicsoftware.tex.DefaultTeXFont;
import com.whitemagicsoftware.tex.TeXEnvironment;
import com.whitemagicsoftware.tex.TeXFormula;
import com.whitemagicsoftware.tex.TeXLayout;
import com.whitemagicsoftware.tex.graphics.AbstractGraphics2D;
import com.whitemagicsoftware.tex.graphics.SvgDomGraphics2D;
import com.whitemagicsoftware.tex.graphics.SvgGraphics2D;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.keenwrite.preview.SvgRasterizer.*;
import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that TeX rasterization produces a readable image.
 */
public class TeXRasterization {
  private static final String LOAD_EXTERNAL_DTD =
      "http://apache.org/xml/features/nonvalidating/load-external-dtd";

  private static final String EQUATION =
      "G_{\\mu \\nu} = \\frac{8 \\pi G}{c^4} T_{{\\mu \\nu}}";

  private static final String DIR_TEMP = getProperty( "java.io.tmpdir" );

  private static final long FILESIZE = 12364;

  /**
   * Test that an equation can be converted to a raster image and the
   * final raster image size corresponds to the input equation. This is
   * a simple way to verify that the rasterization process is correct,
   * albeit if any aspect of the SVG algorithm changes (such as padding
   * around the equation), it will cause this test to fail, which is a bit
   * misleading.
   */
  @Test
  public void test_Rasterize_SimpleFormula_CorrectImageSize()
      throws IOException {
    final var g = new SvgGraphics2D();
    drawGraphics( g );
    verifyImage( rasterizeString( g.toString() ) );
  }

  /**
   * Test that an SVG document object model can be parsed and rasterized into
   * an image.
   */
  @Test
  public void getTest_SvgDomGraphics2D_InputElement_OutputRasterizedImage()
      throws ParserConfigurationException, IOException, SAXException {
    final var g = new SvgGraphics2D();
    drawGraphics( g );

    final var expectedSvg = g.toString();
    final var bytes = expectedSvg.getBytes();

    final var dbf = DocumentBuilderFactory.newInstance();
    dbf.setFeature( LOAD_EXTERNAL_DTD, false );
    dbf.setNamespaceAware( false );
    final var builder = dbf.newDocumentBuilder();

    final var doc = builder.parse( new ByteArrayInputStream( bytes ) );
    final var actualSvg = toSvg( doc.getDocumentElement() );

    verifyImage( rasterizeString( actualSvg ) );
  }

  /**
   * Test that an SVG image from a DOM element can be rasterized.
   *
   * @throws IOException Could not write the image.
   */
  @Test
  public void test_SvgDomGraphics2D_InputDom_OutputRasterizedImage()
      throws IOException {
    final var g = new SvgDomGraphics2D();
    drawGraphics( g );

    final var dom = g.toDom();

    verifyImage( rasterize( dom ) );
  }

  /**
   * Asserts that the given image matches an expected file size.
   *
   * @param image The image to check against the file size.
   * @throws IOException Could not write the image.
   */
  private void verifyImage( final BufferedImage image ) throws IOException {
    final var file = export( image, "dom.png" );
    assertEquals( FILESIZE, file.length() );
  }

  /**
   * Creates an SVG string for the default equation and font size.
   */
  private void drawGraphics( final AbstractGraphics2D g ) {
    final var size = 100f;
    final var texFont = new DefaultTeXFont( size );
    final var env = new TeXEnvironment( texFont );
    g.scale( size, size );

    final var formula = new TeXFormula( EQUATION );
    final var box = formula.createBox( env );
    final var layout = new TeXLayout( box, size );

    g.initialize( layout.getWidth(), layout.getHeight() );
    box.draw( g, layout.getX(), layout.getY() );
  }

  @SuppressWarnings("SameParameterValue")
  private File export( final BufferedImage image, final String filename )
      throws IOException {
    final var path = Path.of( DIR_TEMP, filename );
    final var file = path.toFile();
    ImageIO.write( image, "png", file );
    file.deleteOnExit();
    return file;
  }
}
