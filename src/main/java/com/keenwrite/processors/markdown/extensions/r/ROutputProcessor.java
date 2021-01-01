/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.markdown.extensions.r;

import com.keenwrite.processors.ExecutorProcessor;
import com.keenwrite.processors.InlineRProcessor;
import com.keenwrite.processors.markdown.MarkdownProcessor;
import com.keenwrite.processors.markdown.extensions.tex.TeXExtension;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.IParse;
import com.vladsch.flexmark.util.ast.IRender;

/**
 * Responsible for parsing the output from an R eval statement. This class
 * is used to avoid an circular dependency whereby the {@link InlineRProcessor}
 * must treat the output from an R function call as Markdown, which would
 * otherwise require a {@link MarkdownProcessor} instance; however, the
 * {@link MarkdownProcessor} class gives precedence to its extensions, which
 * means the {@link TeXExtension} will be executed <em>before</em> the
 * {@link InlineRProcessor}, thereby being exposed to backticks in a TeX
 * macro---a syntax error. To break the cycle, the {@link InlineRProcessor}
 * uses this class instead of {@link MarkdownProcessor}.
 */
public class ROutputProcessor extends ExecutorProcessor<String> {
  private final IParse mParser = Parser.builder().build();
  private final IRender mRenderer = HtmlRenderer.builder().build();

  @Override
  public String apply( final String markdown ) {
    var node = mParser.parse( markdown ).getFirstChild();

    if( node == null ) {
      node = new Text();
    }
    else if( node.isOrDescendantOfType( Paragraph.class ) ) {
      node = new Text( node.getChars() );
    }

    // Trimming prevents displaced commas and unwanted newlines.
    return mRenderer.render( node ).trim();
  }
}
