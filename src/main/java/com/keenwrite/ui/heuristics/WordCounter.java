/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.ui.heuristics;

import com.whitemagicsoftware.keencount.Tokenizer;
import com.whitemagicsoftware.keencount.TokenizerFactory;

import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Responsible for counting unique words as well as total words in a document.
 */
public class WordCounter {
  /**
   * Parses documents into word counts.
   */
  private final Tokenizer mTokenizer;

  /**
   * Constructs a new {@link WordCounter} instance using the given tokenizer.
   *
   * @param tokenizer The class responsible for parsing a document into unique
   *                  and total word counts.
   */
  private WordCounter( final Tokenizer tokenizer ) {
    mTokenizer = tokenizer;
  }

  /**
   * Counts the number of unique words in the document.
   *
   * @param document The document to tally.
   * @return The total number of words in the document.
   */
  public int count( final String document ) {
    return count( document, ( k, count ) -> {} );
  }

  /**
   * Counts the number of unique words in the document.
   *
   * @param document The document to tally.
   * @param consumer The action to take for each unique word/count pair.
   * @return The total number of words in the document.
   */
  public int count(
    final String document, final BiConsumer<String, Integer> consumer ) {
    final var tokens = mTokenizer.tokenize( document );
    final var sum = new int[]{0};

    tokens.forEach( ( k, v ) -> {
      final var count = v[ 0 ];
      consumer.accept( k, count );
      sum[ 0 ] += count;
    } );

    return sum[ 0 ];
  }

  /**
   * Constructs a new {@link WordCounter} capable of tokenizing a document
   * into words using the given {@link Locale}.
   *
   * @param locale The {@link Tokenizer}'s language settings.
   */
  public static WordCounter create( final Locale locale ) {
    return new WordCounter( createTokenizer( locale ) );
  }

  /**
   * Creates a tokenizer for English text (can handle most Latin languages).
   *
   * @return An English-based tokenizer for counting words.
   */
  private static Tokenizer createTokenizer( final Locale language ) {
    return TokenizerFactory.create( language );
  }
}
