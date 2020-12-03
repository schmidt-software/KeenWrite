/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors;

import com.keenwrite.TextResource;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Responsible for differentiating an instance of {@link TextResource} from
 * other {@link TextResource} subtypes, such as a {@link TextDefinition}.
 * This is primarily used as a marker interface, but also defines a minimal
 * set of functionality required by all {@link TextEditor} instances, which
 * includes scrolling facilities.
 */
public interface TextEditor extends TextResource {

  /**
   * Returns the scrollbars associated with the editor's view so that they
   * can be moved for synchronized scrolling.
   *
   * @return The initialized horizontal and vertical scrollbars.
   */
  VirtualizedScrollPane<StyleClassedTextArea> getScrollPane();

  /**
   * Requests undoing the last text-changing action.
   */
  void undo();

  /**
   * Requests redoing the last text-changing action that was undone.
   */
  void redo();

  /**
   * Requests cutting the selected text, or the current line if none selected.
   */
  void cut();

  /**
   * Requests copying the selected text, or no operation if none selected.
   */
  void copy();

  /**
   * Requests pasting from the clipboard into the editor. This will replace
   * text if selected, otherwise the clipboard contents are inserted at the
   * cursor.
   */
  void paste();

  /**
   * Requests selecting the entire document. This will replace the existing
   * selection, if any.
   */
  void selectAll();

  /**
   * Requests making the selected text, or word at caret, bold.
   */
  default void bold() {
  }

  /**
   * Requests making the selected text, or word at caret, italic.
   */
  default void italic() {
  }

  /**
   * Requests making the selected text, or word at caret, a superscript.
   */
  default void superscript() {
  }

  /**
   * Requests making the selected text, or word at caret, a subscript.
   */
  default void subscript() {
  }

  /**
   * Requests making the selected text, or word at caret, struck.
   */
  default void strikethrough() {
  }

  /**
   * Requests making the selected text, or word at caret, a blockquote block.
   */
  default void blockquote() {
  }

  /**
   * Requests making the selected text, or word at caret, inline code.
   */
  default void code() {
  }

  /**
   * Requests making the selected text, or word at caret, a fenced code block.
   */
  default void fencedCodeBlock() {
  }

  /**
   * Requests making the selected text, or word at caret, a heading.
   *
   * @param level The heading level to apply (typically 1 through 3).
   */
  default void heading( final int level ) {
  }

  /**
   * Requests making the selected text, or word at caret, an unordered list
   * block.
   */
  default void unorderedList() {
  }

  /**
   * Requests making the selected text, or word at caret, an ordered list block.
   */
  default void orderedList() {
  }

  /**
   * Requests making the selected text, or inserting at the caret, a
   * horizontal rule.
   */
  default void horizontalRule() {
  }
}
