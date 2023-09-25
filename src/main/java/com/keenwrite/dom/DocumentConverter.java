/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.dom;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.Document;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.keenwrite.dom.DocumentParser.sDomImplementation;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;
import static java.util.Map.*;

/**
 * Responsible for converting JSoup document object model (DOM) to a W3C DOM.
 * Provides a lighter implementation than the superclass by overriding the
 * {@link #fromJsoup(org.jsoup.nodes.Document)} method to reuse factories,
 * builders, and implementations.
 */
public final class DocumentConverter extends W3CDom {
  /**
   * Retain insertion order using an instance of {@link LinkedHashMap} so
   * that ligature substitution uses longer ligatures ahead of shorter
   * ligatures. The word "ruffian" should use the "ffi" ligature, not the "ff"
   * ligature.
   */
  private static final Map<String, String> LIGATURES = ofEntries(
    entry( "ffi", "ﬃ" ),
    entry( "ffl", "ﬄ" ),
    entry( "ff", "ﬀ" ),
    entry( "fi", "ﬁ" ),
    entry( "fl", "ﬂ" )
  );

  private static final NodeVisitor LIGATURE_VISITOR = new NodeVisitor() {
    @Override
    public void head( final @NotNull Node node, final int depth ) {
      if( node instanceof final TextNode textNode ) {
        final var parent = node.parentNode();
        final var name = parent == null ? "root" : parent.nodeName();

        if( !("pre".equalsIgnoreCase( name ) ||
          "code".equalsIgnoreCase( name ) ||
          "kbd".equalsIgnoreCase( name ) ||
          "var".equalsIgnoreCase( name ) ||
          "tt".equalsIgnoreCase( name )) ) {
          // Calling getWholeText() will return newlines, which must be kept
          // to ensure that preformatted text maintains its formatting.
          textNode.text( replace( textNode.getWholeText(), LIGATURES ) );
        }
      }
    }

    @Override
    public void tail( final @NotNull Node node, final int depth ) { }
  };

  @Override
  public @NotNull Document fromJsoup( final org.jsoup.nodes.Document in ) {
    assert in != null;

    final var out = DocumentParser.newDocument();
    final var doctype = in.documentType();

    if( doctype != null ) {
      out.appendChild(
        sDomImplementation.createDocumentType(
          doctype.name(),
          doctype.publicId(),
          doctype.systemId()
        )
      );
    }

    out.setXmlStandalone( true );
    in.traverse( LIGATURE_VISITOR );
    convert( in, out );

    return out;
  }

  /**
   * Converts the given non-well-formed HTML document into an XML document
   * while preserving whitespace.
   *
   * @param html The document to convert.
   * @return The converted document as an object model.
   */
  public static org.jsoup.nodes.Document parse( final String html ) {
    final var document = Jsoup.parse( html );

    document
      .outputSettings()
      .syntax( Syntax.xml )
      .prettyPrint( false );

    return document;
  }
}
