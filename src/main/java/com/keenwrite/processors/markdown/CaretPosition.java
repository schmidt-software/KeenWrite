/*
 * Copyright 2020 White Magic Software, Ltd.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.keenwrite.processors.markdown;

import com.keenwrite.util.GenericBuilder;
import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.model.Paragraph;
import org.reactfx.collection.LiveList;

import java.util.Collection;

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

  /**
   * Used for building a new {@link CaretPosition} instance.
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

    public void setParagraph( final ObservableValue<Integer> paragraph ) {
      mParagraph = paragraph;
    }

    public void setParagraphs(
        final LiveList<Paragraph<Collection<String>, String,
            Collection<String>>> paragraphs ) {
      mParagraphs = paragraphs;
    }

    public void setTextOffset( final ObservableValue<Integer> textOffset ) {
      mTextOffset = textOffset;
    }

    public void setParaOffset( final ObservableValue<Integer> paraOffset ) {
      mParaOffset = paraOffset;
    }
  }

  private final Mutator mMutator;

  /**
   * Force using the builder pattern.
   */
  private CaretPosition( final Mutator mutator ) {
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
    final int offset = getTextOffset();
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

  public ObservableValue<Integer> textOffsetProperty() {
    return mMutator.mTextOffset;
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
                getParagraphCount(),
                getTextOffset() );
  }
}
