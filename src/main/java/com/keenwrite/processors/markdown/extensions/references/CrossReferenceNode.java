/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.html.HtmlWriter;

/**
 * Responsible for generating anchor links, either named or cross-referenced.
 */
interface CrossReferenceNode {
  String getTypeName();

  String getIdName();

  String getRefAttrName();

  default String toHtml() {
    final String typeName = getTypeName();
    final String idName = getIdName();

    return toHtml( typeName, idName );
  }

  default String toHtml( final String type, final String id ) {
    return STR.
      "<a data-type=\"\{ type }\" \{ getRefAttrName() }=\"\{ id }\" />" ;
  }

  default void write( final HtmlWriter html ) {
    html.raw( toHtml() );
  }
}
