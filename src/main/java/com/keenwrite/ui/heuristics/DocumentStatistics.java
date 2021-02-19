/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.heuristics;

import com.keenwrite.events.DocumentChangedEvent;
import com.panemu.tiwulfx.control.dock.DetachableTab;
import org.greenrobot.eventbus.Subscribe;

/**
 * Responsible for computing data about the document, such as word count and
 * word frequency.
 */
public class DocumentStatistics extends DetachableTab {
  @Subscribe
  public void handle( final DocumentChangedEvent event ) {
    System.out.println( event.getDocument() );
  }
}
