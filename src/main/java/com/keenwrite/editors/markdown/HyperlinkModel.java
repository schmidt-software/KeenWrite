/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.markdown;

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
   * @param link A Markdown link.
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
  public HyperlinkModel(
    final String text, final String url, final String title ) {
    setText( text );
    setUrl( url );
    setTitle( title );
  }

  /**
   * Returns the string in Markdown format by default.
   *
   * @return A Markdown version of the hyperlink.
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
    this.text = sanitize( text );
  }

  public final void setUrl( final String url ) {
    this.url = sanitize( url );
  }

  public final void setTitle( final String title ) {
    this.title = sanitize( title );
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

  private String sanitize( final String s ) {
    return s == null ? "" : s;
  }
}
