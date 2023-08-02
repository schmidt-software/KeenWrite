/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.search;

import com.keenwrite.util.CyclicIterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexRange;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.List;

import static org.ahocorasick.trie.Trie.builder;

/**
 * Responsible for finding words in a text document. This implementation uses
 * a {@link Trie} for efficiency.
 */
public final class SearchModel {
  private final ObjectProperty<IndexRange> mMatchOffset =
      new SimpleObjectProperty<>();
  private final ObjectProperty<Integer> mMatchCount =
      new SimpleObjectProperty<>();
  private final ObjectProperty<Integer> mMatchIndex =
      new SimpleObjectProperty<>();

  private CyclicIterator<Emit> mMatches = new CyclicIterator<>( List.of() );

  private String mNeedle = "";

  /**
   * Creates a new {@link SearchModel} that finds all text string in a
   * document simultaneously.
   */
  public SearchModel() {
  }

  public ObjectProperty<Integer> matchCountProperty() {
    return mMatchCount;
  }

  public ObjectProperty<Integer> matchIndexProperty() {
    return mMatchIndex;
  }

  /**
   * Observers watch this property to be notified when a needle has been
   * found in the haystack. Use {@link IndexRange#getStart()} to get the
   * absolute offset into the text (zero-based).
   *
   * @return The {@link IndexRange} property to observe, representing the
   * most recently matched text offset into the document.
   */
  public ObservableValue<IndexRange> matchOffsetProperty() {
    return mMatchOffset;
  }

  /**
   * Searches the document for text matching the given parameter value. This
   * is the main entry point for kicking off text searches.
   *
   * @param needle   The text string to find in the document, no regex allowed.
   * @param haystack The document to search within for a text string.
   */
  public void search( final String needle, final String haystack ) {
    assert needle != null;
    assert haystack != null;

    final var trie = builder()
        .ignoreCase()
        .ignoreOverlaps()
        .addKeyword( needle )
        .build();
    final var emits = trie.parseText( haystack );

    mMatches = new CyclicIterator<>( emits );
    mMatchCount.set( emits.size() );
    mNeedle = needle;
    advance();
  }

  /**
   * Searches the document for the last known needle.
   *
   * @param haystack The new text to search.
   */
  public void search( final String haystack ) {
    search( mNeedle, haystack );
  }

  /**
   * Moves the search iterator to the next match, wrapping as needed.
   */
  public void advance() {
    if( mMatches.hasNext() ) {
      setCurrent( mMatches.next() );
    }
  }

  /**
   * Moves the search iterator to the previous match, wrapping as needed.
   */
  public void retreat() {
    if( mMatches.hasPrevious() ) {
      setCurrent( mMatches.previous() );
    }
  }

  private void setCurrent( final Emit emit ) {
    mMatchOffset.set( new IndexRange( emit.getStart(), emit.getEnd() ) );
    mMatchIndex.set( mMatches.getIndex() + 1 );
  }
}
