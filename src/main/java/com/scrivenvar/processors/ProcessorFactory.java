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

import com.scrivenvar.AbstractFileFactory;
import com.scrivenvar.FileEditorTab;
import com.scrivenvar.preview.HTMLPreviewPane;
import com.scrivenvar.processors.markdown.MarkdownCaretInsertionProcessor;
import com.scrivenvar.processors.markdown.MarkdownProcessor;
import javafx.beans.value.ObservableValue;

import java.nio.file.Path;
import java.util.Map;

/**
 * Responsible for creating processors capable of parsing, transforming,
 * interpolating, and rendering known file types.
 *
 * @author White Magic Software, Ltd.
 */
public class ProcessorFactory extends AbstractFileFactory {

  private final HTMLPreviewPane mPreviewPane;
  private final Map<String, String> mResolvedMap;
  private final Processor<String> mCommonProcessor;

  /**
   * Constructs a factory with the ability to create processors that can perform
   * text and caret processing to generate a final preview.
   *
   * @param previewPane Where the final output is rendered.
   * @param resolvedMap Flat map of definitions to replace before final render.
   */
  public ProcessorFactory(
      final HTMLPreviewPane previewPane,
      final Map<String, String> resolvedMap ) {
    mPreviewPane = previewPane;
    mResolvedMap = resolvedMap;
    mCommonProcessor = createCommonProcessor();
  }

  /**
   * Creates a processor suitable for parsing and rendering the file opened at
   * the given tab.
   *
   * @param tab The tab containing a text editor, path, and caret position.
   * @return A processor that can render the given tab's text.
   */
  public Processor<String> createProcessor( final FileEditorTab tab ) {
    final Path path = tab.getPath();
    final Processor<String> processor;

    switch( lookup( path ) ) {
      case RMARKDOWN:
        processor = createRProcessor( tab );
        break;

      case SOURCE:
        processor = createMarkdownProcessor( tab );
        break;

      case XML:
        processor = createXMLProcessor( tab );
        break;

      case RXML:
        processor = createRXMLProcessor( tab );
        break;

      default:
        processor = createIdentityProcessor();
        break;
    }

    return processor;
  }

  /**
   * Creates and links the processors at the end of the processing chain.
   *
   * @return A markdown, caret replacement, and preview pane processor chain.
   */
  private Processor<String> createCommonProcessor() {
    final var hpp = new HTMLPreviewProcessor( getPreviewPane() );
    final var mcrp = new CaretReplacementProcessor( hpp );

    return new MarkdownProcessor( mcrp );
  }

  protected Processor<String> createIdentityProcessor() {
    final var hpp = new HTMLPreviewProcessor( getPreviewPane() );

    return new IdentityProcessor( hpp );
  }

  protected Processor<String> createDefinitionProcessor(
      final Processor<String> p ) {
    return new DefinitionProcessor( p, getResolvedMap() );
  }

  protected Processor<String> createMarkdownProcessor(
      final FileEditorTab tab ) {
    final var caret = tab.caretPositionProperty();
    final var tpc = getCommonProcessor();
    final var cip = createMarkdownInsertionProcessor( tpc, caret );

    return createDefinitionProcessor( cip );
  }

  protected Processor<String> createXMLProcessor( final FileEditorTab tab ) {
    final var caret = tab.caretPositionProperty();
    final var tpc = getCommonProcessor();
    final var xmlp = new XMLProcessor( tpc, tab.getPath() );
    final var dp = createDefinitionProcessor( xmlp );

    return createXMLInsertionProcessor( dp, caret );
  }

  protected Processor<String> createRProcessor( final FileEditorTab tab ) {
    final var caret = tab.caretPositionProperty();
    final var tpc = getCommonProcessor();
    final var rp = new InlineRProcessor( tpc, getResolvedMap() );
    final var rvp = new RVariableProcessor( rp, getResolvedMap() );

    return createRInsertionProcessor( rvp, caret );
  }

  protected Processor<String> createRXMLProcessor( final FileEditorTab tab ) {
    final var caret = tab.caretPositionProperty();
    final var tpc = getCommonProcessor();
    final var xmlp = new XMLProcessor( tpc, tab.getPath() );
    final var rp = new InlineRProcessor( xmlp, getResolvedMap() );
    final var rvp = new RVariableProcessor( rp, getResolvedMap() );

    return createXMLInsertionProcessor( rvp, caret );
  }

  private Processor<String> createMarkdownInsertionProcessor(
      final Processor<String> tpc, final ObservableValue<Integer> caret ) {
    return new MarkdownCaretInsertionProcessor( tpc, caret );
  }

  /**
   * Create an insertion processor that is aware of R statements and will insert
   * a caret outside of any statement the caret falls within.
   *
   * @param processor Another link in the processor chain.
   * @param caret     The caret insertion point.
   * @return A processor that can insert a caret token without disturbing any R
   * code.
   */
  private Processor<String> createRInsertionProcessor(
      final Processor<String> processor,
      final ObservableValue<Integer> caret ) {
    return new RMarkdownCaretInsertionProcessor( processor, caret );
  }

  private Processor<String> createXMLInsertionProcessor(
      final Processor<String> tpc, final ObservableValue<Integer> caret ) {
    return new XMLCaretInsertionProcessor( tpc, caret );
  }

  private HTMLPreviewPane getPreviewPane() {
    return mPreviewPane;
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return mResolvedMap;
  }

  /**
   * Returns a processor common to all processors: markdown, caret position
   * token replacer, and an HTML preview renderer.
   *
   * @return Processors at the end of the processing chain.
   */
  private Processor<String> getCommonProcessor() {
    return mCommonProcessor;
  }
}
