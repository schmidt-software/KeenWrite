/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite;

import com.keenwrite.util.GenericBuilder;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

import java.util.Collection;

import static com.keenwrite.Constants.STATUS_BAR_LINE;
import static com.keenwrite.Messages.get;

/**
 * Represents the absolute, relative, and maximum position of the caret. The
 * caret position is a character offset into the text.
 */
public class Caret {

  public static GenericBuilder<Caret.Mutator, Caret> builder() {
    return GenericBuilder.of( Caret.Mutator::new, Caret::new );
  }

  /**
   * Used for building a new {@link Caret} instance.
   */
  public static class Mutator {
    /**
     * Caret's current paragraph index (i.e., current caret line number).
     */
    private ObservableValue<Integer> mParagraph;

    /**
     * Used to count the number of lines in the text editor document.
     */
    private LiveList<Paragraph<Collection<String>, String,
      Collection<String>>> mParagraphs;

    /**
     * Caret offset into the full text, represented as a string index.
     */
    private ObservableValue<Integer> mTextOffset;

    /**
     * Caret offset into the current paragraph, represented as a string index.
     */
    private ObservableValue<Integer> mParaOffset;

    /**
     * Total number of characters in the document.
     */
    private ObservableValue<Integer> mTextLength;

    /**
     * Configures this caret position using properties from the given editor.
     *
     * @param editor The text editor that has a caret with position properties.
     */
    public void setEditor( final StyleClassedTextArea editor ) {
      mParagraph = editor.currentParagraphProperty();
      mParagraphs = editor.getParagraphs();
      mParaOffset = editor.caretColumnProperty();
      mTextOffset = editor.caretPositionProperty();
      mTextLength = editor.lengthProperty();
    }
  }

  private final Mutator mMutator;

  /**
   * Force using the builder pattern.
   */
  private Caret( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
  }

  /**
   * Allows observers to be notified when the value of the caret changes.
   *
   * @return An observer for the caret's document offset.
   */
  public ObservableValue<Integer> textOffsetProperty() {
    return mMutator.mTextOffset;
  }

  /**
   * Answers whether the caret's offset into the text is between the given
   * offsets.
   *
   * @param began Starting value compared against the caret's text offset.
   * @param ended Ending value compared against the caret's text offset.
   * @return {@code true} when the caret's text offset is between the given
   * values, inclusively (for either value).
   */
  public boolean isBetweenText( final int began, final int ended ) {
    final var offset = getTextOffset();
    return began <= offset && offset <= ended;
  }

  /**
   * Answers whether the caret's offset into the paragraph is before the given
   * offset.
   *
   * @param offset Compared against the caret's paragraph offset.
   * @return {@code true} the caret's offset is before the given offset.
   */
  public boolean isBeforeColumn( final int offset ) {
    return getParaOffset() < offset;
  }

  /**
   * Answers whether the caret's offset into the text is before the given
   * text offset.
   *
   * @param offset Compared against the caret's text offset.
   * @return {@code true} the caret's offset is after the given offset.
   */
  public boolean isAfterColumn( final int offset ) {
    return getParaOffset() > offset;
  }

  /**
   * Answers whether the caret's offset into the text exceeds the length of
   * the text.
   *
   * @return {@code true} when the caret is at the end of the text boundary.
   */
  public boolean isAfterText() {
    return getTextOffset() >= getTextLength();
  }

  public boolean isAfter( final int offset ) {
    return offset >= getTextOffset();
  }

  private int getParagraph() {
    return mMutator.mParagraph.getValue();
  }

  /**
   * Returns the number of lines in the text editor.
   *
   * @return The size of the text editor's paragraph list plus one.
   */
  private int getParagraphCount() {
    return mMutator.mParagraphs.size() + 1;
  }

  /**
   * Returns the absolute position of the caret within the entire document.
   *
   * @return A zero-based index of the caret position.
   */
  private int getTextOffset() {
    return mMutator.mTextOffset.getValue();
  }

  /**
   * Returns the position of the caret within the current paragraph being
   * edited.
   *
   * @return A zero-based index of the caret position relative to the
   * current paragraph.
   */
  private int getParaOffset() {
    return mMutator.mParaOffset.getValue();
  }

  /**
   * Returns the total number of characters in the document being edited.
   *
   * @return A zero-based count of the total characters in the document.
   */
  private int getTextLength() {
    return mMutator.mTextLength.getValue();
  }

  /**
   * Returns a human-readable string that shows the current caret position
   * within the text. Typically this will include the current line number,
   * the number of lines, and the character offset into the text.
   * <p>
   * If the {@link Caret} has not been properly built, this will return a
   * string for the status bar having all values set to zero. This can happen
   * during unit testing, but should not happen any other time.
   * </p>
   *
   * @return A string to present to an end user.
   */
  @Override
  public String toString() {
    try {
      return get( STATUS_BAR_LINE,
                  getParagraph() + 1,
                  getParagraphCount(),
                  getTextOffset() + 1 );
    } catch( final NullPointerException ex ) {
      return get( STATUS_BAR_LINE, 0, 0, 0 );
    }
  }
}
