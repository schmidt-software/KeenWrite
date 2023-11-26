/* Copyright 2022 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.processors.markdown;

import com.keenwrite.ExportFormat;
import com.keenwrite.io.MediaType;
import com.keenwrite.processors.Processor;
import com.keenwrite.processors.ProcessorContext;
import com.keenwrite.processors.VariableProcessor;
import com.keenwrite.processors.markdown.extensions.caret.CaretExtension;
import com.keenwrite.processors.markdown.extensions.fences.FencedBlockExtension;
import com.keenwrite.processors.markdown.extensions.images.ImageLinkExtension;
import com.keenwrite.processors.markdown.extensions.outline.DocumentOutlineExtension;
import com.keenwrite.processors.markdown.extensions.r.RInlineExtension;
import com.keenwrite.processors.markdown.extensions.tex.TexExtension;
import com.keenwrite.processors.r.RInlineEvaluator;
import com.keenwrite.processors.r.RVariableProcessor;
import com.vladsch.flexmark.util.misc.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.keenwrite.io.MediaType.TEXT_R_MARKDOWN;
import static com.keenwrite.processors.IdentityProcessor.IDENTITY;

/**
 * Responsible for parsing a Markdown document and rendering it as HTML.
 */
public final class MarkdownProcessor extends BaseMarkdownProcessor {

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
   * @param context Contains necessary information needed to create
   *                extensions used by the Markdown parser.
   * @return {@link List} of extensions invoked when parsing Markdown.
   */
  @Override
  List<Extension> createExtensions( final ProcessorContext context ) {
    final var inputPath = context.getSourcePath();
    final var mediaType = MediaType.fromFilename( inputPath );
    final Processor<String> processor;
    final Function<String, String> evaluator;
    final List<Extension> result = new ArrayList<>();

    if( mediaType == TEXT_R_MARKDOWN ) {
      final var rVarProcessor = new RVariableProcessor( IDENTITY, context );
      final var rInlineEvaluator = new RInlineEvaluator( rVarProcessor );
      result.add( RInlineExtension.create( rInlineEvaluator, context ) );
      processor = rVarProcessor;
      evaluator = rInlineEvaluator;
    }
    else {
      processor = new VariableProcessor( IDENTITY, context );
      evaluator = processor;
    }

    // Add typographic, table, strikethrough, and similar extensions.
    result.addAll( super.createExtensions( context ) );

    result.add( ImageLinkExtension.create( context ) );
    result.add( TexExtension.create( evaluator, context ) );
    result.add( FencedBlockExtension.create( processor, evaluator, context ) );

    if( context.isExportFormat( ExportFormat.NONE ) ) {
      result.add( CaretExtension.create( context ) );
    }

    result.add( DocumentOutlineExtension.create( processor ) );
    return result;
  }
}
