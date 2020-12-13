/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.search;

import com.keenwrite.util.CyclicIterator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexRange;
import org.ahocorasick.trie.Emit;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.ahocorasick.trie.Trie.TrieBuilder;
import static org.ahocorasick.trie.Trie.builder;

/**
 * Responsible for finding words in a text document.
 */
public class SearchModel {
  private final TrieBuilder mBuilder = builder().ignoreCase().ignoreOverlaps();

  private final ObjectProperty<IndexRange> mMatchProperty =
      new SimpleObjectProperty<>();

  private ListIterator<Emit> mMatches = CyclicIterator.of( List.of() );

  /**
   * The document to search.
   */
  private final String mHaystack;

  /**
   * Creates a new {@link SearchModel} that finds all text string in a
   * document simultaneously.
   *
   * @param haystack The document to search for a text string.
   */
  public SearchModel( final String haystack ) {
    mHaystack = haystack;
  }

  /**
   * Observers can bind to this property to be informed when the current
   * matched needle has been found in the haystack.
   *
   * @return The {@link IndexRange} property to observe, representing the
   * most recently matched text offset into the document.
   */
  public ObjectProperty<IndexRange> matchProperty() {
    return mMatchProperty;
  }

  /**
   * Searches the document for text matching the given parameter value. This
   * is the main entry point for kicking off text searches.
   *
   * @param needle The text string to find in the document, no regex allowed.
   */
  public void search( final String needle ) {
    final var trie = mBuilder.addKeyword( needle ).build();
    final var emits = trie.parseText( mHaystack );

    mMatches = CyclicIterator.of( new ArrayList<>( emits ) );
  }

  /**
   * Moves the search iterator to the next match, wrapping as needed.
   */
  public void advance() {
    setCurrent( mMatches.next() );
  }

  /**
   * Moves the search iterator to the previous match, wrapping as needed.
   */
  public void retreat() {
    setCurrent( mMatches.previous() );
  }

  private void setCurrent( final Emit emit ) {
    mMatchProperty.set( new IndexRange( emit.getStart(), emit.getEnd() ) );
  }
}
