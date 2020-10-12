package com.keenwrite.processors.markdown;

import com.keenwrite.util.GenericBuilder;

import static com.keenwrite.Constants.STATUS_BAR_LINE;
import static com.keenwrite.Messages.get;

/**
 * Represents the absolute, relative, and maximum position of the caret.
 * The caret position is a character offset into the text.
 */
public class CaretPosition {
  public static GenericBuilder<CaretPosition.Mutator, CaretPosition> builder() {
    return GenericBuilder.of( CaretPosition.Mutator::new, CaretPosition::new );
  }

  public static class Mutator {
    /**
     * Caret offset into the full text, represented as a string index.
     */
    private int mTextOffset;

    /**
     * Caret offset into the current paragraph, represented as a string index.
     */
    private int mParaOffset;

    /**
     * Caret's current paragraph index (i.e., current caret line number).
     */
    private int mParagraph;

    /**
     * Maximum paragraph index in the text (i.e., number of lines).
     */
    private int mMaxParagraph;

    public void setTextOffset( final int textOffset ) {
      mTextOffset = textOffset;
    }

    public void setParaOffset( final int paraOffset ) {
      mParaOffset = paraOffset;
    }

    public void setParagraph( final int paragraph ) {
      mParagraph = paragraph;
    }

    public void setMaxParagraph( final int maxParagraph ) {
      mMaxParagraph = maxParagraph;
    }
  }

  private final Mutator mMutator;

  /**
   * Force using the builder pattern.
   */
  private CaretPosition( final Mutator mutator ) {
    mMutator = mutator;
  }

  /**
   * Answers whether the caret's offset into the text is before the given
   * offset.
   *
   * @param offset Compared against the caret's text offset.
   * @return {@code true} the caret's offset is before the given offset.
   */
  public boolean isBeforeText( final int offset ) {
    return getTextOffset() < offset;
  }

  /**
   * Answers whether the caret's offset into the text is before the given
   * offset.
   *
   * @param offset Compared against the caret's text offset.
   * @return {@code true} the caret's offset is after the given offset.
   */
  public boolean isAfterText( final int offset ) {
    return getTextOffset() > offset;
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

  private int getTextOffset() {
    return mMutator.mTextOffset;
  }

  private int getParaOffset() {
    return mMutator.mParaOffset;
  }

  private int getMaxParagraph() {
    return mMutator.mMaxParagraph;
  }

  private int getParagraph() {
    return mMutator.mParagraph;
  }

  /**
   * Returns a human-readable string that shows the current caret position
   * within the text. Typically this will include the current line number,
   * the number of lines, and the character offset into the text.
   *
   * @return A string to present to an end user.
   */
  @Override
  public String toString() {
    return get( STATUS_BAR_LINE,
                getParagraph(),
                getMaxParagraph(),
                getTextOffset() );
  }
}
