package com.keenwrite.processors.markdown;

public class CaretPosition {
  /**
   * Caret offset into the full text, represented as a string index.
   */
  private final int mTextOffset;

  /**
   * Caret offset into the current paragraph, represented as a string index.
   */
  private final int mParaOffset;

  /**
   * @param textOffset Caret's offset into the full text, as a string index.
   * @param paraOffset Caret's offset into the paragraph, as a string index.
   */
  public CaretPosition( final int textOffset, final int paraOffset ) {
    mTextOffset = textOffset;
    mParaOffset = paraOffset;
  }

  /**
   * Answers whether the caret's offset into the text is before the given
   * text offset.
   *
   * @param offset Compared against the caret's text offset.
   * @return {@code true} the caret's offset is before the given offset.
   */
  public boolean isBefore( final int offset ) {
    return mTextOffset < offset;
  }

  /**
   * Answers whether the caret's offset into the text is before the given
   * text offset.
   *
   * @param offset Compared against the caret's text offset.
   * @return {@code true} the caret's offset is after the given offset.
   */
  public boolean isAfter( final int offset ) {
    return mTextOffset > offset;
  }
}
