/*
 * Copyright 2020 White Magic Software, Ltd.
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
package com.scrivenvar.editors.markdown;

import com.vladsch.flexmark.ast.Link;

/**
 * Represents the model for a hyperlink: text, url, and title.
 */
public class HyperlinkModel {

  private String text;
  private String url;
  private String title;

  /**
   * Constructs a new hyperlink model in Markdown format by default with no
   * title (i.e., tooltip).
   *
   * @param text The hyperlink text displayed (e.g., displayed to the user).
   * @param url  The destination URL (e.g., when clicked).
   */
  public HyperlinkModel( final String text, final String url ) {
    this( text, url, null );
  }

  /**
   * Constructs a new hyperlink model for the given AST link.
   *
   * @param link A markdown link.
   */
  public HyperlinkModel( final Link link ) {
    this(
        link.getText().toString(),
        link.getUrl().toString(),
        link.getTitle().toString()
    );
  }

  /**
   * Constructs a new hyperlink model in Markdown format by default.
   *
   * @param text  The hyperlink text displayed (e.g., displayed to the user).
   * @param url   The destination URL (e.g., when clicked).
   * @param title The hyperlink title (e.g., shown as a tooltip).
   */
  public HyperlinkModel( final String text, final String url,
                         final String title ) {
    setText( text );
    setUrl( url );
    setTitle( title );
  }

  /**
   * Returns the string in Markdown format by default.
   *
   * @return A markdown version of the hyperlink.
   */
  @Override
  public String toString() {
    String format = "%s%s%s";

    if( hasText() ) {
      format = "[%s]" + (hasTitle() ? "(%s \"%s\")" : "(%s%s)");
    }

    // Becomes ""+URL+"" if no text is set.
    // Becomes [TITLE]+(URL)+"" if no title is set.
    // Becomes [TITLE]+(URL+ \"TITLE\") if title is set.
    return String.format( format, getText(), getUrl(), getTitle() );
  }

  public final void setText( final String text ) {
    this.text = nullSafe( text );
  }

  public final void setUrl( final String url ) {
    this.url = nullSafe( url );
  }

  public final void setTitle( final String title ) {
    this.title = nullSafe( title );
  }

  /**
   * Answers whether text has been set for the hyperlink.
   *
   * @return true This is a text link.
   */
  public boolean hasText() {
    return !getText().isEmpty();
  }

  /**
   * Answers whether a title (tooltip) has been set for the hyperlink.
   *
   * @return true There is a title.
   */
  public boolean hasTitle() {
    return !getTitle().isEmpty();
  }

  public String getText() {
    return this.text;
  }

  public String getUrl() {
    return this.url;
  }

  public String getTitle() {
    return this.title;
  }

  private String nullSafe( final String s ) {
    return s == null ? "" : s;
  }
}
