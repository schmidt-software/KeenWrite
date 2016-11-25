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
package com.scrivenvar.processors;

import java.util.ArrayList;
import java.util.List;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 *
 * @author White Magic Software, Ltd.
 */
public class MarkdownProcessor extends AbstractProcessor<String> {

  private List<Extension> extensions;

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
   *
   * @return The HTML representation of the Markdown document.
   */
  @Override
  public String processLink( final String markdown ) {
    return toHtml( markdown );
  }

  /**
   * Converts a string of markdown into HTML.
   *
   * @param markdown The markdown text to convert to HTML, must not be null.
   *
   * @return The markdown rendered as an HTML document.
   */
  private String toHtml( final String markdown ) {
    return createRenderer().render( createParser().parse( markdown ) );
  }

  /**
   * Returns the list of extensions to use when parsing and rendering Markdown
   * into HTML.
   *
   * @return A non-null list of Markdown extensions.
   */
  private synchronized List<Extension> getExtensions() {
    if( this.extensions == null ) {
      this.extensions = createExtensions();
    }

    return this.extensions;
  }

  /**
   * Creates a list that includes a TablesExtension. Subclasses may override
   * this method to insert more extensions, or remove the table extension.
   *
   * @return A list with an extension for parsing and rendering tables.
   */
  protected List<Extension> createExtensions() {
    final List<Extension> result = new ArrayList<>();
    result.add( TablesExtension.create() );
    return result;
  }

  /**
   * Creates the Markdown document processor.
   *
   * @return A Parser that can build an abstract syntax tree.
   */
  private Parser createParser() {
    return Parser.builder().extensions( getExtensions() ).build();
  }

  /**
   * Creates the HTML document renderer.
   *
   * @return A renderer that can convert a Markdown AST to HTML.
   */
  private HtmlRenderer createRenderer() {
    return HtmlRenderer.builder().extensions( getExtensions() ).build();
  }
}
