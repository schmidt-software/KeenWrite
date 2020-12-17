/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.adapters;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

/**
 * Allows subclasses to implement only specific events of interest.
 */
public abstract class ReplacedElementAdapter implements ReplacedElementFactory {
  @Override
  public void reset() {
  }

  @Override
  public void remove( final Element e ) {
  }

  @Override
  public void setFormSubmissionListener(
      final FormSubmissionListener listener ) {
  }
}
