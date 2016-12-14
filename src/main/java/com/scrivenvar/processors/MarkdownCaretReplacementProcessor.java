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

import static com.scrivenvar.Constants.CARET_POSITION_HTML;
import static com.scrivenvar.Constants.CARET_POSITION_MD;

/**
 * Responsible for replacing the caret position marker with an HTML element
 * suitable to use as a reference for scrolling a view port.
 *
 * @author White Magic Software, Ltd.
 */
public class MarkdownCaretReplacementProcessor extends AbstractProcessor<String> {
  private static final int INDEX_NOT_FOUND = -1;

  public MarkdownCaretReplacementProcessor( final Processor<String> processor ) {
    super( processor );
  }

  /**
   * Replaces each MD_CARET_POSITION with an HTML element that has an id
   * attribute of CARET_POSITION. This should only replace one item.
   *
   * @param t The text that contains
   *
   * @return
   */
  @Override
  public String processLink( final String t ) {
    return replace(t, CARET_POSITION_MD, CARET_POSITION_HTML );
  }

  /**
   * Replaces the needle with thread in the given haystack. Based on Apache
   * Commons 3 StringUtils.replace method. Should be faster than
   * String.replace, which performs a little regex under the hood.
   *
   * @param haystack Search this string for the needle, must not be null.
   * @param needle The text to find in the haystack.
   * @param thread Replace the needle with this text, if the needle is found.
   *
   * @return The haystack with the first instance of needle replaced with
   * thread.
   */
  private static String replace(
    final String haystack, final String needle, final String thread ) {

    final int end = haystack.indexOf( needle, 0 );

    if( end == INDEX_NOT_FOUND ) {
      return haystack;
    }

    int start = 0;
    final int needleLength = needle.length();

    int increase = thread.length() - needleLength;
    increase = (increase < 0 ? 0 : increase);
    final StringBuilder buffer = new StringBuilder( haystack.length() + increase );

    if( end != INDEX_NOT_FOUND ) {
      buffer.append( haystack.substring( start, end ) ).append( thread );
      start = end + needleLength;
    }

    return buffer.append( haystack.substring( start ) ).toString();
  }
}
