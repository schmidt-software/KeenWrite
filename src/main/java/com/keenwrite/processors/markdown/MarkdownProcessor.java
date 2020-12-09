/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.keenwrite.processors.markdown;

import com.keenwrite.ExportFormat;
import com.keenwrite.processors.*;
import com.keenwrite.processors.markdown.r.RExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.IParse;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.Extension;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;

import static com.keenwrite.AbstractFileFactory.lookup;
import static com.keenwrite.Constants.DEFAULT_DIRECTORY;
import static com.keenwrite.ExportFormat.NONE;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 */
public class MarkdownProcessor extends ExecutorProcessor<String> {

  private final IParse mParser;
  private final IRender mRenderer;

  private MarkdownProcessor(
      final Processor<String> successor,
      final Collection<Extension> extensions ) {
    super( successor );

    extensions.add( LigatureExtension.create() );

    mParser = Parser.builder().extensions( extensions ).build();
    mRenderer = HtmlRenderer.builder().extensions( extensions ).build();
  }

  public static MarkdownProcessor create() {
    return create( IdentityProcessor.INSTANCE, DEFAULT_DIRECTORY );
  }

  public static MarkdownProcessor create( final ProcessorContext context ) {
    return create( IdentityProcessor.INSTANCE, context );
  }

  public static MarkdownProcessor create(
      final Processor<String> successor, final Path path ) {
    final var extensions = createExtensions( path, NONE );
    return new MarkdownProcessor( successor, extensions );
  }

  public static MarkdownProcessor create(
      final Processor<String> successor, final ProcessorContext context ) {
    final var extensions = createExtensions( context );
    return new MarkdownProcessor( successor, extensions );
  }

  /**
   * Creating extensions based using an instance of {@link ProcessorContext}
   * indicates that the {@link CaretExtension} should be used to inject the
   * caret position into the final HTML document. This enables the HTML
   * preview pane to scroll to the same position, relatively speaking, within
   * the main document. Scrolling is developed this way to decouple the
   * document being edited from the preview pane so that multiple document
   * formats can be edited.
   *
   * @param context Contains necessary information needed to create extensions
   *                used by the Markdown parser.
   * @return {@link Collection} of extensions invoked when parsing Markdown.
   */
  private static Collection<Extension> createExtensions(
      final ProcessorContext context ) {
    final var path = context.getBasePath();
    final var format = context.getExportFormat();
    final var extensions = createExtensions( path, format );

    extensions.add( CaretExtension.create( context.getCaret() ) );

    return extensions;
  }

  /**
   * Creates parser extensions that tweak the parsing engine based on various
   * conditions. For example, this will add a new {@link TeXExtension} that
   * can export TeX as either SVG or TeX macros. The tweak also includes the
   * ability to keep inline R statements, rather than convert them to inline
   * code elements, so that the {@link InlineRProcessor} can interpret the
   * R statements.
   *
   * @param path   Path name for referencing image files via relative paths
   *               and dynamic file types.
   * @param format TeX export format to use when generating HTMl documents.
   * @return {@link Collection} of extensions invoked when parsing Markdown.
   */
  private static Collection<Extension> createExtensions(
      final Path path, final ExportFormat format ) {
    final var extensions = createDefaultExtensions();

    extensions.add( ImageLinkExtension.create( path ) );
    extensions.add( TeXExtension.create( format ) );

    if( lookup( path ).isR() ) {
      extensions.add( RExtension.create() );
    }

    return extensions;
  }

  /**
   * Instantiates a number of extensions to be applied when parsing. These
   * are typically typographic extensions that convert characters into
   * HTML entities.
   *
   * @return A {@link Collection} of {@link Extension} instances that
   * change the {@link Parser}'s behaviour.
   */
  private static Collection<Extension> createDefaultExtensions() {
    final var extensions = new HashSet<Extension>();
    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( TypographicExtension.create() );
    return extensions;
  }

  /**
   * Converts the given Markdown string into HTML, without the doctype, html,
   * head, and body tags.
   *
   * @param markdown The string to convert from Markdown to HTML.
   * @return The HTML representation of the Markdown document.
   */
  @Override
  public String apply( final String markdown ) {
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
   * Returns the result of converting the given AST into an HTML string.
   *
   * @param node The AST {@link Node} to convert to an HTML string.
   * @return The given {@link Node} as an HTML string.
   */
  public String toHtml( final Node node ) {
    return getRenderer().render( node );
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
    return toHtml( parse( markdown ) );
  }

  /**
   * Creates the Markdown document processor.
   *
   * @return An instance of {@link IParse} for building abstract syntax trees.
   */
  private IParse getParser() {
    return mParser;
  }

  private IRender getRenderer() {
    return mRenderer;
  }
}
