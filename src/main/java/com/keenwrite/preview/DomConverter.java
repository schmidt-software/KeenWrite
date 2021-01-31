/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.keenwrite.events.StatusEvent.clue;
import static com.keenwrite.processors.text.TextReplacementFactory.replace;

/**
 * Responsible for converting JSoup document object model (DOM) to a W3C DOM.
 * Provides a lighter implementation than the superclass by overriding the
 * {@link #fromJsoup(org.jsoup.nodes.Document)} method to reuse factories,
 * builders, and implementations.
 */
final class DomConverter extends W3CDom {
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
      if( node instanceof TextNode ) {
        final var parent = node.parentNode();
        final var name = parent == null ? "root" : parent.nodeName();

        if( !("pre".equalsIgnoreCase( name ) ||
          "code".equalsIgnoreCase( name ) ||
          "tt".equalsIgnoreCase( name )) ) {
          // Calling getWholeText() will return newlines, which must be kept
          // to ensure that preformatted text maintains its formatting.
          final var textNode = (TextNode) node;
          textNode.text( replace( textNode.getWholeText(), LIGATURES ) );
        }
      }
    }

    @Override
    public void tail( final Node node, final int depth ) {
    }
  };

  private static final DocumentBuilderFactory DOCUMENT_FACTORY;
  private static DocumentBuilder DOCUMENT_BUILDER;
  private static DOMImplementation DOM_IMPL;

  static {
    DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
    DOCUMENT_FACTORY.setNamespaceAware( true );

    try {
      DOCUMENT_BUILDER = DOCUMENT_FACTORY.newDocumentBuilder();
      DOM_IMPL = DOCUMENT_BUILDER.getDOMImplementation();
    } catch( final Exception ex ) {
      clue( ex );
    }
  }

  @Override
  public Document fromJsoup( final org.jsoup.nodes.Document in ) {
    assert in != null;
    assert DOCUMENT_BUILDER != null;
    assert DOM_IMPL != null;

    final var out = DOCUMENT_BUILDER.newDocument();
    final org.jsoup.nodes.DocumentType doctype = in.documentType();

    if( doctype != null ) {
      out.appendChild(
        DOM_IMPL.createDocumentType(
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
