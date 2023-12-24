/* Copyright 2023 White Magic Software, Ltd. -- All rights reserved.
 *
 * SPDX-License-Identifier: MIT
 */
package com.keenwrite.ui.models;

/**
 * Represents the model for an image: text, url, and title.
 */
public final class ImageModel extends ObjectModel {

  /**
   * Constructs a new image model in Markdown format by default with no
   * title (i.e., tooltip).
   *
   * @param text The alternate text (e.g., displayed to the user).
   */
  public ImageModel( final String text ) {
    super( text );
  }

  /**
   * Returns the string in Markdown format by default.
   *
   * @return An image reference using Markdown syntax.
   */
  @Override
  public String toString() {
    final String format = hasText()
      ? STR."![%s]\{hasTitle() ? "(%s \"%s\")" : "(%s%s)"}"
      : "![%s](%s)%s";

    return String.format( format, getText(), getUrl(), getTitle() );
  }
}
