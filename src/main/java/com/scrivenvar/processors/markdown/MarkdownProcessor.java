/*
 * Copyright 2016 White Magic Software, Ltd.
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
package com.scrivenvar.processors.markdown;

import com.scrivenvar.processors.AbstractProcessor;
import com.scrivenvar.processors.Processor;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.IParse;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 *
 * @author White Magic Software, Ltd.
 */
public class MarkdownProcessor extends AbstractProcessor<String> {

  private final static HtmlRenderer RENDERER;
  private final static IParse PARSER;

  static {
    final Collection<Extension> extensions = new ArrayList<>();
    extensions.add( TablesExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( ImageLinkExtension.create() );

    RENDERER = HtmlRenderer.builder().extensions( extensions ).build();
    PARSER = Parser.builder().extensions( extensions ).build();
  }

  /**
   * Constructs a new Markdown processor that can create HTML documents.
   *
   * @param successor Usually the HTML Preview Processor.
   */
  public MarkdownProcessor( final Processor<String> successor ) {
    super( successor );
  }

  /**
   * Converts the given Markdown string into HTML, without the doctype, html,
   * head, and body tags.
   *
   * @param markdown The string to convert from Markdown to HTML.
   * @return The HTML representation of the Markdown document.
   */
  @Override
  public String processLink( final String markdown ) {
    return toHtml( markdown );
  }

  /**
   * Returns the AST in the form of a node for the given markdown document. This
   * can be used, for example, to determine if a hyperlink exists inside of a
   * paragraph.
   *
   * @param markdown The markdown to convert into an AST.
   * @return The markdown AST for the given text (usually a paragraph).
   */
  public Node toNode( final String markdown ) {
    return parse( markdown );
  }

  /**
   * Helper method to create an AST given some markdown.
   *
   * @param markdown The markdown to parse.
   * @return The root node of the markdown tree.
   */
  private Node parse( final String markdown ) {
    return getParser().parse( markdown );
  }

  /**
   * Converts a string of markdown into HTML.
   *
   * @param markdown The markdown text to convert to HTML, must not be null.
   * @return The markdown rendered as an HTML document.
   */
  private String toHtml( final String markdown ) {
    return getRenderer().render( parse( markdown ) );
  }

  /**
   * Creates the Markdown document processor.
   *
   * @return A Parser that can build an abstract syntax tree.
   */
  private IParse getParser() {
    return PARSER;
  }

  private HtmlRenderer getRenderer() {
    return RENDERER;
  }
}
