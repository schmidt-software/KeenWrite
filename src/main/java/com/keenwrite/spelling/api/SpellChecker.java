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
