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
import com.scrivenvar.Constants;
import com.scrivenvar.FileEditorTab;
import com.scrivenvar.FileType;
import com.scrivenvar.preview.HTMLPreviewPane;
import java.nio.file.Path;
import java.util.Map;

/**
 * Responsible for creating processors capable of parsing, transforming,
 * interpolating, and rendering known file types.
 *
 * @author White Magic Software, Ltd.
 */
public class ProcessorFactory extends AbstractFileFactory {

  private final HTMLPreviewPane previewPane;
  private final Map<String, String> resolvedMap;

  private Processor<String> terminalProcessChain;

  /**
   * Constructs a factory with the ability to create processors that can perform
   * text and caret processing to generate a final preview.
   *
   * @param previewPane
   * @param resolvedMap
   */
  public ProcessorFactory(
    final HTMLPreviewPane previewPane,
    final Map<String, String> resolvedMap ) {
    this.previewPane = previewPane;
    this.resolvedMap = resolvedMap;
  }

  /**
   * Creates a processor suitable for parsing and rendering the file opened at
   * the given tab.
   *
   * @param tab The tab containing a text editor, path, and caret position.
   *
   * @return A processor that can render the given tab's text.
   */
  public Processor<String> createProcessor( final FileEditorTab tab ) {
    final Path path = tab.getPath();
    final FileType fileType = lookup( path, Constants.GLOB_PREFIX_FILE );
    Processor<String> processor = null;

    switch( fileType ) {
      case RMARKDOWN:
        processor = createRMarkdownProcessor( tab );
        break;

      case MARKDOWN:
        processor = createMarkdownProcessor( tab );
        break;

      case XML:
        processor = createXMLProcessor( tab );
        break;

      default:
        unknownExtension( path );
        break;
    }

    return processor;
  }

  /**
   * Returns a processor common to all processors: markdown, caret position
   * token replacer, and an HTML preview renderer.
   *
   * @return Processors at the end of the processing chain.
   */
  private Processor<String> getTerminalProcessChain() {
    if( this.terminalProcessChain == null ) {
      this.terminalProcessChain = createTerminalProcessChain();
    }

    return this.terminalProcessChain;
  }

  private Processor<String> createTerminalProcessChain() {
    final Processor<String> hpp = new HTMLPreviewProcessor( getPreviewPane() );
    final Processor<String> mcrp = new CaretReplacementProcessor( hpp );
    final Processor<String> mpp = new MarkdownProcessor( mcrp );

    return mpp;
  }

  protected Processor<String> createMarkdownProcessor( final FileEditorTab tab ) {
    final Processor<String> bp = getTerminalProcessChain();
    final Processor<String> xcip = new MarkdownCaretInsertionProcessor( bp, tab.caretPositionProperty() );
    final Processor<String> vp = new VariableProcessor( xcip, getResolvedMap() );

    return vp;
  }

  protected Processor<String> createRMarkdownProcessor( final FileEditorTab tab ) {
    return createMarkdownProcessor( tab );
  }

  protected Processor<String> createXMLProcessor( final FileEditorTab tab ) {
    final Processor<String> bp = getTerminalProcessChain();
    final Processor<String> xmlp = new XMLProcessor( bp, tab.getPath() );
    final Processor<String> xcip = new XMLCaretInsertionProcessor( xmlp, tab.caretPositionProperty() );
    final Processor<String> vp = new VariableProcessor( xcip, getResolvedMap() );

    return vp;
  }

  private HTMLPreviewPane getPreviewPane() {
    return this.previewPane;
  }

  /**
   * Returns the variable map of interpolated definitions.
   *
   * @return A map to help dereference variables.
   */
  private Map<String, String> getResolvedMap() {
    return this.resolvedMap;
  }
}
