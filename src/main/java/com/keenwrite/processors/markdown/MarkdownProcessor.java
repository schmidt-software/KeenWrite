/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.io.MediaType;
import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.IdentityProcessor;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
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

import java.util.ArrayList;
import java.util.List;

import static com.keenwrite.io.MediaType.TEXT_R_MARKDOWN;
import static com.keenwrite.io.MediaType.TEXT_R_XML;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 */
public class MarkdownProcessor extends ExecutorProcessor<String> {

  private final IParse mParser;
  private final IRender mRenderer;

  private MarkdownProcessor(
    final Processor<String> successor,
    final List<Extension> extensions ) {
    super( successor );

    mParser = Parser.builder().extensions( extensions ).build();
    mRenderer = HtmlRenderer.builder().extensions( extensions ).build();
  }

  public static MarkdownProcessor create( final ProcessorContext context ) {
    return create( IdentityProcessor.INSTANCE, context );
  }

  public static MarkdownProcessor create(
    final Processor<String> successor, final ProcessorContext context ) {
    final var extensions = createExtensions( context );
    return new MarkdownProcessor( successor, extensions );
  }

  /**
   * Instantiates a number of extensions to be applied when parsing. These
   * are typically typographic extensions that convert characters into
   * HTML entities.
   *
   * @return A {@link List} of {@link Extension} instances that
   * change the {@link Parser}'s behaviour.
   */
  private static List<Extension> createDefaultExtensions() {
    final List<Extension> extensions = new ArrayList<>();
    extensions.add( DefinitionExtension.create() );
    extensions.add( StrikethroughSubscriptExtension.create() );
    extensions.add( SuperscriptExtension.create() );
    extensions.add( TablesExtension.create() );
    extensions.add( TypographicExtension.create() );
    return extensions;
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
   * @return {@link List} of extensions invoked when parsing Markdown.
   */
  private static List<Extension> createExtensions(
    final ProcessorContext context ) {
    final var extensions = createDefaultExtensions();
    final var format = context.getExportFormat();
    final var workspace = context.getWorkspace();
    final var editorFile = context.getPath();
    final var linkDir = context.getBasePath();

    final var mediaType = MediaType.valueFrom( editorFile );
    if( mediaType == TEXT_R_MARKDOWN || mediaType == TEXT_R_XML ) {
      extensions.add( RExtension.create() );
    }

    extensions.add( ImageLinkExtension.create( linkDir, workspace ) );
    extensions.add( TeXExtension.create( format ) );
    extensions.add( FencedBlockExtension.create( context ) );
    extensions.add( CaretExtension.create( context.getCaret() ) );

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
