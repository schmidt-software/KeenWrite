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
package com.scrivenvar.spelling.impl;

import com.scrivenvar.spelling.api.SpellChecker;
import io.gitlab.rxp90.jsymspell.SuggestItem;
import io.gitlab.rxp90.jsymspell.SymSpell;
import io.gitlab.rxp90.jsymspell.SymSpellBuilder;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity;
import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity.ALL;
import static io.gitlab.rxp90.jsymspell.SymSpell.Verbosity.CLOSEST;
import static java.lang.Character.isLetter;

/**
 * Responsible for spell checking using {@link SymSpell}.
 */
public class SymSpellSpeller implements SpellChecker {
  private final SymSpell mSymSpell;

  /**
   * Creates a new lexicon for the given collection of lexemes.
   *
   * @param lexiconWords The words in the lexicon to add for spell checking,
   *                     must not be empty.
   * @return An instance of {@link SpellChecker} that can check if a word
   * is correct and suggest alternatives.
   */
  public static SpellChecker forLexicon(
      final Collection<String> lexiconWords ) {
    assert lexiconWords != null && !lexiconWords.isEmpty();

    final SymSpellBuilder builder = new SymSpellBuilder()
        .setLexiconWords( lexiconWords );

    return new SymSpellSpeller( builder.build() );
  }

  /**
   * Prevent direct instantiation so that only the {@link SpellChecker}
   * interface
   * is available.
   *
   * @param symSpell The implementation-specific spell checker.
   */
  private SymSpellSpeller( final SymSpell symSpell ) {
    mSymSpell = symSpell;
  }

  @Override
  public boolean inLexicon( final String lexeme ) {
    return lookup( lexeme, CLOSEST ).size() == 1;
  }

  @Override
  public List<String> suggestions( final String lexeme, int count ) {
    final List<String> result = new ArrayList<>( count );

    for( final var item : lookup( lexeme, ALL ) ) {
      if( count-- > 0 ) {
        result.add( item.getSuggestion() );
      }
      else {
        break;
      }
    }

    return result;
  }

  @Override
  public void proofread(
      final String text, final BiConsumer<Integer, Integer> consumer ) {
    assert text != null;
    assert consumer != null;

    final BreakIterator wb = BreakIterator.getWordInstance();
    wb.setText( text );

    int boundaryIndex = wb.first();
    int previousIndex = 0;

    while( boundaryIndex != BreakIterator.DONE ) {
      final String substring =
          text.substring( previousIndex, boundaryIndex ).toLowerCase();

      if( isWord( substring ) && !inLexicon( substring ) ) {
        consumer.accept( previousIndex, boundaryIndex );
      }

      previousIndex = boundaryIndex;
      boundaryIndex = wb.next();
    }
  }

  /**
   * Answers whether the given string is likely a word by checking the first
   * character.
   *
   * @param word The word to check.
   * @return {@code true} if the word begins with a letter.
   */
  private boolean isWord( final String word ) {
    return !word.isEmpty() && isLetter( word.charAt( 0 ) );
  }

  /**
   * Returns a list of {@link SuggestItem} instances that provide alternative
   * spellings for the given lexeme.
   *
   * @param lexeme A word to look up in the lexicon.
   * @param v      Influences the number of results returned.
   * @return Alternative lexemes.
   */
  private List<SuggestItem> lookup( final String lexeme, final Verbosity v ) {
    return getSpeller().lookup( lexeme, v );
  }

  private SymSpell getSpeller() {
    return mSymSpell;
  }
}
