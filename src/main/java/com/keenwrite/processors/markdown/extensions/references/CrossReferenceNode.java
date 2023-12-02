/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown.extensions.references;

import com.vladsch.flexmark.html.HtmlWriter;

/**
 * Responsible for generating anchor links, either named or cross-referenced.
 */
public interface CrossReferenceNode {
  String getTypeName();

  String getIdName();

  String getRefAttrName();

  /**
   * Writes the HTML representation for this cross-reference node.
   *
   * @param html The HTML tag is written to the {@link HtmlWriter}.
   */
  default void write( final HtmlWriter html ) {
    final var type = getTypeName();
    final var id = getIdName();
    final var attr = getRefAttrName();

    final var clazz = STR. "class=\"\{ attr }\"" ;
    final var dataType = STR. "data-type=\"\{ type }\"" ;
    final var refId = STR. "\{ attr }=\"\{ id }\"" ;

    html.raw( STR. "<a \{ clazz } \{ dataType } \{ refId } />" );
  }
}
