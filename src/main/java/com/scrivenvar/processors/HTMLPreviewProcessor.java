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

import com.scrivenvar.preview.HTMLPreviewPane;

/**
 * Responsible for notifying the HTMLPreviewPane when the succession chain has
 * updated. This decouples knowledge of changes to the editor panel from the
 * HTML preview panel as well as any processing that takes place before the
 * final HTML preview is rendered. This should be the last link in the processor
 * chain.
 *
 * @author White Magic Software, Ltd.
 */
public class HTMLPreviewProcessor extends AbstractProcessor<String> {

  private HTMLPreviewPane htmlPreviewPane;

  /**
   * Constructs the end of a processing chain.
   *
   * @param htmlPreviewPane The pane to update with the post-processed document.
   */
  public HTMLPreviewProcessor( final HTMLPreviewPane htmlPreviewPane ) {
    super( null );
    setHtmlPreviewPane( htmlPreviewPane );
  }

  /**
   * Update the preview panel using HTML from the succession chain.
   *
   * @param html The document content to render in the preview pane. The HTML
   * should not contain a doctype, head, or body tag, only content to render
   * within the body.
   *
   * @return null
   */
  @Override
  public String processLink( final String html ) {
    getHtmlPreviewPane().update( html );

    // No more processing required.
    return null;
  }

  private HTMLPreviewPane getHtmlPreviewPane() {
    return this.htmlPreviewPane;
  }

  private void setHtmlPreviewPane( final HTMLPreviewPane htmlPreviewPane ) {
    this.htmlPreviewPane = htmlPreviewPane;
  }
}
