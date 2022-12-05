/* Copyright 2020-2021 White Magic Software, Ltd. -- All rights reserved. */
package com.keenwrite.spelling.impl;

import com.keenwrite.spelling.api.SpellCheckListener;
import com.keenwrite.spelling.api.SpellChecker;
import io.gitlab.rxp90.jsymspell.SymSpell;
import io.gitlab.rxp90.jsymspell.SymSpellBuilder;
import io.gitlab.rxp90.jsymspell.Verbosity;
import io.gitlab.rxp90.jsymspell.api.SuggestItem;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.keenwrite.events.StatusEvent.clue;
import static io.gitlab.rxp90.jsymspell.Verbosity.ALL;
import static io.gitlab.rxp90.jsymspell.Verbosity.CLOSEST;
import static java.lang.Character.isLetter;

/**
 * Responsible for spell checking using {@link SymSpell}.
 */
public class SymSpellSpeller implements SpellChecker {
  private final BreakIterator mBreakIterator = BreakIterator.getWordInstance();
  private final SymSpell mSymSpell;

  /**
   * Creates a new spellchecker for a lexicon of words in the specified file.
   *
   * @param lexicon The word-frequency map.
   * @return An instance of {@link SpellChecker} that can check if a word
   * is correct and suggest alternatives, or {@link PermissiveSpeller} if the
   * lexicon cannot be loaded.
   */
  public static SpellChecker forLexicon( final Map<String, Long> lexicon ) {
    assert lexicon != null;
    assert !lexicon.isEmpty();

    try {
      return new SymSpellSpeller(
        new SymSpellBuilder()
          .setUnigramLexicon( lexicon )
          .build()
      );
    } catch( final Exception ex ) {
      clue( ex );
      return new PermissiveSpeller();
    }
  }

  /**
   * Prevent direct instantiation so that only the {@link SpellChecker}
   * interface is available.
   *
   * @param symSpell The implementation-specific spell checker.
   */
  private SymSpellSpeller( final SymSpell symSpell ) {
    assert symSpell != null;

    mSymSpell = symSpell;
  }

  /**
   * This expensive operation is only called for viable words, not for
   * single punctuation characters or whitespace.
   *
   * @param lexeme The word to check for correctness.
   * @return {@code false} if the word is not in the lexicon.
   */
  @Override
  public boolean inLexicon( final String lexeme ) {
    assert lexeme != null;
    assert !lexeme.isEmpty();

    final var words = lookup( lexeme, CLOSEST );
    return !words.isEmpty() && lexeme.equals( words.get( 0 ).getSuggestion() );
  }

  @Override
  public List<String> suggestions( final String lexeme, int count ) {
    assert lexeme != null;
    assert !lexeme.isEmpty();

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
    final String text,
    final SpellCheckListener consumer ) {
    assert text != null;
    assert consumer != null;

    mBreakIterator.setText( text );

    int boundaryIndex = mBreakIterator.first();
    int previousIndex = 0;

    while( boundaryIndex != BreakIterator.DONE ) {
      final var lex =
        text.substring( previousIndex, boundaryIndex ).toLowerCase();

      // Get the lexeme for the possessive.
      final var pos = lex.endsWith( "'s" ) || lex.endsWith( "’s" );
      final var lexeme = pos ? lex.substring( 0, lex.length() - 2 ) : lex;

      if( isWord( lexeme ) && !inLexicon( lexeme ) ) {
        consumer.accept( lex, previousIndex, boundaryIndex );
      }

      previousIndex = boundaryIndex;
      boundaryIndex = mBreakIterator.next();
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
    assert word != null;

    return !word.isBlank() && isLetter( word.charAt( 0 ) );
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
    assert lexeme != null;
    assert v != null;

    return mSymSpell.lookup( lexeme, v );
  }
}
