/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.dom.DocumentConverter;
import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.fences.FencedDivExtension;
import com.keenwrite.processors.markdown.extensions.r.RInlineExtension;
import com.keenwrite.processors.markdown.extensions.references.CrossReferenceExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.IParse;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for parsing and rendering Markdown into HTML. This is required
 * to break a circular dependency between the {@link MarkdownProcessor} and
 * {@link RInlineExtension}.
 */
public class BaseMarkdownProcessor extends ExecutorProcessor<String> {

  private final IParse mParser;
  private final IRender mRenderer;

  public BaseMarkdownProcessor(
    final Processor<String> successor, final ProcessorContext context ) {
    super( successor );

    final var options = new MutableDataSet();
    options.set( HtmlRenderer.GENERATE_HEADER_ID, true );
    options.set( HtmlRenderer.RENDER_HEADER_ID, true );

    final var builder = Parser.builder( options );
    final var extensions = createExtensions( context );

    mParser = builder.extensions( extensions ).build();
    mRenderer = HtmlRenderer
      .builder( options )
      .extensions( extensions )
      .build();
  }

  /**
   * Instantiates a number of extensions to be applied when parsing.
   *
   * @param context The context that subclasses use to configure custom
   *                extension behaviour.
   * @return A {@link List} of {@link Extension} instances that change the
   * {@link Parser}'s behaviour.
   */
  List<Extension> createExtensions( final ProcessorContext context ) {
    final var extensions = new ArrayList<Extension>();

    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( FencedDivExtension.create() );
    extensions.add( CrossReferenceExtension.create() );

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
    return toXhtml( toHtml( toNode( markdown ) ) );
  }

  /**
   * Returns the AST in the form of a node for the given Markdown document. This
   * can be used, for example, to determine if a hyperlink exists inside a
   * paragraph.
   *
   * @param markdown The Markdown to convert into an AST.
   * @return The Markdown AST for the given text (usually a paragraph).
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
  private String toHtml( final Node node ) {
    return getRenderer().render( node );
  }

  /**
   * Ensures that subsequent processing will receive a well-formed document.
   * That is, an XHTML document.
   *
   * @param html Document to transform (may contain unbalanced HTML tags).
   * @return A well-formed (balanced) equivalent HTML document.
   */
  private String toXhtml( final String html ) {
    return DocumentConverter.parse( html ).html();
  }

  /**
   * Helper method to create an AST given some Markdown.
   *
   * @param markdown The Markdown to parse.
   * @return The root node of the Markdown tree.
   */
  private Node parse( final String markdown ) {
    return getParser().parse( markdown );
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
