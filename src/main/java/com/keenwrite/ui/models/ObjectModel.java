/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.models;

import static com.keenwrite.util.Strings.sanitize;

/**
 * Represents the model for an object containing text, url, and title.
 */
class ObjectModel {
  private String mText;
  private String mUrl;
  private String mTitle;

  /**
   * Constructs a new object model in Markdown format by default with no
   * title (i.e., tooltip).
   *
   * @param text The hyperlink text displayed (e.g., displayed to the user).
   */
  public ObjectModel( final String text ) {
    this( text, null, null );
  }

  /**
   * Constructs a new object model in Markdown format by default.
   *
   * @param text  The text displayed (e.g., to the user).
   * @param url   The destination URL (e.g., when clicked).
   * @param title The text title (e.g., shown as a tooltip).
   */
  public ObjectModel(
    final String text, final String url, final String title ) {
    setText( text );
    setUrl( url );
    setTitle( title );
  }

  public void setText( final String text ) {
    mText = sanitize( text );
  }

  public void setUrl( final String url ) {
    mUrl = sanitize( url );
  }

  public void setTitle( final String title ) {
    mTitle = sanitize( title );
  }

  /**
   * Answers whether text has been set for the model.
   *
   * @return true The text description is set.
   */
  public boolean hasText() {
    return !getText().isEmpty();
  }

  /**
   * Answers whether a title (tooltip) has been set for the model.
   *
   * @return true The title is set.
   */
  public boolean hasTitle() {
    return !getTitle().isEmpty();
  }

  public String getText() {
    return mText;
  }

  public String getUrl() {
    return mUrl;
  }

  public String getTitle() {
    return mTitle;
  }
}
