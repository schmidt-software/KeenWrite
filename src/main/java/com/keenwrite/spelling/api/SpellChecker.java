/* Copyright 2020 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.api;

import java.util.List;

/**
 * Defines the responsibilities for a spell checking API. The intention is
 * to allow different spell checking implementations to be used by the
 * application, such as SymSpell and LinSpell.
 */
public interface SpellChecker {

  /**
   * Answers whether the given lexeme, in whole, is found in the lexicon. The
   * lexicon lookup is performed case-insensitively. This method should be
   * used instead of {@link #suggestions(String, int)} for performance reasons.
   *
   * @param lexeme The word to check for correctness.
   * @return {@code true} if the lexeme is in the lexicon.
   */
  boolean inLexicon( String lexeme );

  /**
   * Gets a list of spelling corrections for the given lexeme.
   *
   * @param lexeme A word to check for correctness that's not in the lexicon.
   * @param count  The maximum number of alternatives to return.
   * @return A list of words in the lexicon that are similar to the given
   * lexeme.
   */
  List<String> suggestions( String lexeme, int count );

  /**
   * Iterates over the given text, emitting starting and ending offsets into
   * the text for every word that is missing from the lexicon.
   *
   * @param text     The text to check for words missing from the lexicon.
   * @param consumer Every missing word emits a message with the starting
   *                 and ending offset into the text where said word is found.
   */
  void proofread( String text, SpellCheckListener consumer );
}
