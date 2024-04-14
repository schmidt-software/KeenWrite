/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors.html;

import com.keenwrite.preview.HtmlPreview;
import com.keenwrite.processors.ExecutorProcessor;

/**
 * Responsible for notifying the {@link HtmlPreview} when the succession
 * chain has updated. This decouples knowledge of changes to the editor panel
 * from the HTML preview panel as well as any processing that takes place
 * before the final HTML preview is rendered. This is the last link in the
 * processor chain.
 */
public final class HtmlPreviewProcessor extends ExecutorProcessor<String> {
  /**
   * There is only one preview panel.
   */
  private static HtmlPreview sHtmlPreview;

  /**
   * Constructs the end of a processing chain.
   *
   * @param htmlPreview The pane to update with the post-processed document.
   */
  public HtmlPreviewProcessor( final HtmlPreview htmlPreview ) {
    sHtmlPreview = htmlPreview;
  }

  /**
   * Update the preview panel using HTML from the succession chain.
   *
   * @param html The document content to render in the preview pane. The HTML
   *             should not contain a doctype, head, or body tag.
   * @return The given {@code html} string.
   */
  @Override
  public String apply( final String html ) {
    assert html != null;

    sHtmlPreview.render( html );
    return html;
  }
}
