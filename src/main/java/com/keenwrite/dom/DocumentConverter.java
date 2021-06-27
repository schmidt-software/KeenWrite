/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.dom;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.Document;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.keenwrite.dom.DocumentParser.sDomImplementation;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;

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
  private static final Map<String, String> LIGATURES = new LinkedHashMap<>();

  static {
    LIGATURES.put( "ffi", "\uFB03" );
    LIGATURES.put( "ffl", "\uFB04" );
    LIGATURES.put( "ff", "\uFB00" );
    LIGATURES.put( "fi", "\uFB01" );
    LIGATURES.put( "fl", "\uFB02" );
  }

  private static final NodeVisitor LIGATURE_VISITOR = new NodeVisitor() {
    @Override
    public void head( final Node node, final int depth ) {
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
    public void tail( final Node node, final int depth ) {
    }
  };

  @Override
  public Document fromJsoup( final org.jsoup.nodes.Document in ) {
    assert in != null;

    final var out = DocumentParser.newDocument();
    final org.jsoup.nodes.DocumentType doctype = in.documentType();

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
}
