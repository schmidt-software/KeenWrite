/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.processors;

/**
 * Responsible for making the body of an HTML document complete by wrapping
 * it with html and body elements.
 */
public final class XhtmlProcessor extends ExecutorProcessor<String> {
  @Override
  public String apply( final String html ) {
    return "<html><body>" + html + "</body></html>";
  }
}
