/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.preview;

import com.panemu.tiwulfx.control.dock.DetachableTab;
import com.panemu.tiwulfx.control.dock.DetachableTabPane;

/**
 * Responsible for displaying output in tabs, such as {@link HtmlPreview}.
 */
public class OutputTabPane extends DetachableTabPane {
  private final HtmlPreview mHtmlPreview = new HtmlPreview();

  public OutputTabPane() {
    getTabs().add( createHtmlPreviewTab() );
  }

  public HtmlPreview getHtmlPreview() {
    return mHtmlPreview;
  }

  private DetachableTab createHtmlPreviewTab() {
    return new DetachableTab( "HTML", getHtmlPreview() );
  }
}
