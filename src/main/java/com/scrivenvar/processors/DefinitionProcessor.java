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

import static com.scrivenvar.processors.text.TextReplacementFactory.replace;

import java.util.Map;

/**
 * Processes interpolated string definitions in the document and inserts
 * their values into the post-processed text. The default variable syntax is
 * {@code $variable$}.
 *
 * @author White Magic Software, Ltd.
 */
public class DefinitionProcessor extends AbstractProcessor<String> {

  private final Map<String, String> mDefinitions;

  public DefinitionProcessor(
      final Processor<String> successor, final Map<String, String> map ) {
    super( successor );
    mDefinitions = map;
  }

  /**
   * Processes the given text document by replacing variables with their values.
   *
   * @param text The document text that includes variables that should be
   *             replaced with values when rendered as HTML.
   * @return The text with all variables replaced.
   */
  @Override
  public String processLink( final String text ) {
    return replace( text, getDefinitions() );
  }

  /**
   * Returns the map to use for variable substitution.
   *
   * @return A map of variable names to values.
   */
  protected Map<String, String> getDefinitions() {
    return mDefinitions;
  }

}
