/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import org.jsoup.helper.W3CDom;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Responsible for converting JSoup document object model (DOM) to a W3C DOM.
 * Provides a lighter implementation than the superclass by overriding the
 * {@link #fromJsoup(org.jsoup.nodes.Document)} method to reuse factories,
 * builders, and implementations.
 */
class DomConverter extends W3CDom {
  private static final DocumentBuilderFactory DOCUMENT_FACTORY;
  private static DocumentBuilder DOCUMENT_BUILDER;
  private static DOMImplementation DOM_IMPL;

  static {
    DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
    DOCUMENT_FACTORY.setNamespaceAware( true );

    try {
      DOCUMENT_BUILDER = DOCUMENT_FACTORY.newDocumentBuilder();
      DOM_IMPL = DOCUMENT_BUILDER.getDOMImplementation();
    } catch( final Exception ignored ) {
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
    convert( in, out );
    return out;
  }
}
