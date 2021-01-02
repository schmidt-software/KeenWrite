/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.adapters;

import org.xhtmlrenderer.event.DocumentListener;

import static com.keenwrite.StatusNotifier.clue;

/**
 * Allows subclasses to implement only specific events of interest.
 */
public class DocumentAdapter implements DocumentListener {
  @Override
  public void documentStarted() {
  }

  @Override
  public void documentLoaded() {
  }

  @Override
  public void onLayoutException( final Throwable t ) {
    clue( t );
  }

  @Override
  public void onRenderException( final Throwable t ) {
    clue( t );
  }
}
