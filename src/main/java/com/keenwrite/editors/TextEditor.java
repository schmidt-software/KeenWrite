/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors;

import com.keenwrite.Caret;
import javafx.scene.control.IndexRange;
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

  StyleClassedTextArea getTextArea();

  /**
   * Requests that styling be added to the document between the given
   * integer values.
   *
   * @param indexes Document offset where style is to start and end.
   * @param style   The style class to apply between the given offset indexes.
   */
  default void stylize( final IndexRange indexes, final String style ) {
  }

  /**
   * Requests that the most recent styling for the given style class be
   * removed from the document between the given integer values.
   */
  default void unstylize( final String style ) {
  }

  /**
   * Returns the complete text for the specified paragraph index.
   *
   * @param paragraph The zero-based paragraph index.
   * @throws IndexOutOfBoundsException The paragraph index is less than zero
   *                                   or greater than the number of
   *                                   paragraphs in the document.
   */
  String getText( int paragraph ) throws IndexOutOfBoundsException;

  /**
   * Returns the text between the indexes specified by the given
   * {@link IndexRange}.
   *
   * @param indexes The start and end document indexes to reference.
   * @return The text between the specified indexes.
   * @throws IndexOutOfBoundsException The indexes are invalid.
   */
  String getText( IndexRange indexes ) throws IndexOutOfBoundsException;

  /**
   * Moves the caret to the given document offset.
   *
   * @param offset The absolute offset into the document, zero-based.
   */
  void moveTo( final int offset );

  /**
   * Returns an object that can be used to track the current caret position
   * within the document.
   *
   * @return The caret's position, which is updated continuously.
   */
  Caret getCaret();

  /**
   * Replaces the text within the given range with the given string.
   *
   * @param indexes The starting and ending document indexes that represent
   *                the range of text to replace.
   * @param s       The text to replace, which can be shorter or longer than the
   *                specified range.
   */
  void replaceText( IndexRange indexes, String s );

  /**
   * Returns the starting and ending indexes into the document for the
   * word at the current caret position.
   * <p>
   * Finds the start and end indexes for the word in the current document,
   * where the caret is located. There are a few different scenarios, where
   * the caret can be at: the start, end, or middle of a word; also, the
   * caret can be at the end or beginning of a punctuated word; as well, the
   * caret could be at the beginning or end of the line or document.
   * </p>
   *
   * @return The start and ending index into the current document that
   * represent the word boundaries of the word under the caret.
   */
  IndexRange getCaretWord();

  /**
   * Convenience method to get the word at the current caret position.
   *
   * @return This will return the empty string if the caret is out of bounds.
   */
  default String getCaretWordText() {
    return getText( getCaretWord() );
  }

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
  default void bold() { }

  /**
   * Requests making the selected text, or word at caret, italic.
   */
  default void italic() { }

  /**
   * Requests making the selected text, or word at caret, monospace.
   */
  default void monospace() { }

  /**
   * Requests making the selected text, or word at caret, a superscript.
   */
  default void superscript() { }

  /**
   * Requests making the selected text, or word at caret, a subscript.
   */
  default void subscript() { }

  /**
   * Requests making the selected text, or word at caret, struck.
   */
  default void strikethrough() { }

  /**
   * Requests making the selected text, or word at caret, a blockquote block.
   */
  default void blockquote() { }

  /**
   * Requests making the selected text, or word at caret, inline code.
   */
  default void code() { }

  /**
   * Requests making the selected text, or word at caret, a fenced code block.
   */
  default void fencedCodeBlock() { }

  /**
   * Requests making the selected text, or word at caret, a heading.
   *
   * @param level The heading level to apply (typically 1 through 3).
   */
  default void heading( final int level ) { }

  /**
   * Requests making the selected text, or word at caret, an unordered list
   * block.
   */
  default void unorderedList() { }

  /**
   * Requests making the selected text, or word at caret, an ordered list block.
   */
  default void orderedList() { }

  /**
   * Requests making the selected text, or inserting at the caret, a
   * horizontal rule.
   */
  default void horizontalRule() { }
}
