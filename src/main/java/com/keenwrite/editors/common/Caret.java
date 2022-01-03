/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.editors.common;

import com.keenwrite.util.GenericBuilder;

import java.util.function.Supplier;

import static com.keenwrite.Messages.get;
import static com.keenwrite.constants.Constants.STATUS_BAR_LINE;

/**
 * Represents the absolute, relative, and maximum position of the caret. The
 * caret position is a character offset into the text.
 */
public class Caret {

  private final Mutator mMutator;

  public static GenericBuilder<Caret.Mutator, Caret> builder() {
    return GenericBuilder.of( Caret.Mutator::new, Caret::new );
  }

  /**
   * Configures a caret.
   */
  public static class Mutator {
    /**
     * Caret's current paragraph index (i.e., current caret line number).
     */
    private Supplier<Integer> mParagraph = () -> 0;

    /**
     * Used to count the number of lines in the text editor document.
     */
    private Supplier<Integer> mParagraphs = () -> 1;

    /**
     * Caret offset into the current paragraph, represented as a string index.
     */
    private Supplier<Integer> mParaOffset = () -> 0;

    /**
     * Caret offset into the full text, represented as a string index.
     */
    private Supplier<Integer> mTextOffset = () -> 0;

    /**
     * Total number of characters in the document.
     */
    private Supplier<Integer> mTextLength = () -> 0;

    public void setParagraph( final Supplier<Integer> paragraph ) {
      assert paragraph != null;
      mParagraph = paragraph;
    }

    public void setParagraphs( final Supplier<Integer> paragraphs ) {
      assert paragraphs != null;
      mParagraphs = paragraphs;
    }

    public void setParaOffset( final Supplier<Integer> paraOffset ) {
      assert paraOffset != null;
      mParaOffset = paraOffset;
    }

    public void setTextOffset( final Supplier<Integer> textOffset ) {
      assert textOffset != null;
      mTextOffset = textOffset;
    }

    public void setTextLength( final Supplier<Integer> textLength ) {
      assert textLength != null;
      mTextLength = textLength;
    }
  }

  /**
   * Force using the builder pattern.
   */
  private Caret( final Mutator mutator ) {
    assert mutator != null;

    mMutator = mutator;
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
    return mMutator.mParagraph.get();
  }

  /**
   * Returns the number of lines in the text editor.
   *
   * @return The size of the text editor's paragraph list plus one.
   */
  private int getParagraphCount() {
    return mMutator.mParagraphs.get();
  }

  /**
   * Returns the absolute position of the caret within the entire document.
   *
   * @return A zero-based index of the caret position.
   */
  private int getTextOffset() {
    return mMutator.mTextOffset.get();
  }

  /**
   * Returns the position of the caret within the current paragraph being
   * edited.
   *
   * @return A zero-based index of the caret position relative to the
   * current paragraph.
   */
  private int getParaOffset() {
    return mMutator.mParaOffset.get();
  }

  /**
   * Returns the total number of characters in the document being edited.
   *
   * @return A zero-based count of the total characters in the document.
   */
  private int getTextLength() {
    return mMutator.mTextLength.get();
  }

  /**
   * Returns a human-readable string that shows the current caret position
   * within the text. Typically, this will include the current line number,
   * the number of lines, and the character offset into the text.
   * <p>
   * If the {@link Caret} has not been properly built, this will return a
   * string for the status bar having all values set to zero. This can happen
   * during unit testing.
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
    } catch( final Exception ex ) {
      return get( STATUS_BAR_LINE, 0, 0, 0 );
    }
  }
}
