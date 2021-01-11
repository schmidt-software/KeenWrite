/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown;

import com.keenwrite.io.MediaType;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.markdown.extensions.FencedBlockExtension;
import com.keenwrite.processors.markdown.extensions.ImageLinkExtension;
import com.keenwrite.processors.markdown.extensions.caret.CaretExtension;
import com.keenwrite.processors.markdown.extensions.r.RExtension;
import com.keenwrite.processors.markdown.extensions.tex.TeXExtension;
import com.keenwrite.processors.r.RProcessor;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.List;

import static com.keenwrite.io.MediaType.TEXT_R_MARKDOWN;
import static com.keenwrite.io.MediaType.TEXT_R_XML;
import static com.keenwrite.processors.IdentityProcessor.IDENTITY;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 */
public class MarkdownProcessor extends BaseMarkdownProcessor {

  private MarkdownProcessor(
    final Processor<String> successor, final ProcessorContext context ) {
    super( successor, context );
  }

  public static MarkdownProcessor create( final ProcessorContext context ) {
    return create( IDENTITY, context );
  }

  public static MarkdownProcessor create(
    final Processor<String> successor, final ProcessorContext context ) {
    return new MarkdownProcessor( successor, context );
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
   * @param extensions {@link List} of extensions invoked when parsing Markdown.
   * @param context    Contains necessary information needed to create
   *                   extensions used by the Markdown parser.
   */
  void init(
    final List<Extension> extensions, final ProcessorContext context ) {
    final var editorFile = context.getDocumentPath();
    final var mediaType = MediaType.valueFrom( editorFile );
    final Processor<String> processor;

    if( mediaType == TEXT_R_MARKDOWN || mediaType == TEXT_R_XML ) {
      final var rProcessor = new RProcessor( context );
      extensions.add( RExtension.create( rProcessor ) );
      processor = rProcessor;
    }
    else {
      processor = IDENTITY;
    }

    // Add typographic extensions.
    super.init( extensions, context );

    extensions.add( ImageLinkExtension.create( context ) );
    extensions.add( TeXExtension.create( context, processor ) );
    extensions.add( FencedBlockExtension.create( context ) );
    extensions.add( CaretExtension.create( context ) );
  }
}
