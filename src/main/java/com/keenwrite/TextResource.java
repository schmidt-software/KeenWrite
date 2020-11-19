package com.keenwrite;

import com.keenwrite.processors.markdown.CaretPosition;

/**
 * A text resource can be persisted and retrieved from its persisted location.
 */
public interface TextResource {
  /**
   * Sets the text string that to be changed through some graphical user
   * interface. For example, a YAML document must be parsed from the given
   * text string into a tree view with which the user may interact.
   *
   * @param text The new content for the resource.
   */
  void setText( String text );

  /**
   * Returns the text string that may have been modified by the user through
   * some graphical user interface.
   *
   * @return The text value, based on the value set from
   * {@link #setText(String)}, but possibly mutated.
   */
  String getText();

  /**
   * Changes the caret position in implementations that support it. Performs
   * no operation by default.
   *
   * @param position The new caret position.
   */
  default void setInsertionPoint( final int position ) {
  }

  /**
   * Returns the container that represents the caret's position within the
   * text editor. If this object has no editor that supports a caret, then
   * this will return a default implementation whose behaviour is undefined.
   *
   * @return An instance of {@link CaretPosition} that reflects the position
   * of the caret in the document being edited, or {@code null} if the document
   * has no caret.
   */
  default CaretPosition createCaretPosition() {
    return null;
  }
}
